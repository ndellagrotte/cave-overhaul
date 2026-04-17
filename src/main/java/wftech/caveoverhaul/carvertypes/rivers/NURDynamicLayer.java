package wftech.caveoverhaul.carvertypes.rivers;

import net.minecraft.world.level.block.Block;
import wftech.caveoverhaul.Config;
import java.util.Arrays;

import wftech.caveoverhaul.fastnoise.FastNoiseLite;
import wftech.caveoverhaul.fastnoise.FastNoiseLite.FractalType;
import wftech.caveoverhaul.fastnoise.FastNoiseLite.NoiseType;
import wftech.caveoverhaul.utils.FabricUtils;
import wftech.caveoverhaul.utils.Globals;
import wftech.caveoverhaul.utils.NoiseChunkMixinUtils;
import wftech.caveoverhaul.utils.Settings;

public class NURDynamicLayer {

    private static final float NOISE_CUTOFF_RIVER = 0.88f;
    private static final float NOISE_CUTOFF_RIVER_NON_WARPED = 0.70f;
    private static final int CEILING_BUFFER = 4;
    private static final int FLOOR_VARIANCE_DIVISOR = 2;
    private static final int MAX_CAVE_SIZE_Y = Settings.MAX_CAVE_SIZE_Y;

    private final int minY;
    private final int seedOffset;
    private final Block fluidBlock;
    private final NURLogic cache;
    private final boolean enabled;

    // Precomputed Y range bounds
    private final int yRangeLower;
    private final int yRangeUpper;

    private volatile FastNoiseLite domainWarp = null;
    private final Object domainWarpLock = new Object();

    private final ThreadLocal<WarpedNoiseCache> warpCache;

    public NURDynamicLayer(Block fluidBlock, int minY, int seedOffset) {
        this.fluidBlock = fluidBlock;
        this.minY = minY;
        this.seedOffset = seedOffset;
        this.cache = new NURLogic(genNoiseIsLiquid(), genShouldCarveNoise(), genNoiseYLevel());

        this.enabled = Config.getBoolSetting(Config.KEY_WATER_RIVER_ENABLE);

        // Precompute Y range bounds
        this.yRangeLower = minY - 2;
        this.yRangeUpper = minY + (MAX_CAVE_SIZE_Y / FLOOR_VARIANCE_DIVISOR) + CEILING_BUFFER + 1;

        this.warpCache = ThreadLocal.withInitial(
            () -> new WarpedNoiseCache(this.yRangeLower, this.yRangeUpper)
        );
    }

    // ==================== Public Getters ====================

    public Block getFluidBlock() {
        return this.fluidBlock;
    }

    public int getMinY() {
        return this.minY;
    }

    // ==================== Enable/Range Checks ====================

    public boolean isEnabled() {
        return this.enabled;
    }

    public boolean isInYRange(int y) {
        return y >= this.yRangeLower && y <= this.yRangeUpper;
    }

    public boolean isOutOfBounds(int x, int y, int z) {
        float shouldCarveNoise = this.cache.getShouldCarveNoise3D(x, y, z);
        return shouldCarveNoise > 0.7f;
    }

    // ==================== Carving Logic ====================

    public boolean isLiquid(int x, int y, int z) {
        if (this.getNoise3D(x, y, z) < NOISE_CUTOFF_RIVER_NON_WARPED) {
            return false;
        }

        if (this.getCaveY() != y) {
            return false;
        }

        return this.getWarpedNoise(x, y, z) > NOISE_CUTOFF_RIVER;
    }

