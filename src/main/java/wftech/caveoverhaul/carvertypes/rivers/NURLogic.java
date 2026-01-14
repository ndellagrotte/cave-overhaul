package wftech.caveoverhaul.carvertypes.rivers;

import wftech.caveoverhaul.fastnoise.FastNoiseLite;
import wftech.caveoverhaul.fastnoise.FastNoiseLite.Vector3;

public class NURLogic {

    private FastNoiseLite domainWarp = null;
    public FastNoiseLite noiseIsLiquid = null;
    public FastNoiseLite noiseShouldCarveBase = null;
    public FastNoiseLite noiseYLevelBase = null;

    public NURLogic(FastNoiseLite noiseIsLiquid, FastNoiseLite noiseShouldCarveBase, FastNoiseLite noiseYLevelBase) {
        this.noiseIsLiquid = noiseIsLiquid;
        this.noiseShouldCarveBase = noiseShouldCarveBase;
        this.noiseYLevelBase = noiseYLevelBase;

        // Domain warp setup - now 3D!
        FastNoiseLite warp = new FastNoiseLite(12345);
        warp.SetDomainWarpType(FastNoiseLite.DomainWarpType.OpenSimplex2);
        warp.SetDomainWarpAmp(50.0f);
        warp.SetFrequency(0.01f);
        this.domainWarp = warp;
    }

    public float getCaveDetailsNoise2D(int x, int z) {
        return getCaveDetailsNoise3D(x, 0, z);
    }

    public float getCaveDetailsNoise3D(int x, int y, int z) {
        if (domainWarp != null) {
            Vector3 coords = new Vector3(x, y, z);
            domainWarp.DomainWarp(coords);
            return noiseIsLiquid.GetNoise(coords.x, coords.y, coords.z);
        }
        return noiseIsLiquid.GetNoise(x, y, z);
    }

    public float getCaveYNoise(int x, int z) {
        return getCaveYNoise3D(x, 0, z);
    }

    public float getCaveYNoise3D(int x, int y, int z) {
        if (domainWarp != null) {
            Vector3 coords = new Vector3(x, y, z);
            domainWarp.DomainWarp(coords);
            return noiseYLevelBase.GetNoise(coords.x, coords.y, coords.z);
        }
        return noiseYLevelBase.GetNoise(x, y, z);
    }

    public float getShouldCarveNoise(int x, int z) {
        return getShouldCarveNoise3D(x, 0, z);
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

    public float getCaveDetailsNoise(int x, int y, int z) {
        if (domainWarp != null) {
            Vector3 coords = new Vector3(x, y, z);
            domainWarp.DomainWarp(coords);
            return noiseIsLiquid.GetNoise(coords.x, coords.y, coords.z);
        }
        return noiseIsLiquid.GetNoise(x, y, z);
    }

}