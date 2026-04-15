package wftech.caveoverhaul.carvertypes;

import wftech.caveoverhaul.fastnoise.FastNoiseLite;
import wftech.caveoverhaul.utils.FloatPos;
import wftech.caveoverhaul.utils.Settings;

//NC stands for NoiseCave
public class NCLogic {

    private final float minY;
    private final float maxY;
    private final FastNoiseLite caveYNoise;
    private final FastNoiseLite caveSizeNoise;

    public static int MAX_CAVE_SIZE_Y = Settings.MAX_CAVE_SIZE_Y;

    public NCLogic(float minY, float maxY, FastNoiseLite caveYNoise, FastNoiseLite caveSizeNoise) {
        this.minY = minY;
        this.maxY = maxY;
        this.caveYNoise = caveYNoise;
        this.caveSizeNoise = caveSizeNoise;
    }

    public float getCachedYLevel(int x, int y, int z) {
        return this.calcYLevel(x, y, z);
    }

    public float getCachedCaveHeight(int x, int y, int z) {
        return this.calcHeight(x, y, z);
    }

    private int getCaveY(float noiseValue) {
        return (int) (((this.maxY - this.minY) * noiseValue) + this.minY);
    }

    private float calcYLevel(int x, int y, int z) {
        float rawNoiseY = getCaveYNoise(x, y, z);
        rawNoiseY = (rawNoiseY + 1f) / 2f;
        rawNoiseY = Math.max(0, rawNoiseY);
        rawNoiseY = Math.min(1, rawNoiseY);

        return getCaveY(rawNoiseY);
    }

    private float getCaveYNoise(int x, int y, int z) {
        FloatPos warped = NoisetypeDomainWarp.getWarpedPosition(x, y, z);
        return this.caveYNoise.GetNoise(warped.x(), warped.y(), warped.z());
    }

    private float getCaveThicknessNoise(int x, int y, int z) {
        FloatPos warped = NoisetypeDomainWarp.getWarpedPosition(x, y, z);
        return this.caveSizeNoise.GetNoise(warped.x(), warped.y(), warped.z());
    }

    private float calcHeight(int x, int y, int z) {
        float caveHeightNoise = getCaveThicknessNoise(x, y, z);
        int caveHeight;
        caveHeightNoise = ((1f + caveHeightNoise) / 2f) * (float) MAX_CAVE_SIZE_Y;
        float caveHeightNoiseSquished = ySquish(caveHeightNoise);
        caveHeight = (int) (caveHeightNoiseSquished * MAX_CAVE_SIZE_Y);

        return caveHeight;
    }

    public static float ySquish(float noiseHeight) {
        float caveOffset = (MAX_CAVE_SIZE_Y) / 2f;
        float k = 2f;
        int dist = 2 + 1;
        if (noiseHeight > caveOffset + dist || noiseHeight < caveOffset - dist) {
            return 0f;
        }

        return 1f - (float) (1f / (1f + Math.exp(k * (-noiseHeight + (caveOffset)))));
    }
}