package wftech.caveoverhaul.carvertypes.rivers;

import wftech.caveoverhaul.fastnoise.FastNoiseLite;
import wftech.caveoverhaul.fastnoise.FastNoiseLite.Vector3;
import wftech.caveoverhaul.utils.NoiseUtils;

public class NURLogic {

    private final FastNoiseLite domainWarp;
    public FastNoiseLite noiseIsLiquid;
    public FastNoiseLite noiseShouldCarveBase;
    public FastNoiseLite noiseYLevelBase;

    public NURLogic(FastNoiseLite noiseIsLiquid, FastNoiseLite noiseShouldCarveBase, FastNoiseLite noiseYLevelBase) {
        this.noiseIsLiquid = noiseIsLiquid;
        this.noiseShouldCarveBase = noiseShouldCarveBase;
        this.noiseYLevelBase = noiseYLevelBase;
        this.domainWarp = NoiseUtils.createStandardDomainWarp();
    }

    public float getCaveYNoise(int x, int y, int z) {
        if (domainWarp != null) {
            Vector3 coords = new Vector3(x, y, z);
            domainWarp.DomainWarp(coords);
            return noiseYLevelBase.GetNoise(coords.x, coords.y, coords.z);
        }
        return noiseYLevelBase.GetNoise(x, y, z);
    }

    public float getShouldCarveNoise3D(int x, int y, int z) {
        if (domainWarp != null) {
            Vector3 coords = new Vector3(x, y, z);
            domainWarp.DomainWarp(coords);
            return noiseShouldCarveBase.GetNoise(coords.x, coords.y, coords.z);
        }
        return noiseShouldCarveBase.GetNoise(x, y, z);
    }

    public float getCaveDetailsNoise3D(int x, int y, int z) {
        if (domainWarp != null) {
            Vector3 coords = new Vector3(x, y, z);
            domainWarp.DomainWarp(coords);
            return noiseIsLiquid.GetNoise(coords.x, coords.y, coords.z);
        }
        return noiseIsLiquid.GetNoise(x, y, z);
    }

}