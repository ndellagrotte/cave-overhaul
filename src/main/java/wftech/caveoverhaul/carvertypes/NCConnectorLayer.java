package wftech.caveoverhaul.carvertypes;

import wftech.caveoverhaul.Config;
import wftech.caveoverhaul.fastnoise.FastNoiseLite;
import wftech.caveoverhaul.utils.FloatPos;

public class NCConnectorLayer {

    private static final int HALF_HEIGHT = 6;
    private static final float PLACEMENT_THRESHOLD = 0.55f;
    private static final float STRUCTURE_THRESHOLD = 0.15f;

    private final int boundaryY;
    private final int yLower;
    private final int yUpper;
    private final FastNoiseLite placementNoise;
    private final FastNoiseLite structuralNoise;

    public NCConnectorLayer(int boundaryY,
                            FastNoiseLite placementNoise,
                            FastNoiseLite structuralNoise) {
        this.boundaryY = boundaryY;
        this.yLower = boundaryY - HALF_HEIGHT;
        this.yUpper = boundaryY + HALF_HEIGHT;
        this.placementNoise = placementNoise;
        this.structuralNoise = structuralNoise;
    }

    public boolean isInYRange(int y) {
        return y >= yLower && y <= yUpper;
    }

    public boolean shouldCarve(int x, int y, int z) {
        if (!isInYRange(y)) {
            return false;
        }

        if (placementNoise.GetNoise((float) x, (float) z) <= PLACEMENT_THRESHOLD) {
            return false;
        }

        // Vertical fade: raise threshold near edges so tunnels taper smoothly
        int distFromCenter = Math.abs(y - boundaryY);
        float fadeFactor = (float) distFromCenter / HALF_HEIGHT; // 0 at center, 1 at edge
        float adjustedThreshold = STRUCTURE_THRESHOLD + (fadeFactor * fadeFactor * 0.25f);

        float verticalStretch = Config.getFloatSetting(Config.KEY_CAVE_VERTICAL_STRETCH);
        float noiseFound = getWarpedNoise(x, y * verticalStretch, z);
        return noiseFound > adjustedThreshold;
    }

    private float getWarpedNoise(float x, float y, float z) {
        FloatPos warped = NoisetypeDomainWarp.getWarpedPosition(x, y, z);
        return structuralNoise.GetNoise(warped.x(), warped.y(), warped.z());
    }
}
