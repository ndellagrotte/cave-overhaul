package wftech.cavesrevisited.mixins;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderGetter.Provider;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.synth.NormalNoise;
import net.minecraftforge.fml.util.thread.EffectiveSide;
import net.minecraftforge.server.ServerLifecycleHooks;
import wftech.cavesrevisited.WorldGenUtils;

@Mixin(RandomState.class)
public class RandomStateMixin_safe {

	//@Inject(method = "<init>(Lnet/minecraft/world/level/levelgen/NoiseGeneratorSettings;Lnet/minecraft/core/HolderGetter;J)V", 
	//		at = @At("RETURN"), remap=true, locals = LocalCapture.CAPTURE_FAILHARD, cancellable = true)
	@ModifyVariable(method = "<init>(Lnet/minecraft/world/level/levelgen/NoiseGeneratorSettings;Lnet/minecraft/core/HolderGetter;J)V", 
			at = @At("HEAD"), 
			remap=true)
	private static NoiseGeneratorSettings changeSettingsToOverworld(NoiseGeneratorSettings defaultSettings, 
			NoiseGeneratorSettings noiseGeneratorSettingsIn, HolderGetter<NormalNoise.NoiseParameters> holderIn, final long longIn) {
		//return NoiseGeneratorSettin

		HolderGetter noise = null;
		HolderGetter density_function = null;
		RegistryAccess registries;
		if(EffectiveSide.get().isClient()) {
			ClientLevel level = Minecraft.getInstance().level;
			registries = level.registryAccess();

			noise = level.holderLookup(Registries.NOISE);
			density_function = level.holderLookup(Registries.DENSITY_FUNCTION);
			
		} else {
			MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
			registries = server.registryAccess();
			//registries.asGetterLookup()
			Provider provider = registries.asGetterLookup();
			noise = provider.lookupOrThrow(Registries.NOISE);
			density_function = provider.lookupOrThrow(Registries.NOISE);
			
			/*
			for(Level level: server.getAllLevels()) {
				noise = level.holderLookup(Registries.NOISE);
				density_function = level.holderLookup(Registries.DENSITY_FUNCTION);
				break;
			}
			*/
		}
		
		Registry<NoiseGeneratorSettings> noiseReg = registries.registryOrThrow(Registries.NOISE_SETTINGS);
		
		Logger logger = LogManager.getFormatterLogger("cavesrevisited");
		NoiseGeneratorSettings overworldNoise = noiseReg.getOrThrow(NoiseGeneratorSettings.OVERWORLD);
		
		for(ResourceLocation key: noiseReg.keySet()) {
			//ResourceKey<NoiseGeneratorSettings>
			NoiseGeneratorSettings ngs_noise = noiseReg.get(key);
			if(key.getPath().toLowerCase().contains("overworld") && (defaultSettings == ngs_noise)) {

				NoiseGeneratorSettings modified_worldgen = new NoiseGeneratorSettings(
					    defaultSettings.noiseSettings(), 
					    defaultSettings.defaultBlock(), 
					    defaultSettings.defaultFluid(), 
					    WorldGenUtils.overworld(
				    		noise, 
				    		density_function, 
					        false, 
					        false),
					    defaultSettings.surfaceRule(), //SurfaceRuleData.overworld()
					    defaultSettings.spawnTarget(), //(new OverworldBiomeBuilder()).spawnTarget()
					    defaultSettings.seaLevel(), 
					    defaultSettings.disableMobGeneration(), 
					    defaultSettings.aquifersEnabled(), 
					    defaultSettings.oreVeinsEnabled(), 
					    defaultSettings.useLegacyRandomSource()); //63, false, true, true, false
				
				return modified_worldgen;
			}
		}
		

		//HolderGetter noise;
		//HolderGetter density_function;
		//BuiltInRegistries.BLOCK.asLookup()
		//Level#holderLookup(Registries.BLOCK)
		/*
		 * MD: net/minecraft/world/level/levelgen/NoiseRouterData/m_255262_ (Lnet/minecraft/core/HolderGetter;Lnet/minecraft/core/HolderGetter;ZZ)Lnet/minecraft/world/level/levelgen/NoiseRouter; net/minecraft/world/level/levelgen/NoiseRouterData/overworld (Lnet/minecraft/core/HolderGetter;Lnet/minecraft/core/HolderGetter;ZZ)Lnet/minecraft/world/level/levelgen/NoiseRouter;
		 */
		
		return defaultSettings;
	}
	
	
	
}