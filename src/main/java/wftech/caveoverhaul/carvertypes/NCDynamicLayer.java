package wftech.caveoverhaul.carvertypes;

import java.util.Arrays;

import wftech.caveoverhaul.fastnoise.FastNoiseLite;
import wftech.caveoverhaul.utils.FloatPos;
import wftech.caveoverhaul.utils.Settings;

public class NCDynamicLayer {

    private static final int MAX_CAVE_SIZE_Y = Settings.MAX_CAVE_SIZE_Y;
    private static final float NOISE_THRESHOLD = Settings.NOISE_CAVE_STRUCTURE_THRESHOLD;

    private final int minY;
    private final int yRangeUpper;  // Precomputed upper bound
    private final FastNoiseLite caveStructureNoise;
    private final NCLogic logic;

    // Per-chunk XZ-column cache for the noise-cave slab (center-Y + thickness).
    // Mirrors the ThreadLocal pattern in NURDynamicLayer. Same ThreadLocal lifetime
    // caveat applies: on world reload, stale entries in worker ThreadLocalMaps leak
    // until the thread exits — small and matches rivers, not fixing here.
    private final ThreadLocal<ColumnCache> columnCache =
            ThreadLocal.withInitial(ColumnCache::new);

    public NCDynamicLayer(int minY, int maxY,
                          FastNoiseLite caveSizeNoise,
                          FastNoiseLite caveYNoise,
                          FastNoiseLite caveStructureNoise) {
        this.minY = minY;
        this.yRangeUpper = maxY + MAX_CAVE_SIZE_Y;
        this.caveStructureNoise = caveStructureNoise;
        this.logic = new NCLogic(minY, maxY, caveYNoise, caveSizeNoise);
    }

    public boolean isInYRange(int y) {
        return y >= minY && y <= yRangeUpper;
    }

    public boolean shouldCarve(int x, int y, int z) {
        ColumnCache cache = columnCache.get();
        cache.ensureChunk(x >> 4, z >> 4);
        int idx = cache.index(x & 15, z & 15);

        int caveY;
        int caveHeight;
        int cachedYLevel = cache.yLevels[idx];
        if (cachedYLevel == Integer.MIN_VALUE) {
            caveHeight = logic.calcHeight(x, z);
            if (caveHeight <= 0) {
                // Cache the miss so later voxel checks in the same column short-circuit.
                // yLevel=0 collides with a legit yLevel=0 column only when height>0,
                // which is gated separately below — so the collision is harmless.
                cache.yLevels[idx] = 0;
                cache.heights[idx] = 0;
                return false;
            }
            caveY = logic.calcYLevel(x, z);
            cache.yLevels[idx] = caveY;
            cache.heights[idx] = caveHeight;
        } else {
            caveHeight = cache.heights[idx];
            if (caveHeight <= 0) {
                return false;
            }
            caveY = cachedYLevel;
        }

        if (y < caveY || y > caveY + caveHeight) {
            return false;
        }

        float noiseFound = getWarpedNoise(x, y, z);
        return noiseFound > NOISE_THRESHOLD;
    }

    private float getWarpedNoise(float x, float y, float z) {
        FloatPos warped = NoisetypeDomainWarp.getWarpedPosition(x, y, z);
        return caveStructureNoise.GetNoise(warped.x(), warped.y(), warped.z());
    }

    private static final class ColumnCache {
        int chunkX = Integer.MIN_VALUE;
        int chunkZ = Integer.MIN_VALUE;
        final int[] yLevels = new int[256];
        final int[] heights = new int[256];

        ColumnCache() {
            Arrays.fill(yLevels, Integer.MIN_VALUE);
        }

        void ensureChunk(int cx, int cz) {
            if (cx != chunkX || cz != chunkZ) {
                chunkX = cx;
                chunkZ = cz;
                Arrays.fill(yLevels, Integer.MIN_VALUE);
            }
        }

        int index(int localX, int localZ) {
            return (localX << 4) | localZ;
        }
    }
}