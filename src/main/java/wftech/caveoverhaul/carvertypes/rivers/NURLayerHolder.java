package wftech.caveoverhaul.carvertypes.rivers;

import net.minecraft.world.level.block.Blocks;
import wftech.caveoverhaul.fastnoise.FastNoiseLite;
import wftech.caveoverhaul.utils.FabricUtils;
import wftech.caveoverhaul.utils.Globals;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class NURLayerHolder {

    private static volatile NURLayerHolder INSTANCE = null;
    private static final Object LOCK = new Object();

    // Duplicate entries (e.g., -4 twice) are intentional - each creates a separate layer
    // with a different seed offset, resulting in denser/more varied river generation at that depth
    private static final int[] WATER_Y_LEVELS = { -25, -4, -4, -8, 18, 36, 48 };
    private static final int[] LAVA_Y_LEVELS = { -56, -56, -42, -25 };

    // Y coordinate range where both water and lava river layers can generate
    private static final int OVERLAP_Y_MIN;
    private static final int OVERLAP_Y_MAX;
    static {
        // Calculate actual Y ranges for each layer type
        // yRangeLower = minY - 2
        // yRangeUpper = minY + (MAX_CAVE_SIZE_Y / 2) + CEILING_BUFFER + 1
        //             = minY + 6 + 4 + 1 = minY + 11  (with MAX_CAVE_SIZE_Y=12, CEILING_BUFFER=4)
        int waterMin = Integer.MAX_VALUE, waterMax = Integer.MIN_VALUE;
        int lavaMin = Integer.MAX_VALUE, lavaMax = Integer.MIN_VALUE;

        for (int minY : WATER_Y_LEVELS) {
            waterMin = Math.min(waterMin, minY - 2);
            waterMax = Math.max(waterMax, minY + 11);
        }
        for (int minY : LAVA_Y_LEVELS) {
            lavaMin = Math.min(lavaMin, minY - 2);
            lavaMax = Math.max(lavaMax, minY + 11);
        }

        // Overlap is the intersection of water and lava ranges
        OVERLAP_Y_MIN = Math.max(waterMin, lavaMin);
        OVERLAP_Y_MAX = Math.min(waterMax, lavaMax);
    }

    private final List<NURDynamicLayer> riverLayers = new ArrayList<>();
    private volatile FastNoiseLite typeSelector = null;
    private final Object typeSelectorLock = new Object();

    public static NURLayerHolder getInstance() {
        NURLayerHolder instance = INSTANCE;
        if (instance == null) {
            synchronized (LOCK) {
                instance = INSTANCE;
                if (instance == null) {
                    INSTANCE = instance = new NURLayerHolder();
                }
            }
        }
        return instance;
    }

    public static void reset() {
        synchronized (LOCK) {
            INSTANCE = null;
        }
    }

    private NURLayerHolder() {
        int seedOffset = 0;

        for (int minY : WATER_Y_LEVELS) {
            riverLayers.add(new NURDynamicLayer(Blocks.WATER, minY, seedOffset));
            seedOffset += 5;
        }

        for (int minY : LAVA_Y_LEVELS) {
            riverLayers.add(new NURDynamicLayer(Blocks.LAVA, minY, seedOffset));
            seedOffset += 5;
        }

        if (Globals.getMinY() < -64) {
            int y = -56 - 32;
            while (y > Globals.getMinY() + 8) {
                riverLayers.add(new NURDynamicLayer(Blocks.LAVA, y, seedOffset));
                seedOffset += 5;
                y -= 32;
            }
        }
    }

    public boolean shouldSetToAirRivers(int x, int y, int z) {
        for (NURDynamicLayer layer : riverLayers) {
            if (shouldProcessLayer(layer, x, y, z)) {
                continue;
            }
            if (layer.isAir(x, y, z)) {
                return true;
            }
        }
        return false;
    }

    public boolean shouldSetToStone(int x, int y, int z) {
        for (NURDynamicLayer layer : riverLayers) {
            if (shouldProcessLayer(layer, x, y, z)) {
                continue;
            }
            if (layer.isBelowRiverSupport(x, y, z)
                    || layer.isBoundary(x, y, z)) {
                return true;
            }
        }
        return false;
    }

    public NURDynamicLayer getRiverLayer(int x, int y, int z) {
        for (NURDynamicLayer layer : riverLayers) {
            if (shouldProcessLayer(layer, x, y, z)) {
                continue;
            }
            if (layer.isLiquid(x, y, z)) {
                return layer;
            }
        }
        return null;
    }

    private boolean shouldProcessLayer(NURDynamicLayer layer, int x, int y, int z) {
        if (!layer.isEnabled()) {
            return true;
        }

        if (!layer.isInYRange(y)) {
            return true;
        }

        // At Y coordinates where both water and lava layers exist,
        // check type exclusion BEFORE layer-specific bounds
        if (isExcludedAtOverlapY(layer, x, y, z)) {
            return true;
        }

        // Now check layer-specific bounds
        return layer.isOutOfBounds(x, y, z);
    }

    /**
     * At Y coordinates where both water and lava river layers can generate,
     * use noise to determine which type "owns" each x,z position. This prevents
     * overlap while maintaining overall river frequency.
     */
    private boolean isExcludedAtOverlapY(NURDynamicLayer layer, int x, int y, int z) {
        // Check if this Y coordinate is in the overlap range
        if (y < OVERLAP_Y_MIN || y > OVERLAP_Y_MAX) {
            return false;
        }

        FastNoiseLite selector = typeSelector;
        if (selector == null) {
            synchronized (typeSelectorLock) {
                selector = typeSelector;
                if (selector == null) {
                    selector = createTypeSelector();
                    typeSelector = selector;
                }
            }
        }

        float noise = selector.GetNoise(x, 0, z);
        boolean prefersWater = noise > 0;

        // Exclude this layer if its type doesn't match the noise preference
        return layer.isWater() != prefersWater;
    }

    private FastNoiseLite createTypeSelector() {
        FastNoiseLite noise = new FastNoiseLite();
        noise.SetSeed(getWorldSeed() + 12345);
        noise.SetNoiseType(FastNoiseLite.NoiseType.OpenSimplex2);
        noise.SetRotationType3D(FastNoiseLite.RotationType3D.ImproveXZPlanes);
        noise.SetFrequency(0.005f);
        return noise;
    }

    private int getWorldSeed() {
        return (int) FabricUtils.server.getWorldData().worldGenOptions().seed();
    }
}