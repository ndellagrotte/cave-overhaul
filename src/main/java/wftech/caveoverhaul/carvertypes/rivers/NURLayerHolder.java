package wftech.caveoverhaul.carvertypes.rivers;

import net.minecraft.world.level.block.Blocks;

import java.util.ArrayList;
import java.util.List;

public class NURLayerHolder {

    private static volatile NURLayerHolder INSTANCE = null;
    private static final Object LOCK = new Object();

    // Duplicate entries (e.g., -4 twice) are intentional - each creates a separate layer
    // with a different seed offset, resulting in denser/more varied river generation at that depth
    private static final int[] WATER_Y_LEVELS = { 18, 36, 48 };

    private final List<NURDynamicLayer> riverLayers = new ArrayList<>();

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

        return layer.isOutOfBounds(x, y, z);
    }
}