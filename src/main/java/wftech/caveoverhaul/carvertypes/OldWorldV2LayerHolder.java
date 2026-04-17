package wftech.caveoverhaul.carvertypes;

import wftech.caveoverhaul.fastnoise.FastNoiseLite;
import wftech.caveoverhaul.utils.FabricUtils;
import wftech.caveoverhaul.utils.Settings;

/*
 * Old World Caves v2 — per-chunk noise oracle.
 *
 * Unlike v1 (pure per-chunk random roll, which clusters), v2 samples a single
 * low-frequency OpenSimplex2 field at chunk resolution to decide which chunks
 * are eligible to spawn a cave cluster. This produces "soft zones" — broad
 * smoothly-varying regions that favor caves, separated by broad regions that
 * don't — rather than either uniform-random scatter (too even, no regional
 * variation) or clumped hot-spots.
 *
 * Actual tunnel geometry is produced by OldWorldV2Carver using v1-style
 * node-walk logic; this class only answers "should a cluster start here?".
 */
public class OldWorldV2LayerHolder {

    private static volatile OldWorldV2LayerHolder INSTANCE = null;
    private static final Object LOCK = new Object();

    public static OldWorldV2LayerHolder getInstance() {
        OldWorldV2LayerHolder instance = INSTANCE;
        if (instance == null) {
            synchronized (LOCK) {
                instance = INSTANCE;
                if (instance == null) {
                    INSTANCE = instance = new OldWorldV2LayerHolder();
                }
            }
        }
        return instance;
    }

    public static void reset() {
        synchronized (LOCK) {
            INSTANCE = null;
        }
    }

    private final FastNoiseLite gateNoise;

    private OldWorldV2LayerHolder() {
        int seed = Long.hashCode(FabricUtils.server.getWorldGenSettings().options().seed());
        this.gateNoise = buildGateNoise(seed);
    }

    private static FastNoiseLite buildGateNoise(int seed) {
        FastNoiseLite n = new FastNoiseLite();
        n.SetSeed(seed);
        n.SetNoiseType(FastNoiseLite.NoiseType.OpenSimplex2);
        // No fractal — a single smooth octave is what we want for broad zones.
        n.SetFractalType(FastNoiseLite.FractalType.None);
        n.SetFrequency(Settings.OLD_WORLD_V2_GATE_FREQUENCY);
        return n;
    }

    /**
     * Returns true if a cave cluster is allowed to spawn with its origin in
     * the given chunk. Sampled at chunk resolution so adjacent chunks vary
     * smoothly; a typical zone spans several chunks.
     *
     * Output of the noise is in roughly [-1, 1]. We pass when it exceeds the
     * threshold: lower threshold → more chunks pass → denser caves overall.
     */
    public boolean shouldSpawnAt(int chunkX, int chunkZ) {
        float v = gateNoise.GetNoise((float) chunkX, (float) chunkZ);
        return v > Settings.OLD_WORLD_V2_GATE_THRESHOLD;
    }
}
