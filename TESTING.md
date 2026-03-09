# Cave Generation Testing Pipeline

Automated testing pipeline for validating cave and river generation behavior without manual Minecraft client launches.

## Quick Start

```bash
bash scripts/run_cave_test.sh
```

This will:
1. Clean any previous test world
2. Build the mod
3. Launch a dedicated server with RCON enabled
4. Force-load a grid of chunks around the origin
5. Wait for generation to complete
6. Gracefully stop the server
7. Parse `[CAVEDATA]` log lines into a JSON report

Results are written to `test_results/cave_report_<timestamp>.json`.

## What's Measured

Each chunk logs the following via `[CAVEDATA]` lines in the server log:

| Metric | Description |
|--------|-------------|
| `cave_blocks` | Blocks carved to air by noise cave layers |
| `river_blocks` | Total blocks affected by river generation (liquid + air) |
| `river_liquid` | Blocks set to water/lava by river layers |
| `river_air` | Blocks carved to air by river carving |
| `seed` | World seed |

## Report Output

The parse script (`scripts/parse_cave_logs.py`) produces:

- **Total chunks analyzed**
- **Mean/min/max/stdev/median** for cave_blocks and river_blocks
- **Count/percentage** of chunks with caves, chunks with rivers
- **Chunks with zero caves**
- **Anomalous chunks** with density >2 standard deviations above the mean

## Individual Scripts

### RCON Client
```bash
python3 scripts/rcon.py localhost 25575 devtest "forceload add -80 -80 80 80"
```

### Log Parser
```bash
python3 scripts/parse_cave_logs.py run/logs/latest.log --output report.json
```

## Configuration

Server settings are in `run/server.properties` (gitignored). Key RCON settings:
- `rcon.port=25575`
- `rcon.password=devtest`

## What's NOT Instrumented

- **Biome per chunk**: Not available at the NoiseChunk level without significant plumbing
- **Warp magnitude**: Would require tracking domain warp deltas in NoisetypeDomainWarp; too invasive
- **River segments**: Rivers are continuous noise-based volumes, not discrete segments; `river_blocks` is the appropriate metric

## Extending

To add new metrics:
1. Add a counter field to `CaveDataLogger.ChunkStats`
2. Add a `record*()` method to `CaveDataLogger`
3. Add the field to `flush()` log format
4. Update the regex in `parse_cave_logs.py`
