package wftech.caveoverhaul.carvertypes.rivers;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import org.jspecify.annotations.Nullable;
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

    public static float NOISE_CUTOFF_RIVER = 0.92f;
    public static float NOISE_CUTOFF_RIVER_NON_WARPED = 0.75f;
    public int seedOffset;
    private final int min_y;
    private final Block blockType;
    private FastNoiseLite domainWarp = null;
    private NURLogic cache = null;
    private static final int CEILING_BUFFER = 4;
    private static final int FLOOR_VARIANCE_DIVISOR = 2;
    public static int MAX_CAVE_SIZE_Y = Settings.MAX_CAVE_SIZE_Y;

    public NURDynamicLayer(Block blockType, int min_y, int seedOffset) {
        super();
        this.blockType = blockType;
        this.min_y = min_y;
        this.seedOffset = seedOffset;

        this.cache = new NURLogic(genNoiseIsLiquid(), genShouldCarveNoise(), genNoiseYLevel());
    }

    protected int getCaveY(float noiseValue) {
        float min = this.min_y;
        float max = this.min_y + ((float) MAX_CAVE_SIZE_Y / FLOOR_VARIANCE_DIVISOR);

        if (this.blockType == Blocks.WATER) {
            if (Config.getBoolSetting(Config.KEY_WATER_RIVER_FLAT)) {
                return (int) min;
            }
        } else if (this.blockType == Blocks.LAVA) {
            if (Config.getBoolSetting(Config.KEY_LAVA_RIVER_FLAT)) {
                return (int) min;
            }
        }

        float diffSize = max - min;
        return (int) (noiseValue * diffSize) + (int) min;
    }

    public Block getLiquidType() {
        return this.blockType;
    }

    public boolean isOutOfBounds(int x, int y, int z) {
        float shouldCarveNoise = this.cache.getShouldCarveNoise3D(x, y, z);
        return shouldCarveNoise > 0.7f;
    }

    protected FastNoiseLite genNoiseYLevel() {
        FastNoiseLite tnoise = new FastNoiseLite();
        tnoise.SetSeed((int) FabricUtils.server.getWorldData().worldGenOptions().seed() + seedOffset);
        tnoise.SetNoiseType(NoiseType.OpenSimplex2);
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

    public boolean enableRiver() {
        Block preferredBlock = this.getLiquidType();
        if (preferredBlock == Blocks.LAVA && !Config.getBoolSetting(Config.KEY_LAVA_RIVER_ENABLE)) {
            return true;
        } else return preferredBlock == Blocks.WATER && !Config.getBoolSetting(Config.KEY_WATER_RIVER_ENABLE);
    }

    public float getNoise3D(int xPos, int yPos, int zPos) {
        return this.getCaveDetailsNoise(xPos, yPos, zPos);
    }

    private FastNoiseLite genNoiseIsLiquid() {
        FastNoiseLite tnoise = new FastNoiseLite();
        tnoise.SetSeed((int) FabricUtils.server.getWorldData().worldGenOptions().seed() + seedOffset + 2);
        tnoise.SetNoiseType(NoiseType.OpenSimplex2);
        tnoise.SetFrequency(0.003f);
        tnoise.SetFractalType(FractalType.Ridged);
        tnoise.SetFractalOctaves(1);

        return tnoise;
    }

    public float getCaveDetailsNoise(int x, int y, int z) {
        return this.cache.getCaveDetailsNoise3D(x, y, z);
    }

    protected float getWarpedNoise(int xPos, int yPos, int zPos) {
        if (domainWarp == null) {
            initDomainWarp();
        }

        float warpX = xPos;
        float warpY = yPos;
        float warpZ = zPos;
        for (int i = 0; i < 2; i++) {
            warpX += domainWarp.GetNoise(warpX + 20, warpY, warpZ + 20) * 2f;
            warpY += domainWarp.GetNoise(warpX, warpY + 20, warpZ) * 2f;
            warpZ += domainWarp.GetNoise(warpX - 20, warpY, warpZ - 20) * 2f;
        }

        return this.getNoise3D((int) warpX, (int) warpY, (int) warpZ);
    }

    public boolean isLiquid(int x, int y, int z) {
        BlockPos bPos = new BlockPos(x, y, z);

        if (this.getNoise3D(bPos.getX(), bPos.getY(), bPos.getZ()) < NOISE_CUTOFF_RIVER_NON_WARPED) {
            return false;
        }

        int caveY = this.getFixedCaveY();  // Use fixed Y, not noise-based
        if (caveY != y) {
            return false;
        }

        float noise = this.getWarpedNoise(bPos.getX(), bPos.getY(), bPos.getZ());
        return noise > NOISE_CUTOFF_RIVER;
    }

    // New method - always returns flat floor level
    private int getFixedCaveY() {
        return this.min_y;
    }

    public boolean isAir(int x, int y, int z) {
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
        int lowerBound = this.min_y - 2;
        int upperBound = this.min_y + (MAX_CAVE_SIZE_Y / FLOOR_VARIANCE_DIVISOR) + CEILING_BUFFER + 1;
        return y < lowerBound || y > upperBound;
    }

    public boolean isBoundary(int x, int y, int z) {
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

        if (NoiseChunkMixinUtils.getRiverLayer(x + 1, y, z) != null) {
            return true;
        } else if (NoiseChunkMixinUtils.getRiverLayer(x - 1, y, z) != null) {
            return true;
        } else if (NoiseChunkMixinUtils.getRiverLayer(x, y, z + 1) != null) {
            return true;
        } else return NoiseChunkMixinUtils.getRiverLayer(x, y, z - 1) != null;
    }

    public boolean isBelowWaterfallSupport(int x, int y, int z) {
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
        Boolean neighborY1 = isDropoffEdgeToNeighbor(y, y_o, mbPos);
        if (neighborY1 != null) return neighborY1;
        mbPos.set(bPos.getX(), bPos.getY(), bPos.getZ() + 1);
        Boolean neighborY2 = isDropoffEdgeToNeighbor(y, y_o, mbPos);
        if (neighborY2 != null) return neighborY2;
        mbPos.set(bPos.getX(), bPos.getY(), bPos.getZ() - 1);
        Boolean neighborY3 = isDropoffEdgeToNeighbor(y, y_o, mbPos);
        if (neighborY3 != null) return neighborY3;

        return false;
    }

    @Nullable
    private Boolean isDropoffEdgeToNeighbor(int y, int y_o, BlockPos.MutableBlockPos mbPos) {
        if (this.getWarpedNoise(mbPos.getX(), mbPos.getY(), mbPos.getZ()) > NOISE_CUTOFF_RIVER) {
            float yLevelNoise1 = this.getCaveYNoise(mbPos.getX(), mbPos.getY(), mbPos.getZ());
            int neighborY = this.getCaveY(yLevelNoise1);
            return y == neighborY && y_o != y && neighborY < y_o;
        }
        return null;
    }

    private float getCaveYNoise(int x, int y, int z) {
        return this.cache.getCaveYNoise(x, y, z);
    }

    public boolean isBelowRiverSupport(int x, int y, int z) {
        if (y <= (Globals.minY + 8)) {
            return false;
        }

        if (this.isLiquid(x, y + 1, z)) {
            return true;
        } else return this.isLiquid(x, y + 2, z);
    }
}