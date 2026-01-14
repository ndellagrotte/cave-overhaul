package wftech.caveoverhaul.carvertypes.rivers;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import wftech.caveoverhaul.Config;
import wftech.caveoverhaul.fastnoise.FastNoiseLite;
import wftech.caveoverhaul.fastnoise.FastNoiseLite.FractalType;
import wftech.caveoverhaul.fastnoise.FastNoiseLite.NoiseType;
import wftech.caveoverhaul.utils.FabricUtils;
import wftech.caveoverhaul.utils.Globals;
import wftech.caveoverhaul.utils.NoiseChunkMixinUtils;
import wftech.caveoverhaul.utils.Settings;

//NUR stands for Noise Underground River
public class NURDynamicLayer {

    public static int MAX_CAVE_SIZE_Y = Settings.MAX_CAVE_SIZE_Y;
    public static float NOISE_CUTOFF_RIVER = 0.92f;
    public static float NOISE_CUTOFF_RIVER_NON_WARPED = 0.75f;
    public int seedOffset;
    private final int min_y;
    private final Block blockType;
    private FastNoiseLite domainWarp = null;
    private NURLogic cache = null;

    //public static NoiseUndergroundRiver INSTANCE = new NURDynamicLayer();
    public NURDynamicLayer(Block blockType, int min_y, int seedOffset) {
        super();
        this.blockType = blockType;
        this.min_y = min_y;
        this.seedOffset = seedOffset;

        this.cache = new NURLogic(genNoiseIsLiquid(), genShouldCarveNoise(), genNoiseYLevel());
    }

    /*
     * Edit code below
     *
     * Layer 1 = -64 to -48
     * 2 = -48 to -32 * Lava x1
     * 3 = -32 to -16 * Mixed
     * -16 to 0 * Water x2
     * 0 to 16 * Water x1
     * 16 to 32 * Water x1
     * 32 to 48 * Water x1
     * 48 to 64
     */

    protected int getCaveY(float noiseValue) {
        //40 is the midpoint
        float min = this.min_y;
        float max = (this.min_y) + 8; //3

        if (this.blockType == Blocks.WATER) {
            if(Config.getBoolSetting(Config.KEY_WATER_RIVER_FLAT)){
                return (int) min;
            }
        } else if (this.blockType == Blocks.LAVA) {
            if(Config.getBoolSetting(Config.KEY_LAVA_RIVER_FLAT)){
                return (int) min;
            }
        }

        float diffSize = max - min;
        return (int) (noiseValue * (diffSize)) + (int) min;
    }

    public Block getLiquidType() {
        return this.blockType;
    }

    //Lava by default (in a mixed set) -OR- <entry>2 -> > 0f. Else, < 0f.
    //I think this is malfunctioning, disabling for now
    /*
    protected boolean isOutOfBounds(int x, int z) {
        float shouldCarveNoise = this.getShouldCarveNoise(x, z);
        return shouldCarveNoise > 0f;
    }

     */

    protected FastNoiseLite genNoiseYLevel() {
        FastNoiseLite tnoise = new FastNoiseLite();
        tnoise.SetSeed((int) FabricUtils.server.getWorldData().worldGenOptions().seed() + seedOffset);
        tnoise.SetNoiseType(NoiseType.OpenSimplex2); //SimplexFractal
        tnoise.SetFrequency(0.002f);

        return tnoise;
    }

    protected FastNoiseLite genShouldCarveNoise() {

        FastNoiseLite tnoise = new FastNoiseLite();
        tnoise.SetSeed((int) FabricUtils.server.getWorldData().worldGenOptions().seed() + seedOffset + 1);
        tnoise.SetNoiseType(NoiseType.OpenSimplex2);
        tnoise.SetFrequency(0.0015f);

        return tnoise;
    }

    protected void initDomainWarp() {

        FastNoiseLite tnoise = new FastNoiseLite();
        tnoise.SetSeed((int) FabricUtils.server.getWorldData().worldGenOptions().seed());
        tnoise.SetNoiseType(NoiseType.OpenSimplex2);
        tnoise.SetFrequency(0.025f);
        tnoise.SetFractalLacunarity(1.1f);
        tnoise.SetFractalGain(1.6f);
        domainWarp = tnoise;
    }


    /*
    It's for the out of bounds test. I think it's not working properly. Disabling for now.
     */
    /*
    protected float getShouldCarveNoise(int x, int z) {
        return this.cache.getShouldCarveNoise(x, z);
    }
     */




