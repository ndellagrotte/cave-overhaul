package wftech.worldgenrevisited.biomemodifiers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.Lifecycle;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.core.WritableRegistry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.resources.RegistryDataLoader.Loader;
import net.minecraft.resources.RegistryDataLoader.RegistryData;
import net.minecraft.server.packs.resources.CloseableResourceManager;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.GenerationStep.Carving;
import net.minecraft.world.level.levelgen.GenerationStep.Decoration;
import net.minecraft.world.level.levelgen.carver.ConfiguredWorldCarver;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraftforge.common.world.BiomeGenerationSettingsBuilder;
import net.minecraftforge.common.world.BiomeModifier;
import net.minecraftforge.common.world.ModifiableBiomeInfo;
import net.minecraftforge.registries.DataPackRegistriesHooks;
import wftech.worldgenrevisited.WorldgenRevisited;
import wftech.worldgenrevisited.InitConfigFeatures;
import wftech.worldgenrevisited.utils.RegistryUtils;
import wftech.worldgenrevisited.virtualpack.AddPackFindersEventWatcher;
import wftech.worldgenrevisited.virtualpack.JsonConfigFeatures;
//Reference below
import net.minecraftforge.common.world.ForgeBiomeModifiers;

public record DynamicAddFeaturesBiomeModifier(
		HolderSet<Biome> biomes,
		HolderSet<PlacedFeature> features,
		GenerationStep.Decoration step) implements BiomeModifier {
	
	@Override
	public void modify(Holder<Biome> biome, Phase phase, ModifiableBiomeInfo.BiomeInfo.Builder builder) {
		//WorldgenRevisited.LOGGER.error("[WorldgenRevisited] DynamicAddFeaturesBiomeModifier JUST ENTERED");
		List<ResourceLocation> unique_resources_to_add = (List<ResourceLocation>) (Object) List.of((new HashSet(JsonConfigFeatures.RESOURCES_TO_ADD)).toArray());

		List<Holder<PlacedFeature>> newFeaturesList = new ArrayList<Holder<PlacedFeature>>();

		Registry<PlacedFeature> placedFeaturesRegistry2 = RegistryUtils.getRegistry(Registries.PLACED_FEATURE);
		for(ResourceLocation key: placedFeaturesRegistry2.keySet()) {
			if(key.toString().toLowerCase().contains("iron")) {
				//Why is the key worldgenrevisited:d_feature/ore_iron_small?
				//And why can't I look for it in my registries?
				//WorldgenRevisited.LOGGER.error("[WorldgenRevisited] DynamicAddFeaturesBiomeModifier keycheck -> " + key + ", " + placedFeaturesRegistry2.get(key));
			}
		}
		
		for(ResourceLocation requestedResource : unique_resources_to_add) {
			//WorldgenRevisited.LOGGER.error("[WorldgenRevisited] DynamicAddFeaturesBiomeModifier 0, " + requestedResource);

			Registry<PlacedFeature> placedFeaturesRegistry = RegistryUtils.getRegistry(Registries.PLACED_FEATURE);
			//WorldgenRevisited.LOGGER.error("[WorldgenRevisited] DynamicAddFeaturesBiomeModifier 1, " + requestedResource + " -> " + placedFeaturesRegistry.get(requestedResource));
			newFeaturesList.add(placedFeaturesRegistry.wrapAsHolder(placedFeaturesRegistry.get(requestedResource)));
			//WorldgenRevisited.LOGGER.error("[WorldgenRevisited] DynamicAddFeaturesBiomeModifier 2 " + placedFeaturesRegistry.wrapAsHolder(placedFeaturesRegistry.get(requestedResource)));
		}
		
		//WorldgenRevisited.LOGGER.debug("[WorldgenRevisited] DynamicAddFeaturesBiomeModifier");
		if (phase == BiomeModifier.Phase.ADD /*&& this.biomes.contains(biome)*/ && biome.is(BiomeTags.IS_OVERWORLD)) {
			BiomeGenerationSettingsBuilder generationSettings = builder.getGenerationSettings();
			newFeaturesList.forEach(holder -> generationSettings.addFeature(Decoration.UNDERGROUND_ORES, holder));
		}
	}

	@Override
	public Codec<? extends BiomeModifier> codec() {
		return InitBiomeModifiers.BM_ADD_FEATURES.get();
	}
	
}