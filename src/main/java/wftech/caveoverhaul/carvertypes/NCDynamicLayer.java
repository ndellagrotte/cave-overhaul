package wftech.caveoverhaul.carvertypes;

import net.minecraft.core.BlockPos;
import wftech.caveoverhaul.fastnoise.FastNoiseLite;
import wftech.caveoverhaul.utils.FloatPos;
import wftech.caveoverhaul.utils.Settings;

//NC stands for NoiseCave
public class NCDynamicLayer {

    public static float norm(float f) {
        return (1f + f) / 2f;
    }

    public static int MAX_CAVE_SIZE_Y = Settings.MAX_CAVE_SIZE_Y;

    public FastNoiseLite caveSizeNoise = null;
    public FastNoiseLite caveYNoise = null;
    public FastNoiseLite caveStructureNoise = null;
    private NCLogic cache = null;

    /*
     * -64 to 0, doubling up to expand the amount of fun caves near the bottom
     */
    public float minY = -64;
    public float maxY = 0;

    public NCDynamicLayer(int minY, int maxY, FastNoiseLite caveThicknessMap, FastNoiseLite caveYNoise, FastNoiseLite caveStructureNoise) {
        this.minY = minY;
        this.maxY = maxY;

        this.caveSizeNoise = caveThicknessMap;
        this.caveYNoise = caveYNoise;
        this.caveStructureNoise = caveStructureNoise;

        this.cache = new NCLogic(minY, maxY, caveYNoise, caveThicknessMap);
    }

    public boolean isInYRange(int y) {
        return y <= (maxY + MAX_CAVE_SIZE_Y) && y >= minY;
    }

    //Used
    public float getWarpedNoise(float x, float y, float z) {
        FloatPos fpos = NoisetypeDomainWarp.getWarpedPosition(x, y, z);
        return caveStructureNoise.GetNoise(fpos.x, fpos.y, fpos.z);
    }

    public boolean shouldCarve(float x, float y, float z) {
        /*
         * Put in subclass (copy and paste)
         */
        BlockPos.MutableBlockPos mPos = new BlockPos.MutableBlockPos();
        int earlyXPos = (int) x;
        int earlyYPos = (int) y;
        int earlyZPos = (int) z;

        /*
        Eyelets are so we phase in and out of where caverns can be drawn, then the actual check occurs.
        Think about it like sketching out activity zones. If we're in an activity zone, we then check the activity
        itself. Else, do nothing.
         */
        int caveHeight = (int) this.cache.getCachedCaveHeight(earlyXPos, earlyYPos, earlyZPos);
        if (caveHeight <= 0) {
            return false;
        }

        int caveY = (int) this.cache.getCachedYLevel(earlyXPos, earlyYPos, earlyZPos);

        return shouldCarveBasedOnHeight(x, y, z, caveHeight, caveY);
    }

    // REMOVE
    public static float ySquish(float noiseHeight) {
        float caveOffset = ((float) MAX_CAVE_SIZE_Y) / 2f;
        float k = 2f;
        int dist = 2 + 1;
        if (noiseHeight > caveOffset + dist || noiseHeight < caveOffset - dist) {
            return 0f;
        }

        return 1f - (float) (1f / (1f + Math.exp(k * (-noiseHeight + (caveOffset)))));
    }

    public float getNoiseThreshold() {
        return 0.15f;
    }

    public boolean shouldCarveBasedOnHeight(float x, float y, float z, int caveHeight, int caveY) {
        int yPos = (int) y;

        if (yPos < caveY || yPos > caveY + caveHeight) {
            return false;
        }

        int xPos = (int) x;
        int zPos = (int) z;

        float noiseFound = getWarpedNoise(xPos, yPos * 2, zPos);

        return noiseFound > getNoiseThreshold();
    }
}