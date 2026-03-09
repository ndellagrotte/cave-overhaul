#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
PROJECT_DIR="$(cd "$SCRIPT_DIR/.." && pwd)"
RCON_HOST="localhost"
RCON_PORT=25575
RCON_PASS="devtest"
RCON_TIMEOUT=300
GEN_WAIT=60

cd "$PROJECT_DIR"

echo "=== Cave Generation Test Pipeline ==="
echo "Project: $PROJECT_DIR"
echo ""

# 1. Clean previous test world
echo "[1/7] Cleaning previous test world..."
rm -rf run/test_world

# 2. Build
echo "[2/7] Building mod..."
./gradlew build || { echo "BUILD FAILED"; exit 1; }

# 3. Ensure server config exists
echo "[3/7] Ensuring server config..."
if [ ! -f run/eula.txt ]; then
    echo "eula=true" > run/eula.txt
fi
if [ ! -f run/server.properties ]; then
    cat > run/server.properties << 'PROPS'
enable-rcon=true
rcon.port=25575
rcon.password=devtest
server-port=25565
online-mode=false
spawn-protection=0
max-tick-time=120000
level-name=test_world
level-type=minecraft\:normal
gamemode=creative
difficulty=peaceful
PROPS
fi

# 4. Launch server in background
echo "[4/7] Launching server..."
./gradlew runServer --no-daemon --console=plain &
SERVER_PID=$!

cleanup() {
    echo "Cleaning up..."
    if kill -0 "$SERVER_PID" 2>/dev/null; then
        kill "$SERVER_PID" 2>/dev/null || true
        wait "$SERVER_PID" 2>/dev/null || true
    fi
}
trap cleanup EXIT

# 5. Wait for RCON port
echo "[5/7] Waiting for server (RCON port $RCON_PORT, timeout ${RCON_TIMEOUT}s)..."
ELAPSED=0
while ! (echo > /dev/tcp/$RCON_HOST/$RCON_PORT) 2>/dev/null; do
    sleep 5
    ELAPSED=$((ELAPSED + 5))
    if [ "$ELAPSED" -ge "$RCON_TIMEOUT" ]; then
        echo "TIMEOUT waiting for server to start"
        exit 1
    fi
    echo "  ... waiting ($ELAPSED/${RCON_TIMEOUT}s)"
done
echo "  Server RCON is ready."

# 6. Force-load chunks for generation
echo "[6/7] Force-loading chunks and waiting for generation..."
python3 "$SCRIPT_DIR/rcon.py" "$RCON_HOST" "$RCON_PORT" "$RCON_PASS" "forceload add -80 -80 80 80"
echo "  Waiting ${GEN_WAIT}s for chunk generation..."
sleep "$GEN_WAIT"

# 7. Stop server gracefully
echo "[7/7] Stopping server..."
python3 "$SCRIPT_DIR/rcon.py" "$RCON_HOST" "$RCON_PORT" "$RCON_PASS" "stop" || true
echo "  Waiting for server to exit..."
wait "$SERVER_PID" 2>/dev/null || true
trap - EXIT

# Parse results
echo ""
echo "=== Parsing Results ==="
mkdir -p test_results
TIMESTAMP=$(date +%Y%m%d_%H%M%S)
REPORT="test_results/cave_report_${TIMESTAMP}.json"

python3 "$SCRIPT_DIR/parse_cave_logs.py" run/logs/latest.log --output "$REPORT"

echo ""
echo "=== Done ==="
echo "Report: $REPORT"
