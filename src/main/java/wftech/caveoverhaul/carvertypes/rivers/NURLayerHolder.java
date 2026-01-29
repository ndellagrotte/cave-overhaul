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

    private static final int[] WATER_Y_LEVELS = { -25, -4, -4, -8, 18, 36, 48 };
    private static final int[] LAVA_Y_LEVELS = { -56, -56, -42, -25 };

    // Y levels where both water and lava rivers exist
    private static final Set<Integer> SHARED_Y_LEVELS;
    static {
        Set<Integer> waterYSet = new HashSet<>();
        Set<Integer> lavaYSet = new HashSet<>();
        for (int y : WATER_Y_LEVELS) waterYSet.add(y);
        for (int y : LAVA_Y_LEVELS) lavaYSet.add(y);
        SHARED_Y_LEVELS = new HashSet<>();
        for (int y : waterYSet) {
            if (lavaYSet.contains(y)) {
                SHARED_Y_LEVELS.add(y);
            }
        }
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
        return !layer.isEnabled()
                || !layer.isInYRange(y)
                || layer.isOutOfBounds(x, y, z)
                || isExcludedAtSharedYLevel(layer, x, z);
    }

    /**
     * At Y levels where both water and lava rivers exist, use noise to determine
     * which type "owns" each x,z position. This prevents overlap while maintaining
     * overall river frequency.
     */
    private boolean isExcludedAtSharedYLevel(NURDynamicLayer layer, int x, int z) {
        if (!SHARED_Y_LEVELS.contains(layer.getMinY())) {
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
        noise.SetFrequency(0.005f);
        return noise;
    }

    private int getWorldSeed() {
        return (int) FabricUtils.server.getWorldData().worldGenOptions().seed();
    }
}