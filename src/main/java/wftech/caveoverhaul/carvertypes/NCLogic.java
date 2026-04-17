package wftech.caveoverhaul.carvertypes;

import wftech.caveoverhaul.fastnoise.FastNoiseLite;
import wftech.caveoverhaul.utils.FloatPos;
import wftech.caveoverhaul.utils.Settings;

//NC stands for NoiseCave
public class NCLogic {

    private final float minY;
    private final float maxY;
    private final int layerCenterY;
    private final FastNoiseLite caveYNoise;
    private final FastNoiseLite caveSizeNoise;

    public static int MAX_CAVE_SIZE_Y = Settings.MAX_CAVE_SIZE_Y;

    public NCLogic(float minY, float maxY, FastNoiseLite caveYNoise, FastNoiseLite caveSizeNoise) {
        this.minY = minY;
        this.maxY = maxY;
        this.layerCenterY = Math.round((minY + maxY) / 2f);
        this.caveYNoise = caveYNoise;
        this.caveSizeNoise = caveSizeNoise;
    }

    private int getCaveY(float noiseValue) {
        return (int) (((this.maxY - this.minY) * noiseValue) + this.minY);
    }

    int calcYLevel(int x, int z) {
        float rawNoiseY = getCaveYNoise(x, z);
        rawNoiseY = (rawNoiseY + 1f) / 2f;
        rawNoiseY = Math.max(0, rawNoiseY);
        rawNoiseY = Math.min(1, rawNoiseY);

        return getCaveY(rawNoiseY);
    }

    // Samples at layerCenterY so each layer's slab has one center-Y per XZ column.
    // The domain warp is intentionally Y-dependent (lower Y = stronger warp), so the
    // per-layer fixed Y preserves inter-layer variation while killing within-layer drift.
    private float getCaveYNoise(int x, int z) {
        FloatPos warped = NoisetypeDomainWarp.getWarpedPosition(x, layerCenterY, z);
        return this.caveYNoise.GetNoise(warped.x(), warped.y(), warped.z());
    }

    private float getCaveThicknessNoise(int x, int z) {
        FloatPos warped = NoisetypeDomainWarp.getWarpedPosition(x, layerCenterY, z);
        return this.caveSizeNoise.GetNoise(warped.x(), warped.y(), warped.z());
    }

    int calcHeight(int x, int z) {
        float caveHeightNoise = getCaveThicknessNoise(x, z);
        caveHeightNoise = ((1f + caveHeightNoise) / 2f) * (float) MAX_CAVE_SIZE_Y;
        float caveHeightNoiseSquished = ySquish(caveHeightNoise);
        return (int) (caveHeightNoiseSquished * MAX_CAVE_SIZE_Y);
    }

    public static float ySquish(float noiseHeight) {
        float center = Settings.CAVE_HEIGHT_SIGMOID_CENTER;
        float k = Settings.CAVE_HEIGHT_SIGMOID_STEEPNESS;
        return (float) (1.0 / (1.0 + Math.exp(k * (noiseHeight - center))));
    }
}