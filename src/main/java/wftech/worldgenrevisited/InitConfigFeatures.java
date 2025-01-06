package wftech.worldgenrevisited;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.HolderGetter.Provider;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.data.worldgen.features.OreFeatures;
import net.minecraft.data.worldgen.placement.OrePlacements;
import net.minecraft.data.worldgen.placement.PlacementUtils;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.valueproviders.FloatProvider;
import net.minecraft.util.valueproviders.UniformFloat;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.carver.CarverDebugSettings;
import net.minecraft.world.level.levelgen.carver.CaveCarverConfiguration;
import net.minecraft.world.level.levelgen.carver.ConfiguredWorldCarver;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.OreFeature;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration;
import net.minecraft.world.level.levelgen.heightproviders.HeightProvider;
import net.minecraft.world.level.levelgen.heightproviders.UniformHeight;
import net.minecraft.world.level.levelgen.placement.CountPlacement;
import net.minecraft.world.level.levelgen.placement.HeightRangePlacement;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;
import net.minecraft.world.level.levelgen.placement.PlacementModifierType;
import net.minecraft.world.level.levelgen.placement.RarityFilter;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.util.thread.EffectiveSide;
import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.server.ServerLifecycleHooks;
import wftech.worldgenrevisited.utils.RegistryUtils;

public class InitConfigFeatures {

	public static List<Holder<PlacedFeature>> newFeatureList;
	public static List<Holder<PlacedFeature>> featuresToRemove;

	public static void init() {
		/*
		 * Build a comprehensive list of features I want to remove
		 */
		List<String> newOreFrequencies1 = Config.CHANGE_ORE_FREQUENCY_LIST.get();
		List<String> requestedFeaturesToRemove = Config.CHANGE_ORE_FREQUENCY_LIST.get();
		
		List<Holder<PlacedFeature>> newOrePlacements1 = new ArrayList<Holder<PlacedFeature>>();
		List<Reference> referencesToRegister1 = new ArrayList<Reference>();
		
		HolderGetter<PlacedFeature> holderGetter = RegistryUtils.getHolderGetter(Registries.PLACED_FEATURE);
		
		for(String featureToRemoveRaw: requestedFeaturesToRemove) {
			String featurePart1 = featureToRemoveRaw;
			try {
				Holder<PlacedFeature> holderFeature = (Holder<PlacedFeature>) holderGetter.get(ResourceKey.create(Registries.PLACED_FEATURE, new ResourceLocation(featurePart1))).get();
				featuresToRemove.add(holderFeature);
			} catch (ClassCastException e) {
				e.printStackTrace();
			}
		}
		
		
		for(String featureToRemoveRaw: newOreFrequencies1) {
			String featurePart1 = featureToRemoveRaw.split("=")[0];
			try {
				Holder<PlacedFeature> holderFeature = (Holder<PlacedFeature>) holderGetter.get(ResourceKey.create(Registries.PLACED_FEATURE, new ResourceLocation(featurePart1))).get();
				featuresToRemove.add(holderFeature);
			} catch (ClassCastException e) {
				e.printStackTrace();
			}
		}
		
		/*
		 * Re-register features
		 */
		List<String> newOreFrequencies = Config.CHANGE_ORE_FREQUENCY_LIST.get();
		
		List<Holder<PlacedFeature>> newOrePlacements = new ArrayList<Holder<PlacedFeature>>();
		List<Reference> referencesToRegister = new ArrayList<Reference>();
		
		Registry<PlacedFeature> placedFeatureRegistry = RegistryUtils.getRegistryDirect(Registries.PLACED_FEATURE);
		//Registry<PlacedFeature> placedFeatureRegistry = RegistryUtils.getRegistry(Registries.PLACED_FEATURE);
		
		BootstapContext placedFeatureBC = new EasyFeatureBootstrapContext(RegistryUtils.getRegistry(Registries.PLACED_FEATURE));
		
		for(String featureToRemoveRaw: newOreFrequencies) {
			String featurePart1 = featureToRemoveRaw.split("=")[0];
			ResourceLocation targetPlacement = new ResourceLocation(featurePart1);
			float frequencyMultiplier = Float.parseFloat(featureToRemoveRaw.split("=")[1]);
			
			for(ResourceLocation key: placedFeatureRegistry.keySet()) {

				if(!key.equals(targetPlacement) && !featurePart1.equals("all_ores")) {
					continue;
				}
				
				PlacedFeature foundFeature = placedFeatureRegistry.get(key);

				if(featurePart1.equals("all_ores")) {
					if(!(foundFeature.feature().value().feature() == Feature.ORE)) {
						continue;
					}
				}
				
				Holder<ConfiguredFeature<?, ?>> configuredFeature = foundFeature.feature();
				List<PlacementModifier> modifiersToRegister = new ArrayList<PlacementModifier>();
				
				boolean foundFrequencyModifier = false;
				
				for(PlacementModifier modifier: foundFeature.placement()) {
					//WorldgenRevisited.LOGGER.info("[WorldgenRevisited/InitConfigFeatures] Placement modifier: " + modifier);
					
					if(modifier.type() == PlacementModifierType.RARITY_FILTER) {
						RarityFilter foundModifier = (RarityFilter) modifier;
						frequencyMultiplier = 1 / frequencyMultiplier;
						int newChance = (int) Math.floor(foundModifier.chance * frequencyMultiplier);
						newChance = newChance == 0 ? 1 : newChance;
						RarityFilter newFilter = RarityFilter.onAverageOnceEvery(newChance);
						modifiersToRegister.add(newFilter);
						foundFrequencyModifier = true;
					} else if(modifier.type() == PlacementModifierType.COUNT) {
						CountPlacement foundModifier = (CountPlacement) modifier;
						modifiersToRegister.add(CountPlacement.of((int) Math.floor(foundModifier.count.getMaxValue() * frequencyMultiplier)));
						foundFrequencyModifier = true;
					} else {
						modifiersToRegister.add(modifier);
					}
				}
				
				String keyPart = targetPlacement.getPath();
				Reference reference = new Reference(placedFeatureBC, "cr_replaced_" + keyPart, configuredFeature, modifiersToRegister);
				referencesToRegister.add(reference);

			}
		}

		for(Reference reference: referencesToRegister) {
			Holder<PlacedFeature> result = reference.register();
			newOrePlacements.add(result);
		}
		
		newFeatureList = newOrePlacements;
		
		
	}
	
