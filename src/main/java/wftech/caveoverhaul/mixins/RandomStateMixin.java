package wftech.caveoverhaul.mixins;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.levelgen.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.levelgen.synth.NormalNoise;
import wftech.caveoverhaul.Config;
import wftech.caveoverhaul.NoiseMaker;
import wftech.caveoverhaul.WorldGenUtils;
import wftech.caveoverhaul.utils.FabricUtils;

import java.util.Map;

@Mixin(RandomState.class)
public class RandomStateMixin {

	//SRG name: <init>(Lnet/minecraft/world/level/levelgen/NoiseGeneratorSettings;Lnet/minecraft/core/HolderGetter;J)V
	@ModifyVariable(method = "<init>(Lnet/minecraft/world/level/levelgen/NoiseGeneratorSettings;Lnet/minecraft/core/HolderGetter;J)V",
			at = @At("HEAD"),
			remap = true, argsOnly = true)
	private static NoiseGeneratorSettings changeSettingsToOverworld(NoiseGeneratorSettings defaultSettings,
																	NoiseGeneratorSettings noiseGeneratorSettingsIn, HolderGetter<NormalNoise.NoiseParameters> holderIn, final long longIn) {

        Config.initConfig();

        RegistryAccess registries;
        MinecraftServer server = FabricUtils.server;
        registries = server.registryAccess();

        Registry<NoiseGeneratorSettings> noiseReg = registries.lookupOrThrow(Registries.NOISE_SETTINGS);

        noiseReg.get(NoiseGeneratorSettings.OVERWORLD)
                .orElseThrow(() -> new IllegalStateException("Overworld noise settings not found"));

        for (Map.Entry<ResourceKey<NoiseGeneratorSettings>, NoiseGeneratorSettings> entry : noiseReg.entrySet()) {
            if (entry.getKey().identifier().getPath().equals("overworld")) {
                WorldGenUtils.addNGS(entry.getValue());
            }
        }

        for (Identifier key : noiseReg.keySet()) {
            NoiseGeneratorSettings ngs_noise = noiseReg.get(key)
                    .orElseThrow(() -> new IllegalStateException("Noise settings not found for: " + key))
                    .value();

            if ((!Config.getBoolSetting(Config.KEY_GENERATE_CAVERNS))
                    && key.getPath().toLowerCase().contains("overworld") // Note: & should be &&
                    && (defaultSettings == ngs_noise)) {

                NoiseRouter defaultNoiseRouter = defaultSettings.noiseRouter();
                HolderGetter<DensityFunction> hg_density_function = registries.lookupOrThrow(Registries.DENSITY_FUNCTION);

                DensityFunction densityfunction8 = NoiseRouterDataAccessor.getFunction(hg_density_function, NoiseRouterDataAccessor.FACTOR());
                DensityFunction densityfunction9 = NoiseRouterDataAccessor.getFunction(hg_density_function, NoiseRouterDataAccessor.DEPTH());
                DensityFunction densityfunction10 = NoiseRouterDataAccessor.noiseGradientDensity(
                        DensityFunctions.cache2d(densityfunction8), densityfunction9);

                NoiseRouterDataAccessor.slideOverworld(
                        false,
                        DensityFunctions.add(
                                densityfunction10,
                                DensityFunctions.constant(-0.703125D)
                        ).clamp(-64.0D, 64.0D));

                // ... rest of the logic

                NoiseRouterDataAccessor.postProcess(NoiseRouterDataAccessor.getFunction(hg_density_function, NoiseRouterDataAccessor.SLOPED_CHEESE()));

                DensityFunction modifiedDF = NoiseMaker.makeNoise(defaultSettings.noiseRouter().finalDensity());

                NoiseRouter newNoiseRouter = new NoiseRouter(
                        defaultNoiseRouter.barrierNoise(),
                        defaultNoiseRouter.fluidLevelFloodednessNoise(),
                        defaultNoiseRouter.fluidLevelSpreadNoise(),
                        defaultNoiseRouter.lavaNoise(),
                        defaultNoiseRouter.temperature(),
                        defaultNoiseRouter.vegetation(),
                        defaultNoiseRouter.continents(),
                        defaultNoiseRouter.erosion(),
                        defaultNoiseRouter.depth(),
                        defaultNoiseRouter.ridges(),
                        defaultNoiseRouter.preliminarySurfaceLevel(),
                        modifiedDF, //newFinalDensity
                        defaultNoiseRouter.veinToggle(),
                        defaultNoiseRouter.veinRidged(),
                        defaultNoiseRouter.veinGap()
                );

                return new NoiseGeneratorSettings(
                        defaultSettings.noiseSettings(),
                        defaultSettings.defaultBlock(),
                        defaultSettings.defaultFluid(),
                        newNoiseRouter,
                        defaultSettings.surfaceRule(),
                        defaultSettings.spawnTarget(),
                        defaultSettings.seaLevel(),
                        defaultSettings.disableMobGeneration(),
                        defaultSettings.aquifersEnabled(),
                        defaultSettings.oreVeinsEnabled(),
                        defaultSettings.useLegacyRandomSource());
            }
        }
        return defaultSettings;
    }
}