    public boolean isAir(int x, int y, int z) {
        if (this.getNoise3D(x, y, z) < NOISE_CUTOFF_RIVER_NON_WARPED) {
            return false;
        }

        float noise = this.getWarpedNoise(x, y, z);
        if (noise <= NOISE_CUTOFF_RIVER) {
            return false;
        }

        // Allow air carving down to minY + 6 (Y=-58 with standard minY=-64)
        // This supports lava rivers at Y=-56 which need air above at Y=-55
        if (y <= Globals.getMinY() + 6) {
            return false;
        }

        // Check if liquid exists below (respecting isOutOfBounds for the same reason as
        // isBelowRiverSupport: adjacent Y positions can have independent in/out-of-bounds
        // status due to 3D shouldCarveNoise with high domain warp amplitude).
        if ((!isOutOfBounds(x, y - 1, z) && isLiquid(x, y - 1, z))
                || (!isOutOfBounds(x, y - 2, z) && isLiquid(x, y - 2, z))) {
            return true;
        }

        // Extended ceiling based on noise intensity
        float noiseDelta = noise - NOISE_CUTOFF_RIVER;
        int ceilingHeight = (int) (noiseDelta * 100) / 2;

        for (int i = 1; i < ceilingHeight; i++) {
            if (!isOutOfBounds(x, y - (2 + i), z) && isLiquid(x, y - (2 + i), z)) {
                return true;
            }
        }

        return false;
    }

    public boolean isBoundary(int x, int y, int z) {
        // Allow boundary placement down to minY + 4 (Y=-60 with standard minY=-64)
        // This supports lava rivers at Y=-56 which need boundaries at Y=-57/-58
        if (y <= Globals.getMinY() + 4) {
            return false;
        }

        // If this position is inside the river channel, it's not a boundary
        if (this.getWarpedNoise(x, y, z) > NOISE_CUTOFF_RIVER) {
            return false;
        }

        // Check if any adjacent position (including above) has liquid
        return NoiseChunkMixinUtils.getRiverLayer(x + 1, y, z) != null
                || NoiseChunkMixinUtils.getRiverLayer(x - 1, y, z) != null
                || NoiseChunkMixinUtils.getRiverLayer(x, y, z + 1) != null
                || NoiseChunkMixinUtils.getRiverLayer(x, y, z - 1) != null
                || NoiseChunkMixinUtils.getRiverLayer(x, y + 1, z) != null;
    }

    public boolean isBelowRiverSupport(int x, int y, int z) {
        // Allow floor support down to minY + 4 (Y=-60 with standard minY=-64)
        // This supports lava rivers at Y=-56 which need floor at Y=-57/-58
        if (y <= Globals.getMinY() + 4) {
            return false;
        }
        // Check isOutOfBounds for the positions above before calling isLiquid.
        // shouldCarveNoise is 3D with high domain warp amplitude (50 blocks), so adjacent
        // Y positions can have independent in/out-of-bounds status. Without this check,
        // isBelowRiverSupport can place stone below "phantom liquid" that would never
        // actually be processed, overriding cave carving at river endpoints.
        return (!isOutOfBounds(x, y + 1, z) && isLiquid(x, y + 1, z))
            || (!isOutOfBounds(x, y + 2, z) && isLiquid(x, y + 2, z));
    }

    // ==================== Noise Generation ====================

    private float getNoise3D(int x, int y, int z) {
        return this.cache.getCaveDetailsNoise3D(x, y, z);
    }

    private float getWarpedNoise(int x, int y, int z) {
        WarpedNoiseCache cache = warpCache.get();
        int cx = x >> 4;
        int cz = z >> 4;
        cache.ensureChunk(cx, cz);

        int yOffset = y - cache.yBase;
        if (yOffset >= 0 && yOffset < cache.ySpan) {
            int localX = x & 15;
            int localZ = z & 15;
            int idx = cache.index(localX, localZ, yOffset);
            if (cache.computed[idx]) {
                return cache.values[idx];
            }
            float result = computeWarpedNoiseUncached(x, y, z);
            cache.values[idx] = result;
            cache.computed[idx] = true;
            return result;
        }

        return computeWarpedNoiseUncached(x, y, z);
    }

    private float computeWarpedNoiseUncached(int x, int y, int z) {
        FastNoiseLite warp = domainWarp;
        if (warp == null) {
            synchronized (domainWarpLock) {
                warp = domainWarp;
                if (warp == null) {
                    warp = createDomainWarp();
                    domainWarp = warp;
                }
            }
        }

        float warpX = x;
        float warpY = y;
        float warpZ = z;

        for (int i = 0; i < 2; i++) {
            warpX += warp.GetNoise(warpX + 20, warpY, warpZ + 20) * 2f;
            warpY += warp.GetNoise(warpX, warpY + 20, warpZ) * 2f;
            warpZ += warp.GetNoise(warpX - 20, warpY, warpZ - 20) * 2f;
        }

        return this.cache.getCaveDetailsNoise3D(warpX, warpY, warpZ);
    }