    public boolean enableRiver() {
        Block preferredBlock = this.getLiquidType();
        if(preferredBlock == Blocks.LAVA && !Config.getBoolSetting(Config.KEY_LAVA_RIVER_ENABLE)) {
            return true;
        } else return preferredBlock == Blocks.WATER && !Config.getBoolSetting(Config.KEY_WATER_RIVER_ENABLE);
    }

    public float getNoise3D(int xPos, int yPos, int zPos) {
        return this.getCaveDetailsNoise(xPos, yPos, zPos);
    }
    private FastNoiseLite genNoiseIsLiquid() {
        FastNoiseLite tnoise = new FastNoiseLite();
        tnoise.SetSeed((int) FabricUtils.server.getWorldData().worldGenOptions().seed() + seedOffset + 2);
        tnoise.SetNoiseType(NoiseType.OpenSimplex2); //SimplexFractal
        tnoise.SetFrequency(0.003f); //CHANGED was 0.003
        tnoise.SetFractalType(FractalType.Ridged);
        tnoise.SetFractalOctaves(1);

        return tnoise;
    }

    public float getCaveDetailsNoise(int x, int y, int z) {
        if (domainWarp != null) {
            FastNoiseLite.Vector3 coords = new FastNoiseLite.Vector3(x, y, z);
            domainWarp.DomainWarp(coords);
            return genNoiseIsLiquid().GetNoise(coords.x, coords.y, coords.z);
        }
        return genNoiseIsLiquid().GetNoise(x, y, z);
    }

    protected float getWarpedNoise(int xPos, int yPos, int zPos) {

        if (domainWarp == null) {
            initDomainWarp();
        }

        float warpX = xPos;
        float warpY = yPos;
        float warpZ = zPos;
        for (int i = 0; i < 2; i++) {
            //CHANGED
            //Not applying an offset to warpX is intentional.
            //The location for warpX can be anywhere, so it's ok that there's no offset. It should have no skew change or anything.
            warpX += domainWarp.GetNoise(warpX + 20, warpY, warpZ + 20) * 2f;
            warpY += domainWarp.GetNoise(warpX, warpY + 20, warpZ) * 2f;
            warpZ += domainWarp.GetNoise(warpX - 20, warpY, warpZ - 20) * 2f;
        }

        return this.getNoise3D((int) warpX, (int) warpY, (int) warpZ);
    }

    public boolean isLiquid(int x, int y, int z) {

        if (enableRiver()) {
            return false;
        }

        BlockPos bPos = new BlockPos(x, y, z);

        if (this.getNoise3D(bPos.getX(), bPos.getY(), bPos.getZ()) < NOISE_CUTOFF_RIVER_NON_WARPED) {
            return false;
        }

        float yLevelNoise = this.getCaveYNoise(x, y, z);
        int caveY = this.getCaveY(yLevelNoise);
        if (caveY != y) {
            return false;
        }

        float noise = this.getWarpedNoise(bPos.getX(), bPos.getY(), bPos.getZ());
        return noise > NOISE_CUTOFF_RIVER;
    }

    public boolean isAir(int x, int y, int z) {

        if (enableRiver()) {
            return false;
        }

        BlockPos bPos = new BlockPos(x, y, z);

        if (this.getNoise3D(bPos.getX(), bPos.getY(), bPos.getZ()) < NOISE_CUTOFF_RIVER_NON_WARPED) {
            return false;
        }

        float noise = this.getWarpedNoise(bPos.getX(), bPos.getY(), bPos.getZ());
        if (noise <= NOISE_CUTOFF_RIVER) {
            return false;
        }

        if (y <= (Globals.minY + 9)) {
            return false;
        }

        if (isLiquid(x, y - 1, z)) {
            return true;
        } else if (isLiquid(x, y - 2, z)) {
            return true;
        }

        //Carve roof
        float noiseDelta = noise - NOISE_CUTOFF_RIVER;
        int noiseCutoffCeiling = (int) (noiseDelta * 100);
        noiseCutoffCeiling /= 2;
        for (int i = 1; i < noiseCutoffCeiling; i++) {
            if (isLiquid(x, y - (2 + i), z)) {
                return true;
            }
        }

        return false;
    }

    public boolean isInYRange(int y) {
        //16 is arbitrary, should be more than enough
        //for any cavernous tops + an extra stone as a boundary
        return y < (this.min_y - 2) || y > (this.min_y + 16);
    }

