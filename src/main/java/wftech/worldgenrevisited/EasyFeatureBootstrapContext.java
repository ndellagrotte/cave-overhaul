package wftech.worldgenrevisited;

import java.util.List;

import com.mojang.serialization.Lifecycle;

import net.minecraft.core.Holder.Reference;
import net.minecraft.core.HolderGetter.Provider;
import net.minecraft.core.HolderLookup.RegistryLookup;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.RegistryAccess;
import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.data.worldgen.features.AquaticFeatures;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.fml.util.thread.EffectiveSide;
import net.minecraftforge.server.ServerLifecycleHooks;
import wftech.worldgenrevisited.utils.LazyLoadingSafetyWrapper;
import wftech.worldgenrevisited.utils.RegistryUtils;

public class EasyFeatureBootstrapContext<PlacedFeature> implements BootstapContext<PlacedFeature> {

	/*
	 * Check RegistryDataLoader::75 for registry stuff
	 */
	
	//Could be Holder (minecraft.core.Holder)
	private Registry<PlacedFeature> registry;
	//private MappedRegistry<ConfiguredFeature> registry_cfg;
	
	public EasyFeatureBootstrapContext(Registry<PlacedFeature> registry) {

        HolderGetter<ConfiguredFeature<?, ?>> holdergetter = this.lookup(Registries.CONFIGURED_FEATURE);
        Holder.Reference<ConfiguredFeature<?, ?>> reference = holdergetter.getOrThrow(AquaticFeatures.SEAGRASS_SHORT);
        
		this.registry = registry;
	}
	
	//I think as long as I stick to PlacementUtils.register(), I can just return null
	@Override
	public Reference register(ResourceKey<PlacedFeature> resourceKey, PlacedFeature placedFeature, Lifecycle lifeCycle) {
		//Registry<PlacedFeature> registry = p_190888_.registryAccess().registryOrThrow(Registries.PLACED_FEATURE);
		//Registries.PLACED_FEATURE;
		//Registry.register(BuiltInRegistries.FEATURE, null, null)
		Registry.register(this.registry, resourceKey, placedFeature);
		//return this.registry.register(p_256008_, p_256454_, p_255725_);
		
		HolderGetter hg = RegistryUtils.getHolderGetter(Registries.PLACED_FEATURE);
		
		return hg.getOrThrow(resourceKey);
	}

	@Override
	public HolderGetter lookup(ResourceKey requestedRegestry) {

		HolderGetter hg_requested = null;

		RegistryAccess registries;
		if(EffectiveSide.get().isClient()) {
			Level level = LazyLoadingSafetyWrapper.getClientLevel();
			registries = level.registryAccess();

			hg_requested = level.holderLookup(requestedRegestry);
			
		} else {
			MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
			registries = server.registryAccess();
			Provider registryProvider = registries.asGetterLookup();
			hg_requested = registryProvider.lookupOrThrow(requestedRegestry);		
		}
		
		return hg_requested;
	}

}
