package wftech.caveoverhaul.carvertypes;

import wftech.caveoverhaul.Config;
import wftech.caveoverhaul.fastnoise.FastNoiseLite;
import wftech.caveoverhaul.utils.FabricUtils;
import wftech.caveoverhaul.utils.Globals;
import wftech.caveoverhaul.utils.Settings;

/*
 * Old World Caves v2 — noise-driven labyrinthine tunnel system.
 *
 * Replaces the legacy carver-based OldWorldCarverv12 when KEY_DEBUG_OLD_WORLD_CAVES_V2
 * is enabled. The two systems share no code and are not designed to run simultaneously.
 *
 * Geometry: two ridged OpenSimplex2 fields trace thin 2D surfaces in 3D space; their
 * intersection (both > threshold) traces 1D curves — tunnels. Distribution is uniform
 * by construction since both fields are translation-invariant. Sampling Y at a stretched
 * coordinate biases tunnel orientation toward horizontal for playable slopes.
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

    private final FastNoiseLite noise1;
    private final FastNoiseLite noise2;
    private final int minY;

    private OldWorldV2LayerHolder() {
        int seed = Long.hashCode(FabricUtils.server.getWorldGenSettings().options().seed());
        this.minY = Globals.getMinY();
        this.noise1 = buildRidged(seed);
        // Golden-ratio seed offset decorrelates the two fields.
        this.noise2 = buildRidged(seed ^ 0x9E3779B1);
    }

    private static FastNoiseLite buildRidged(int seed) {
        FastNoiseLite n = new FastNoiseLite();
        n.SetSeed(seed);
        n.SetNoiseType(FastNoiseLite.NoiseType.OpenSimplex2);
        n.SetRotationType3D(FastNoiseLite.RotationType3D.ImproveXZPlanes);
        n.SetFractalType(FastNoiseLite.FractalType.Ridged);
        n.SetFrequency(Settings.OLD_WORLD_V2_FREQUENCY);
        n.SetFractalOctaves(Settings.OLD_WORLD_V2_OCTAVES);
        n.SetFractalLacunarity(Settings.OLD_WORLD_V2_LACUNARITY);
        n.SetFractalGain(Settings.OLD_WORLD_V2_GAIN);
        return n;
    }

    public boolean shouldCarve(int x, int y, int z) {
        if (!Config.getBoolSetting(Config.KEY_DEBUG_OLD_WORLD_CAVES_V2)) {
            return false;
        }

        int topCap = Settings.OLD_WORLD_V2_TOP_CAP;
        int bottomCap = minY + Settings.OLD_WORLD_V2_BEDROCK_BUFFER;
        if (y > topCap || y < bottomCap) {
            return false;
        }

        float threshold = Settings.OLD_WORLD_V2_RIDGE_THRESHOLD;
        int fadeZone = Settings.OLD_WORLD_V2_TOP_FADE_ZONE;
        if (y > topCap - fadeZone) {
            // Soft taper near the top cap: raise the threshold as y approaches topCap
            // so tunnels thin out rather than slicing off at a visible ceiling.
            float t = (y - (topCap - fadeZone)) / (float) fadeZone;
            threshold += t * Settings.OLD_WORLD_V2_TOP_FADE_STRENGTH;
        }

        float sy = y * Settings.OLD_WORLD_V2_Y_SCALE;
        float r1 = noise1.GetNoise((float) x, sy, (float) z);
        if (r1 <= threshold) {
            return false;
        }
        float r2 = noise2.GetNoise((float) x, sy, (float) z);
        return r2 > threshold;
    }
}
