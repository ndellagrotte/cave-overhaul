package wftech.caveoverhaul.carvertypes;

import wftech.caveoverhaul.fastnoise.FastNoiseLite;
import wftech.caveoverhaul.utils.FabricUtils;
import wftech.caveoverhaul.utils.Globals;

import java.util.ArrayList;
import java.util.List;

//NC stands for NoiseCave
public class NCLayerHolder {

    private static volatile NCLayerHolder INSTANCE = null;
    private static final Object LOCK = new Object();

    public static NCLayerHolder getInstance() {
        NCLayerHolder instance = INSTANCE;
        if (instance == null) {
            synchronized (LOCK) {
                instance = INSTANCE;
                if (instance == null) {
                    INSTANCE = instance = new NCLayerHolder(Globals.getMinY());
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

    /*
    Actual class
     */

    private final List<NCDynamicLayer> layers = new ArrayList<>();
    private final int[][] height_min_max_for_each_layer = {
            {96, 128}, {64, 96}, {32, 64}, //top layers
            {0, 32}, {-32, 0}, //middle layers
            // DO NOT REMOVE THE REPEATED ENTRIES
            {-64, 0}, {-64, 0}, {-64, -54}, //bottom layers, the repeated segments are intentional so more cave bits
            //are exposed
    };
    /*
    private int[][] height_min_max_for_each_layer = {
            {128, 96}, {96, 64}, {64, 32}, //top layers
            {32, 0}, {0, -32}, //middle layers
            // DO NOT REMOVE THE REPEATED ENTRIES
            {0, -64}, {0, -64}, {-54, -64}, //bottom layers, the repeated segments are intentional so more cave bits
            //are exposed
    };

     */

    public NCLayerHolder(int min_y){

        int seed = (int) FabricUtils.server.getWorldData().worldGenOptions().seed();
        FastNoiseLite genericNoiseStructural = this.genStructuralNoise(seed, 0);

        addMainLayers(seed, min_y, genericNoiseStructural);

        if (min_y < -64) {
            addExtraLayers(seed, min_y, genericNoiseStructural);
        }

    }


    private void addMainLayers(int seed, int min_y, FastNoiseLite genericNoiseStructural) {
        // Load in the predefined segments
        for(int i = 0; i < height_min_max_for_each_layer.length; i++) {
            FastNoiseLite genericNoiseThickness = this.genThicknessHeight(seed, (i * 3));
            FastNoiseLite genericNoiseHeight = this.genCaveHeight(seed, (i * 3) + 1);

            layers.add(new NCDynamicLayer(
                    height_min_max_for_each_layer[i][0],
                    height_min_max_for_each_layer[i][1],
                    genericNoiseThickness,
                    genericNoiseHeight,
                    genericNoiseStructural));
        }
    }

    private void addExtraLayers(int seed, int min_y, FastNoiseLite genericNoiseStructural) {

        // If y != -64, add more cave layers
        int seedOffset = height_min_max_for_each_layer.length * 3;
        for(int new_y = -64; new_y > min_y; new_y -= 64 ) {
            int new_min = new_y - 64;
            new_min = Math.max(new_min, min_y); //clamp to min_y

            FastNoiseLite genericNoiseThickness = this.genThicknessHeight(seed, seedOffset);
            FastNoiseLite genericNoiseHeight = this.genCaveHeight(seed, seedOffset + 1);

            FastNoiseLite genericNoiseThickness2 = this.genThicknessHeight(seed, seedOffset + 3);
            FastNoiseLite genericNoiseHeight2 = this.genCaveHeight(seed, seedOffset + 4);

            seedOffset += 6;

            //Double-up 1
            layers.add(new NCDynamicLayer(
                    new_min,
                    new_y,
                    genericNoiseThickness,
                    genericNoiseHeight,
                    genericNoiseStructural));

            //Double-up 2
            layers.add(new NCDynamicLayer(
                    new_min,
                    new_y,
                    genericNoiseThickness2,
                    genericNoiseHeight2,
                    genericNoiseStructural));
        }

    }

    public FastNoiseLite genStructuralNoise(int seed, int seedOffset) {
        FastNoiseLite tnoise = new FastNoiseLite();
        tnoise.SetSeed((int) FabricUtils.server.getWorldData().worldGenOptions().seed());
        tnoise.SetFractalOctaves(1);
        tnoise.SetNoiseType(FastNoiseLite.NoiseType.OpenSimplex2);
        tnoise.SetFractalGain(0.3f);
        tnoise.SetFrequency(0.025f);
        tnoise.SetFractalType(FastNoiseLite.FractalType.FBm);

        return tnoise;
    }

    /*
    Cave layer specific
    Or is it global? I don't recall. Initial checks imply these are global settings.
    BTW the change between y layer caves is based on y depth and domain warping.
    Lower y = stronger domain warping
     */
    public FastNoiseLite genThicknessHeight(int seed, int seedOffset) {
        seed += seedOffset + 1;

        FastNoiseLite tnoise = new FastNoiseLite();
        tnoise.SetSeed(seed);
        tnoise.SetNoiseType(FastNoiseLite.NoiseType.OpenSimplex2); //SimplexFractal
        tnoise.SetFrequency(0.015f); //was 0.01
        tnoise.SetFractalType(FastNoiseLite.FractalType.FBm);
        tnoise.SetFractalGain(1.3f); //seems to top out at 3.5 though
        tnoise.SetFractalOctaves(2);
        tnoise.SetFractalLacunarity(0.2f); //<-- 0.1?

        return tnoise;
    }

    /*
    Cave layer specific
    Or is it global? Just like the above! I don't recall. Initial checks imply these are global settings.
    BTW the change between y layer caves is based on y depth and domain warping.
    Lower y = stronger domain warping
     */

    public FastNoiseLite genCaveHeight(int seed, int seedOffset) {
        seed += seedOffset;

        FastNoiseLite tnoise = new FastNoiseLite();
        tnoise.SetSeed(seed);
        tnoise.SetNoiseType(FastNoiseLite.NoiseType.OpenSimplex2);
        tnoise.SetFrequency(0.01f);
        tnoise.SetFractalType(FastNoiseLite.FractalType.FBm);
        tnoise.SetFractalGain(2.5f);
        tnoise.SetFractalOctaves(2);
        tnoise.SetFractalLacunarity(0.1f);

        return tnoise;
    }

    public boolean shouldCarveInternal(float x, float y, float z) {
        for(NCDynamicLayer layer: this.layers) {
            if(!layer.isInYRange((int) y)) {
                continue;
            }

            if (layer.shouldCarve((int) x, (int) y, (int) z)) {
                return true;
            }
        }

        return false;
    }

    public boolean shouldCarve(float x, float y, float z) {
        return shouldCarveInternal(x, y, z);
    }

}