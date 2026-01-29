package wftech.caveoverhaul.mixins;

import net.minecraft.world.level.levelgen.*;
import org.jspecify.annotations.NonNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.synth.NormalNoise;
import wftech.caveoverhaul.Config;
import wftech.caveoverhaul.NoiseMaker;
import wftech.caveoverhaul.WorldGenUtils;
import wftech.caveoverhaul.utils.FabricUtils;

@Mixin(RandomState.class)
public class RandomStateMixin {

    @ModifyVariable(
            method = "<init>(Lnet/minecraft/world/level/levelgen/NoiseGeneratorSettings;Lnet/minecraft/core/HolderGetter;J)V",
            at = @At("HEAD"),
            argsOnly = true
    )
    private static NoiseGeneratorSettings changeSettingsToOverworld(NoiseGeneratorSettings defaultSettings,
                                                                    NoiseGeneratorSettings noiseGeneratorSettingsIn,
                                                                    HolderGetter<NormalNoise.NoiseParameters> holderIn,
                                                                    long seed) {
        Config.initConfig();

        if (Config.getBoolSetting(Config.KEY_GENERATE_CAVERNS)) {
            return defaultSettings;
        }

        RegistryAccess registries = FabricUtils.server.registryAccess();
        Registry<NoiseGeneratorSettings> noiseReg = registries.lookupOrThrow(Registries.NOISE_SETTINGS);

        // Register overworld settings with WorldGenUtils
        noiseReg.get(NoiseGeneratorSettings.OVERWORLD)
                .ifPresent(holder -> WorldGenUtils.addNGS(holder.value()));

        // Only modify if this is the overworld settings
        if (!isOverworldSettings(noiseReg, defaultSettings)) {
            return defaultSettings;
        }

        return createModifiedSettings(defaultSettings, registries);
    }

    @Unique
    private static boolean isOverworldSettings(Registry<NoiseGeneratorSettings> registry,
                                               NoiseGeneratorSettings settings) {
        return registry.get(NoiseGeneratorSettings.OVERWORLD)
                .map(holder -> holder.value() == settings)
                .orElse(false);
    }

    @Unique
    private static NoiseGeneratorSettings createModifiedSettings(NoiseGeneratorSettings defaultSettings,
                                                                 RegistryAccess registries) {
        HolderGetter<DensityFunction> densityFunctions = registries.lookupOrThrow(Registries.DENSITY_FUNCTION);

        DensityFunction factor = NoiseRouterDataAccessor.getFunction(densityFunctions, NoiseRouterDataAccessor.FACTOR());
        DensityFunction depth = NoiseRouterDataAccessor.getFunction(densityFunctions, NoiseRouterDataAccessor.DEPTH());
        DensityFunction gradientDensity = NoiseRouterDataAccessor.noiseGradientDensity(
                DensityFunctions.cache2d(factor), depth);

        NoiseRouterDataAccessor.slideOverworld(
                false,
                DensityFunctions.add(gradientDensity, DensityFunctions.constant(-0.703125D))
                        .clamp(-64.0D, 64.0D)
        );

        NoiseRouterDataAccessor.postProcess(
                NoiseRouterDataAccessor.getFunction(densityFunctions, NoiseRouterDataAccessor.SLOPED_CHEESE())
        );

        DensityFunction modifiedFinalDensity = NoiseMaker.makeNoise(
                defaultSettings.noiseRouter().finalDensity()
        );

        NoiseRouter newRouter = getNoiseRouter(defaultSettings, modifiedFinalDensity);

        return new NoiseGeneratorSettings(
                defaultSettings.noiseSettings(),
                defaultSettings.defaultBlock(),
                defaultSettings.defaultFluid(),
                newRouter,
                defaultSettings.surfaceRule(),
                defaultSettings.spawnTarget(),
                defaultSettings.seaLevel(),
                defaultSettings.disableMobGeneration(),
                defaultSettings.aquifersEnabled(),
                defaultSettings.oreVeinsEnabled(),
                defaultSettings.useLegacyRandomSource()
        );
    }

    @Unique
    private static @NonNull NoiseRouter getNoiseRouter(NoiseGeneratorSettings defaultSettings, DensityFunction modifiedFinalDensity) {
        NoiseRouter originalRouter = defaultSettings.noiseRouter();
        return new NoiseRouter(
                originalRouter.barrierNoise(),
                originalRouter.fluidLevelFloodednessNoise(),
                originalRouter.fluidLevelSpreadNoise(),
                originalRouter.lavaNoise(),
                originalRouter.temperature(),
                originalRouter.vegetation(),
                originalRouter.continents(),
                originalRouter.erosion(),
                originalRouter.depth(),
                originalRouter.ridges(),
                originalRouter.preliminarySurfaceLevel(),
                modifiedFinalDensity,
                originalRouter.veinToggle(),
                originalRouter.veinRidged(),
                originalRouter.veinGap()
        );
    }
}