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

    // ==== v1 carvers ====
    public static final WorldCarver<CanyonCarverConfiguration> VANILLA_CANYON = new VanillaCanyon(CanyonCarverConfiguration.CODEC);
    public static final WorldCarver<CaveCarverConfiguration> V12_CAVES = new OldWorldCarverv12(CaveCarverConfiguration.CODEC);

    // ==== v2 carvers ====
    // Old World Caves v2 — hybrid chunk-gate + v1-style node-walk. Gated at
    // runtime by Config.KEY_DEBUG_OLD_WORLD_CAVES_V2; always registered so
    // the config toggle can be flipped at runtime without a world reload.
    public static final WorldCarver<CaveCarverConfiguration> V2_CAVES = new OldWorldV2Carver(CaveCarverConfiguration.CODEC);

    public static void init() {
        /*
        Register the caves/canyons
         */
        // ==== v1 registrations ====
        Identifier vanilla_canyon_rloc = Identifier.tryBuild(CaveOverhaul.MOD_ID, "vanilla_canyon");
        Identifier v12_caves_rloc = Identifier.tryBuild(CaveOverhaul.MOD_ID, "v12_caves");
        assert vanilla_canyon_rloc != null;
        Registry.register(BuiltInRegistries.CARVER, vanilla_canyon_rloc, VANILLA_CANYON);
        assert v12_caves_rloc != null;
        Registry.register(BuiltInRegistries.CARVER, v12_caves_rloc, V12_CAVES);

        // ==== v2 registrations ====
        Identifier v2_caves_rloc = Identifier.tryBuild(CaveOverhaul.MOD_ID, "v2_caves");
        assert v2_caves_rloc != null;
        Registry.register(BuiltInRegistries.CARVER, v2_caves_rloc, V2_CAVES);

        /*
        Minecraft will at some point take the above registered caves and canyons,
        then load a configured carver (defined in resources/data/caveoverhaul/worldgen/configured_carver),
        then reference the BiomeModifications's modifications to add the carvers based on the ResourceKey of our
        newly registered carvers
         */

        /*
        Declare keys
         */
        // ==== v1 configured carver keys ====
        Identifier canyons_rloc = Identifier.tryBuild(CaveOverhaul.MOD_ID, "canyons");
        Identifier canyons_low_y_rloc = Identifier.tryBuild(CaveOverhaul.MOD_ID, "canyons_low_y");
        Identifier caves_rloc = Identifier.tryBuild(CaveOverhaul.MOD_ID, "caves_noise_distribution");

        // ==== v2 configured carver key ====
        Identifier caves_v2_rloc = Identifier.tryBuild(CaveOverhaul.MOD_ID, "caves_v2");

        /*
        Actually add
         */
        // ==== v1 biome attachments ====
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

        // ==== v2 biome attachment ====
        // v2 is attached unconditionally; its carve() short-circuits on the
        // debug toggle, so this does nothing when the toggle is off.
        assert caves_v2_rloc != null;
        BiomeModifications.addCarver(
                BiomeSelectors.foundInOverworld(),
                ResourceKey.create(Registries.CONFIGURED_CARVER, caves_v2_rloc));
    }
}
