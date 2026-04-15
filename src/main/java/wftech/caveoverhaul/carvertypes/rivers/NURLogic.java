package wftech.caveoverhaul.carvertypes.rivers;

import wftech.caveoverhaul.carvertypes.NoisetypeDomainWarp;
import wftech.caveoverhaul.fastnoise.FastNoiseLite;
import wftech.caveoverhaul.utils.FloatPos;

public class NURLogic {

    public FastNoiseLite noiseIsLiquid;
    public FastNoiseLite noiseShouldCarveBase;
    public FastNoiseLite noiseYLevelBase;

    public NURLogic(FastNoiseLite noiseIsLiquid, FastNoiseLite noiseShouldCarveBase, FastNoiseLite noiseYLevelBase) {
        this.noiseIsLiquid = noiseIsLiquid;
        this.noiseShouldCarveBase = noiseShouldCarveBase;
        this.noiseYLevelBase = noiseYLevelBase;
    }

    public float getCaveYNoise(int x, int y, int z) {
        FloatPos warped = NoisetypeDomainWarp.getWarpedPosition(x, y, z);
        return noiseYLevelBase.GetNoise(warped.x(), warped.y(), warped.z());
    }

    public float getShouldCarveNoise3D(int x, int y, int z) {
        FloatPos warped = NoisetypeDomainWarp.getWarpedPosition(x, y, z);
        return noiseShouldCarveBase.GetNoise(warped.x(), warped.y(), warped.z());
    }

    public float getCaveDetailsNoise3D(int x, int y, int z) {
        FloatPos warped = NoisetypeDomainWarp.getWarpedPosition(x, y, z);
        return noiseIsLiquid.GetNoise(warped.x(), warped.y(), warped.z());
    }

    public float getCaveDetailsNoise3D(float x, float y, float z) {
        FloatPos warped = NoisetypeDomainWarp.getWarpedPosition(x, y, z);
        return noiseIsLiquid.GetNoise(warped.x(), warped.y(), warped.z());
    }

}