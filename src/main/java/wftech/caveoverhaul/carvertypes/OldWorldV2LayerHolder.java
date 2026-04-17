package wftech.caveoverhaul.carvertypes;

import wftech.caveoverhaul.Config;
import wftech.caveoverhaul.fastnoise.FastNoiseLite;
import wftech.caveoverhaul.utils.FabricUtils;
import wftech.caveoverhaul.utils.Globals;
import wftech.caveoverhaul.utils.Settings;

/*
 * Old World Caves v2 — "slime-mold" trace + dilation.
 *
 * Two ridged OpenSimplex2 fields are sampled with domain-warped coordinates;
 * their intersection (both > RIDGE_THRESHOLD) traces a sparse 1-voxel lattice
 * of curves through 3D space. Each block then checks itself plus an
 * axis-aligned cross of face neighbors at a fixed radius — if any of those
 * points is on the trace, this block gets carved. The dilation step is what
 * turns the 1-voxel centerline into a uniform-width tunnel; radius 1 yields
 * roughly 3-wide tunnels regardless of the crossing angle of the two fields.
 *
 * Why dilation (not a looser threshold): ridged-field intersection width
 * depends on crossing angle — shallow crossings give wider traces than steep
 * ones. A single warp plus a fixed dilation radius produces uniform-width
 * tunnels, which is the property the user wants.
 *
 * Domain warp breaks up the regular lattice structure of the raw ridged
 * intersection, so traces meander organically rather than following the
 * OpenSimplex grid axes.
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
    private final FastNoiseLite warp;
    private final int minY;

    private OldWorldV2LayerHolder() {
        int seed = Long.hashCode(FabricUtils.server.getWorldGenSettings().options().seed());
        this.minY = Globals.getMinY();
        this.noise1 = buildRidged(seed);
        // Golden-ratio seed offsets decorrelate the three fields.
        this.noise2 = buildRidged(seed ^ 0x9E3779B1);
        this.warp   = buildWarp(seed ^ 0x85EBCA6B);
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

    private static FastNoiseLite buildWarp(int seed) {
        FastNoiseLite n = new FastNoiseLite();
        n.SetSeed(seed);
        n.SetNoiseType(FastNoiseLite.NoiseType.OpenSimplex2);
        n.SetRotationType3D(FastNoiseLite.RotationType3D.ImproveXZPlanes);
        n.SetFractalType(FastNoiseLite.FractalType.None);
        n.SetFrequency(Settings.OLD_WORLD_V2_WARP_FREQUENCY);
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
            // Soft taper near the top cap — raise the threshold as y approaches
            // topCap so traces thin out rather than slicing off at a hard ceiling.
            float t = (y - (topCap - fadeZone)) / (float) fadeZone;
            threshold += t * Settings.OLD_WORLD_V2_TOP_FADE_STRENGTH;
        }

        // One warp per block, shared across the dilation neighborhood so the
        // sampled points are contiguous in noise-space (no speckle artifacts).
        float strength = Settings.OLD_WORLD_V2_WARP_STRENGTH;
        float fx = (float) x, fy = (float) y, fz = (float) z;
        float wx = fx + warp.GetNoise(fx,         fy,         fz)         * strength;
        float wy = fy + warp.GetNoise(fx + 31.1f, fy + 31.1f, fz + 31.1f) * strength;
        float wz = fz + warp.GetNoise(fx - 31.1f, fy - 31.1f, fz - 31.1f) * strength;

        float yScale = Settings.OLD_WORLD_V2_Y_SCALE;

        // Axis-aligned cross dilation: center + ±r on each axis. Radius 1 =>
        // 7 samples, tunnels ~3 blocks across the thick axes.
        if (isTraceAt(wx, wy, wz, yScale, threshold)) return true;
        int radius = Settings.OLD_WORLD_V2_DILATE_RADIUS;
        for (int r = 1; r <= radius; r++) {
            if (isTraceAt(wx + r, wy, wz, yScale, threshold)) return true;
            if (isTraceAt(wx - r, wy, wz, yScale, threshold)) return true;
            if (isTraceAt(wx, wy + r, wz, yScale, threshold)) return true;
            if (isTraceAt(wx, wy - r, wz, yScale, threshold)) return true;
            if (isTraceAt(wx, wy, wz + r, yScale, threshold)) return true;
            if (isTraceAt(wx, wy, wz - r, yScale, threshold)) return true;
        }
        return false;
    }

    private boolean isTraceAt(float wx, float wy, float wz, float yScale, float threshold) {
        float sy = wy * yScale;
        float r1 = noise1.GetNoise(wx, sy, wz);
        if (r1 <= threshold) return false;
        float r2 = noise2.GetNoise(wx, sy, wz);
        return r2 > threshold;
    }
}