    //checkIfInRiver = true for the noise mixin, false = if it's called by waterfall function
    public boolean isBoundary(int x, int y, int z) {

        if (enableRiver()) {
            return false;
        }

        //Do not place stone in the bottom-of-world lava area
        if (y <= (Globals.minY + 8)) {
            return false;
        }

        BlockPos bPos = new BlockPos(x, y, z);

        if (this.getNoise3D(bPos.getX(), bPos.getY(), bPos.getZ()) < NOISE_CUTOFF_RIVER_NON_WARPED) {
            return false;
        }

        float noise = this.getWarpedNoise(bPos.getX(), bPos.getY(), bPos.getZ());
        boolean shouldCarveRiver = noise > NOISE_CUTOFF_RIVER;
        if (shouldCarveRiver) {
            return false;
        }

        if (NoiseChunkMixinUtils.getRiverLayer(128, x + 1, y, z) != null) {
            return true;
        } else if (NoiseChunkMixinUtils.getRiverLayer(128, x - 1, y, z) != null) {
            return true;
        } else if (NoiseChunkMixinUtils.getRiverLayer(128, x, y, z + 1) != null) {
            return true;
        } else return NoiseChunkMixinUtils.getRiverLayer(128, x, y, z - 1) != null;
    }

    //checkIfInRiver = true for the noise mixin, false = if it's called by waterfall function
    public boolean isBelowWaterfallSupport(int x, int y, int z) {

        if (enableRiver()) {
            return false;
        }

        //Do not place stone in the bottom-of-world lava area
        if (y <= (Globals.minY + 8)) {
            return false;
        }

        BlockPos bPos = new BlockPos(x, y, z);

        if (this.getNoise3D(bPos.getX(), bPos.getY(), bPos.getZ()) < NOISE_CUTOFF_RIVER_NON_WARPED) {
            return false;
        }

        this.getWarpedNoise(bPos.getX(), bPos.getY(), bPos.getZ());

        float yLevelNoise_o = this.getCaveYNoise(x, y, z);
        int y_o = this.getCaveY(yLevelNoise_o);

        BlockPos.MutableBlockPos mbPos = new BlockPos.MutableBlockPos();
        mbPos.set(bPos.getX() + 1, bPos.getY(), bPos.getZ());
        if (this.getWarpedNoise(mbPos.getX(), mbPos.getY(), mbPos.getZ()) > NOISE_CUTOFF_RIVER) {
            float yLevelNoise1 = this.getCaveYNoise(mbPos.getX(), mbPos.getY(), mbPos.getZ());
            int neighborY = this.getCaveY(yLevelNoise1);
            return y == neighborY && (y_o != y) && neighborY < y_o;
        }
        mbPos.set(bPos.getX() - 1, bPos.getY(), bPos.getZ());
        if (this.getWarpedNoise(mbPos.getX(), mbPos.getY(), mbPos.getZ()) > NOISE_CUTOFF_RIVER) {
            float yLevelNoise1 = this.getCaveYNoise(mbPos.getX(), mbPos.getY(), mbPos.getZ());
            int neighborY = this.getCaveY(yLevelNoise1);
            return y == neighborY && y_o != y && neighborY < y_o;
        }
        mbPos.set(bPos.getX(), bPos.getY(), bPos.getZ() + 1);
        if (this.getWarpedNoise(mbPos.getX(), mbPos.getY(), mbPos.getZ()) > NOISE_CUTOFF_RIVER) {
            float yLevelNoise1 = this.getCaveYNoise(mbPos.getX(), mbPos.getY(), mbPos.getZ());
            int neighborY = this.getCaveY(yLevelNoise1);
            return y == neighborY && y_o != y && neighborY < y_o;
        }
        mbPos.set(bPos.getX(), bPos.getY(), bPos.getZ() - 1);
        if (this.getWarpedNoise(mbPos.getX(), mbPos.getY(), mbPos.getZ()) > NOISE_CUTOFF_RIVER) {
            float yLevelNoise1 = this.getCaveYNoise(mbPos.getX(), mbPos.getY(), mbPos.getZ());
            int neighborY = this.getCaveY(yLevelNoise1);
            return y == neighborY && y_o != y && neighborY < y_o;
        }

        return false;
    }

    private float getCaveYNoise(int x, int y, int z) {
        return this.cache.getCaveYNoise(x, y, z);    }

    public boolean isBelowRiverSupport(int x, int y, int z) {

        if (enableRiver()) {
            return false;
        }

        //Do not place stone in the bottom-of-world lava area
        if (y <= (Globals.minY + 8)) {
            return false;
        }

        if (this.isLiquid(x, y + 1, z)) {
            return true;
        } else return this.isLiquid(x, y + 2, z);
    }
}