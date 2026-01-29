package wftech.caveoverhaul.utils;

import wftech.caveoverhaul.fastnoise.FastNoiseLite;

public class NoiseUtils {

    /**
     * Creates a standard domain warp noise generator used for river and cave generation.
     * Uses OpenSimplex2 with consistent parameters across the codebase.
     */
    public static FastNoiseLite createStandardDomainWarp() {
        FastNoiseLite warp = new FastNoiseLite(12345);
        warp.SetDomainWarpType(FastNoiseLite.DomainWarpType.OpenSimplex2);
        warp.SetDomainWarpAmp(50.0f);
        warp.SetFrequency(0.01f);
        return warp;
    }
}