	public static class Reference{
		public BootstapContext contextToUse;
		public String name;
		public Holder<ConfiguredFeature<?, ?>> feature;
		public List<PlacementModifier> modifiers;
		
		public ResourceKey<PlacedFeature> resultingKey;
		
		public Reference(BootstapContext contextToUse, String name, Holder feature, List modifiers) {
			this.contextToUse = contextToUse;
			this.name = name;
			this.feature = feature;
			this.modifiers = modifiers;
		}
		
		public Holder<PlacedFeature> register(){
			//public static final ResourceKey<PlacedFeature> ORE_COAL_UPPER = PlacementUtils.createKey("ore_coal_upper");
			this.resultingKey = PlacementUtils.createKey(this.name);
			
			
			HolderGetter<ConfiguredFeature<?, ?>> hg_feature = RegistryUtils.getHolderGetter(Registries.CONFIGURED_FEATURE);
			Holder.Reference<ConfiguredFeature<?, ?>> holder12 = hg_feature.getOrThrow(OreFeatures.ORE_COAL);
			PlacementUtils.register(this.contextToUse, this.resultingKey, holder12, this.modifiers);

			HolderGetter<PlacedFeature> hg_placed_feature = RegistryUtils.getHolderGetter(Registries.PLACED_FEATURE);
			
			return hg_placed_feature.getOrThrow(this.resultingKey);
		}
		
		public ResourceKey<PlacedFeature> getKey(){
			return this.resultingKey;
		}
	}
	
	
}
