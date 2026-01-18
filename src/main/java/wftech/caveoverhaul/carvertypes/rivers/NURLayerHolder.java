package wftech.caveoverhaul.carvertypes.rivers;

import net.minecraft.world.level.block.Blocks;
import wftech.caveoverhaul.utils.Globals;

import java.util.ArrayList;
import java.util.List;

public class NURLayerHolder {

    private static NURLayerHolder INSTANCE = null;

    private static final int[] WATER_Y_LEVELS = { -25, -4, -4, -8, 18, 36, 48 };
    private static final int[] LAVA_Y_LEVELS = { -56, -56, -42, -25 };

    private final List<NURDynamicLayer> riverLayers = new ArrayList<>();

    public static NURLayerHolder getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new NURLayerHolder();
        }
        return INSTANCE;
    }

    public static void reset() {
        INSTANCE = null;
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

        if (Globals.minY < -64) {
            int y = -56 - 32;
            while (y > Globals.minY + 8) {
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
                    || layer.isBelowWaterfallSupport(x, y, z)
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
                || layer.isOutOfBounds(x, y, z);
    }
}