    private int getCaveY() {
        return this.minY;
    }

    // ==================== Noise Initialization ====================

    // Rivers use their own domain warp rather than the NoisetypeDomainWarp singleton.
    // Why: the singleton is tuned for caves — its amplitude scales with depth (up to ~25
    // blocks near minY) to produce twisted, large-scale cavern shapes. River channels are
    // narrow features gated by NOISE_CUTOFF_RIVER = 0.88, so feeding them through the
    // cave warp would shred them at the fixed river Y-levels (-12, 0, 12) where the
    // cave warp amplitude is already huge. Rivers need a tight, bounded meander instead:
    // higher frequency (0.025 vs 0.01) for finer detail, and a fixed amplitude of 2f
    // applied in computeWarpedNoiseUncached — consistent across Y, not depth-scaled.
    private FastNoiseLite createDomainWarp() {
        FastNoiseLite noise = new FastNoiseLite();
        noise.SetSeed(getWorldSeed());
        noise.SetNoiseType(NoiseType.OpenSimplex2);
        noise.SetRotationType3D(FastNoiseLite.RotationType3D.ImproveXZPlanes);
        noise.SetFrequency(0.025f);
        noise.SetFractalLacunarity(1.1f);
        noise.SetFractalGain(1.6f);
        return noise;
    }

    private FastNoiseLite genNoiseIsLiquid() {
        FastNoiseLite noise = new FastNoiseLite();
        noise.SetSeed(getWorldSeed() + seedOffset + 2);
        noise.SetNoiseType(NoiseType.OpenSimplex2);
        noise.SetRotationType3D(FastNoiseLite.RotationType3D.ImproveXZPlanes);
        noise.SetFrequency(0.003f);
        noise.SetFractalType(FractalType.Ridged);
        noise.SetFractalOctaves(1);
        return noise;
    }

    private FastNoiseLite genShouldCarveNoise() {
        FastNoiseLite noise = new FastNoiseLite();
        noise.SetSeed(getWorldSeed() + seedOffset + 1);
        noise.SetNoiseType(NoiseType.OpenSimplex2);
        noise.SetRotationType3D(FastNoiseLite.RotationType3D.ImproveXZPlanes);
        noise.SetFrequency(0.0015f);
        return noise;
    }

    private FastNoiseLite genNoiseYLevel() {
        FastNoiseLite noise = new FastNoiseLite();
        noise.SetSeed(getWorldSeed() + seedOffset);
        noise.SetNoiseType(NoiseType.OpenSimplex2);
        noise.SetRotationType3D(FastNoiseLite.RotationType3D.ImproveXZPlanes);
        noise.SetFrequency(0.002f);
        return noise;
    }

    private int getWorldSeed() {
        return Long.hashCode(FabricUtils.server.getWorldGenSettings().options().seed());
    }

    private static final class WarpedNoiseCache {
        int chunkX = Integer.MIN_VALUE;
        int chunkZ = Integer.MIN_VALUE;
        final float[] values;
        final boolean[] computed;
        final int yBase;
        final int ySpan;

        WarpedNoiseCache(int yRangeLower, int yRangeUpper) {
            this.yBase = yRangeLower;
            this.ySpan = yRangeUpper - yRangeLower + 1;
            int size = 16 * 16 * ySpan;
            this.values = new float[size];
            this.computed = new boolean[size];
        }

        void ensureChunk(int cx, int cz) {
            if (cx != chunkX || cz != chunkZ) {
                chunkX = cx;
                chunkZ = cz;
                Arrays.fill(computed, false);
            }
        }

        int index(int localX, int localZ, int yOffset) {
            return (localX * 16 + localZ) * ySpan + yOffset;
        }
    }
}