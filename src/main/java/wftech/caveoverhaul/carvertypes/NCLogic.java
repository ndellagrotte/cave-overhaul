package wftech.caveoverhaul.carvertypes;

import wftech.caveoverhaul.fastnoise.FastNoiseLite;
import wftech.caveoverhaul.fastnoise.FastNoiseLite.Vector3;
import wftech.caveoverhaul.utils.Settings;

//NC stands for NoiseCave
public class NCLogic {

    private final FastNoiseLite domainWarp;
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

        // 3D Domain warp setup
        FastNoiseLite warp = new FastNoiseLite(12345);
        warp.SetDomainWarpType(FastNoiseLite.DomainWarpType.OpenSimplex2);
        warp.SetDomainWarpAmp(50.0f);
        warp.SetFrequency(0.01f);
        this.domainWarp = warp;
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
        if (domainWarp != null) {
            Vector3 coords = new Vector3(x, y, z);
            domainWarp.DomainWarp(coords);
            return this.caveYNoise.GetNoise(coords.x, coords.y, coords.z);
        }
        return this.caveYNoise.GetNoise(x, y, z);
    }

    private float getCaveThicknessNoise(int x, int y, int z) {
        if (domainWarp != null) {
            Vector3 coords = new Vector3(x, y, z);
            domainWarp.DomainWarp(coords);
            return this.caveSizeNoise.GetNoise(coords.x, coords.y, coords.z);
        }
        return this.caveSizeNoise.GetNoise(x, y, z);
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