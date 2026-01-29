package wftech.caveoverhaul.carvertypes;

import wftech.caveoverhaul.Config;
import wftech.caveoverhaul.fastnoise.FastNoiseLite;
import wftech.caveoverhaul.utils.FloatPos;
import wftech.caveoverhaul.utils.Settings;

public class NCDynamicLayer {

    private static final int MAX_CAVE_SIZE_Y = Settings.MAX_CAVE_SIZE_Y;
    private static final float NOISE_THRESHOLD = 0.15f;

    private final int minY;
    private final int maxY;
    private final int yRangeUpper;  // Precomputed upper bound
    private final FastNoiseLite caveStructureNoise;
    private final NCLogic cache;

    public NCDynamicLayer(int minY, int maxY,
                          FastNoiseLite caveSizeNoise,
                          FastNoiseLite caveYNoise,
                          FastNoiseLite caveStructureNoise) {
        this.minY = minY;
        this.maxY = maxY;
        this.yRangeUpper = maxY + MAX_CAVE_SIZE_Y;
        this.caveStructureNoise = caveStructureNoise;
        this.cache = new NCLogic(minY, maxY, caveYNoise, caveSizeNoise);
    }

    public boolean isInYRange(int y) {
        return y >= minY && y <= yRangeUpper;
    }

    public boolean shouldCarve(int x, int y, int z) {
        int caveHeight = (int) cache.getCachedCaveHeight(x, y, z);
        if (caveHeight <= 0) {
            return false;
        }

        int caveY = (int) cache.getCachedYLevel(x, y, z);
        if (y < caveY || y > caveY + caveHeight) {
            return false;
        }

        float verticalStretch = Config.getFloatSetting(Config.KEY_CAVE_VERTICAL_STRETCH);
        float noiseFound = getWarpedNoise(x, y * verticalStretch, z);
        return noiseFound > NOISE_THRESHOLD;
    }

    private float getWarpedNoise(float x, float y, float z) {
        FloatPos warped = NoisetypeDomainWarp.getWarpedPosition(x, y, z);
        return caveStructureNoise.GetNoise(warped.x(), warped.y(), warped.z());
    }
}