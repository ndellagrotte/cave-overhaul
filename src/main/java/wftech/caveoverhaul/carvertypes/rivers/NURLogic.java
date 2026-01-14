package wftech.caveoverhaul.carvertypes.rivers;

import wftech.caveoverhaul.fastnoise.FastNoiseLite;

public class NURLogic {

    private final FastNoiseLite domainWarp;
    public FastNoiseLite noiseIsLiquid;
    public FastNoiseLite noiseShouldCarveBase = null;
    public FastNoiseLite noiseYLevelBase = null;

    public NURLogic(FastNoiseLite noiseIsLiquid, FastNoiseLite noiseShouldCarveBase, FastNoiseLite noiseYLevelBase) {
        this.noiseIsLiquid = noiseIsLiquid;
        this.noiseShouldCarveBase = noiseShouldCarveBase;
        this.noiseYLevelBase = noiseYLevelBase;

        // Domain warp setup - tweak these values for different effects
        FastNoiseLite warp = new FastNoiseLite(12345); // hardcoded seed for testing
        warp.SetDomainWarpType(FastNoiseLite.DomainWarpType.OpenSimplex2);
        warp.SetDomainWarpAmp(50.0f);  // higher = more warping
        warp.SetFrequency(0.01f);       // lower = larger warp features
        this.domainWarp = warp;
    }

    public float getCaveDetailsNoise2D(int x, int z) {
        if (domainWarp != null) {
            float[] coords = {x, z};
            domainWarp.DomainWarp(coords);
            return noiseIsLiquid.GetNoise(coords[0], coords[1]);
        }
        return noiseIsLiquid.GetNoise(x, z);
    }

    public float getCaveYNoise(int x, int z) {
        if (domainWarp != null) {
            float[] coords = {x, z};
            domainWarp.DomainWarp(coords);
            return noiseYLevelBase.GetNoise(coords[0], coords[1]);
        }
        return noiseYLevelBase.GetNoise(x, z);
    }

}