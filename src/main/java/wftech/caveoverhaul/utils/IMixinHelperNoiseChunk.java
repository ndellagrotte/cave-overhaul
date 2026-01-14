package wftech.caveoverhaul.utils;

import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.NoiseSettings;

public interface IMixinHelperNoiseChunk {

    void wFCaveOverhaul_Fork$setNGS(NoiseGeneratorSettings NGS);
    NoiseGeneratorSettings wFCaveOverhaul_Fork$getNGS();

    void wFCaveOverhaul_Fork$setNS(NoiseSettings NS);
    NoiseSettings wFCaveOverhaul_Fork$getNS();

}