package wftech.caveoverhaul.carvertypes;

import wftech.caveoverhaul.fastnoise.FastNoiseLite;
import wftech.caveoverhaul.utils.FabricUtils;
import wftech.caveoverhaul.utils.FloatPos;

public class NoisetypeDomainWarp {

    private static volatile NoisetypeDomainWarp INSTANCE = null;
    private static final Object LOCK = new Object();

    public static void init(int minY) {
        if (INSTANCE == null || INSTANCE.minY != Math.abs(minY)) {
            synchronized (LOCK) {
                if (INSTANCE == null || INSTANCE.minY != Math.abs(minY)) {
                    INSTANCE = new NoisetypeDomainWarp(minY);
                }
            }
        }
    }

    public static void reset() {
        synchronized (LOCK) {
            INSTANCE = null;
        }
    }

    public static FloatPos getWarpedPosition(float xPos, float yPos, float zPos) {
        NoisetypeDomainWarp instance = INSTANCE;
        if (instance == null) {
            throw new IllegalStateException("NoisetypeDomainWarp not initialized");
        }
        return instance.getWarpedPositionInternal(xPos, yPos, zPos);
    }

    private final int minY;
    private volatile FastNoiseLite domainWarp = null;
    private final Object domainWarpLock = new Object();

    private NoisetypeDomainWarp(int minY) {
        this.minY = Math.abs(minY);
    }

    private void initDomainWarp() {
        if (domainWarp != null) {
            return;
        }

        synchronized (domainWarpLock) {
            if (domainWarp != null) {
                return;
            }

            FastNoiseLite noise = new FastNoiseLite();
            noise.SetSeed((int) FabricUtils.server.getWorldData().worldGenOptions().seed());
            noise.SetNoiseType(FastNoiseLite.NoiseType.OpenSimplex2);
            noise.SetRotationType3D(FastNoiseLite.RotationType3D.ImproveXZPlanes);
            noise.SetFrequency(0.01f);
            domainWarp = noise;
        }
    }

    private FloatPos getWarpedPositionInternal(float xPos, float yPos, float zPos) {
        if (domainWarp == null) {
            initDomainWarp();
        }

        float tYPos = yPos + minY;
        float warpSlide = 25f * (tYPos / (minY * 2f));

        float warpX = xPos;
        float warpY = yPos;
        float warpZ = zPos;

        int iterAmounts = yPos >= 0 ? 2 : 3;
        float yPosClamped = Math.min(yPos, 64f);
        float warpOffsetF = yPos >= 0
                ? ((yPosClamped / 64f) * 5f) + 5f
                : (((-yPosClamped) / 64f) * 20f) + 10f;
        int warpOffset = Math.round(warpOffsetF);

        for (int i = 0; i < iterAmounts; i++) {
            warpY += domainWarp.GetNoise(warpX, warpY, warpZ) * warpSlide;
            warpX += domainWarp.GetNoise(warpX + warpOffset, warpY + warpOffset, warpZ + warpOffset) * warpSlide;
            warpZ += domainWarp.GetNoise(warpX - warpOffset, warpY - warpOffset, warpZ - warpOffset) * warpSlide;
        }

        return new FloatPos(warpX, warpY, warpZ);
    }
}