package wftech.caveoverhaul.carvertypes;

import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.levelgen.carver.CanyonCarverConfiguration;
import net.minecraft.world.level.levelgen.carver.CaveCarverConfiguration;
import net.minecraft.world.level.levelgen.carver.WorldCarver;
import wftech.caveoverhaul.CaveOverhaul;

public class InitCarverTypesFabric {

    public static final WorldCarver<CanyonCarverConfiguration> VANILLA_CANYON = new VanillaCanyon(CanyonCarverConfiguration.CODEC);
    public static final WorldCarver<CaveCarverConfiguration> CAVES_NOISE_DISTRIBUTION = new OldWorldCarverv12ReverseNoiseDistribution(CaveCarverConfiguration.CODEC);
    public static final WorldCarver<CaveCarverConfiguration> V12_CAVES = new OldWorldCarverv12(CaveCarverConfiguration.CODEC);

    public static void init() {
        /*
        Register the caves/canyons
         */
        Identifier vanilla_canyon_rloc = Identifier.tryBuild(CaveOverhaul.MOD_ID, "vanilla_canyon");
        Identifier caves_noise_distribution_rloc = Identifier.tryBuild(CaveOverhaul.MOD_ID, "caves_noise_distribution");
        Identifier v12_caves_rloc = Identifier.tryBuild(CaveOverhaul.MOD_ID, "v12_caves");
        assert vanilla_canyon_rloc != null;
        Registry.register(BuiltInRegistries.CARVER, vanilla_canyon_rloc, VANILLA_CANYON);
        assert v12_caves_rloc != null;
        Registry.register(BuiltInRegistries.CARVER, v12_caves_rloc, CAVES_NOISE_DISTRIBUTION);

        /*
        Minecraft will at some point take the above registered caves and canyons,
        then load a configured carver (defined in resources/data/caveoverhaul/worldgen/configured_carver),
        then reference the BiomeModifications's modifications to add the carvers based on the ResourceKey of our
        newly registered carvers
         */

        /*
        Declare keys
         */
        Identifier canyons_rloc = Identifier.tryBuild(CaveOverhaul.MOD_ID, "canyons");
        Identifier canyons_low_y_rloc = Identifier.tryBuild(CaveOverhaul.MOD_ID, "canyons_low_y");
        Identifier caves_rloc = Identifier.tryBuild(CaveOverhaul.MOD_ID, "caves_noise_distribution");

        /*
        Actually add
         */
        assert canyons_rloc != null;
        BiomeModifications.addCarver(
                BiomeSelectors.foundInOverworld(),
                ResourceKey.create(Registries.CONFIGURED_CARVER, canyons_rloc));

        assert canyons_low_y_rloc != null;
        BiomeModifications.addCarver(
                BiomeSelectors.foundInOverworld(),
                ResourceKey.create(Registries.CONFIGURED_CARVER, canyons_low_y_rloc));

        assert caves_rloc != null;
        BiomeModifications.addCarver(
                BiomeSelectors.foundInOverworld(),
                ResourceKey.create(Registries.CONFIGURED_CARVER, caves_rloc));

    }
}
