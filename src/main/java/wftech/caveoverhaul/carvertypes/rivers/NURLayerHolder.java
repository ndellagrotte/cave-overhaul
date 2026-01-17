package wftech.caveoverhaul.carvertypes.rivers;

import net.minecraft.world.level.block.Blocks;
import wftech.caveoverhaul.utils.Globals;

import java.util.ArrayList;
import java.util.List;

//NUR stands for Noise Underground River
public class NURLayerHolder {

    public static NURLayerHolder INSTANCE = new NURLayerHolder();

    private final List<NURDynamicLayer> riverLayers = new ArrayList<>();

    public int[] min_values_water = {
            //water
            -25, -4, -4, -8, 18, 36, 48
    };
    public int[] min_values_lava = {
            //lava
            -56, -56, -42, -25
    };

    public NURLayerHolder() {
        int t_seedOffset = 0;

        for (int min_val : min_values_water) {
            this.addLayer(new NURDynamicLayer(Blocks.WATER, min_val, t_seedOffset));
            t_seedOffset += 5;
        }

        for (int min_val : min_values_lava) {
            this.addLayer(new NURDynamicLayer(Blocks.LAVA, min_val, t_seedOffset));
            t_seedOffset += 5;
        }

        if (Globals.minY < -64) {
            int start_y = -56 - 32;

            while (start_y > (Globals.minY + 8)) {
                this.addLayer(new NURDynamicLayer(Blocks.LAVA, start_y, t_seedOffset));
                t_seedOffset += 5;

                start_y -= 32;
            }
        }
    }

    public void addLayer(NURDynamicLayer layer) {
        this.riverLayers.add(layer);
    }

    public boolean shouldSetToAirRivers(int x, int y, int z) {
        return shouldSetToAirRiversInternal(x, y, z);
    }

    public NURDynamicLayer getRiverLayer(int x, int y, int z) {
        return getRiverLayerInternal(x, y, z);
    }

    public boolean shouldSetToStone(int x, int y, int z) {
        return shouldSetToStoneInternal(x, y, z);
    }

    private boolean shouldSkipLayer(NURDynamicLayer layer, int x, int y, int z) {
        if (layer.enableRiver()) {
            return true;
        }
        if (layer.isInYRange(y)) {
            return true;
        }
        if (layer.isOutOfBounds(x, y, z)) {
            return true;
        }
        return false;
    }

    public NURDynamicLayer getRiverLayerInternal(int x, int y, int z) {
        for (NURDynamicLayer layer : this.riverLayers) {
            if (shouldSkipLayer(layer, x, y, z)) {
                continue;
            }
            if (layer.isLiquid(x, y, z)) {
                return layer;
            }
        }
        return null;
    }

    public boolean shouldSetToStoneInternal(int x, int y, int z) {
        for (NURDynamicLayer layer : this.riverLayers) {
            if (shouldSkipLayer(layer, x, y, z)) {
                continue;
            }
            if (layer.isBelowRiverSupport(x, y, z)) {
                return true;
            }
            if (layer.isBelowWaterfallSupport(x, y, z)) {
                return true;
            }
            if (layer.isBoundary(x, y, z)) {
                return true;
            }
        }
        return false;
    }

    public boolean shouldSetToAirRiversInternal(int x, int y, int z) {
        for (NURDynamicLayer layer : this.riverLayers) {
            if (shouldSkipLayer(layer, x, y, z)) {
                continue;
            }
            if (layer.isAir(x, y, z)) {
                return true;
            }
        }
        return false;
    }

}