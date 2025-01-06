package wftech.worldgenrevisited.biomemodifiers;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import com.mojang.serialization.Codec;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
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
import wftech.worldgenrevisited.WorldgenRevisited;
import wftech.worldgenrevisited.InitConfigFeatures;
import wftech.worldgenrevisited.utils.RegistryUtils;
import wftech.worldgenrevisited.virtualpack.AddPackFindersEventWatcher;
import wftech.worldgenrevisited.virtualpack.JsonConfigFeatures;

public record DynamicRemoveFeaturesBiomeModifier(
		HolderSet<Biome> biomes,
		HolderSet<PlacedFeature> features,
		GenerationStep.Decoration step) implements BiomeModifier {		

	@Override
	public void modify(Holder<Biome> biome, Phase phase, ModifiableBiomeInfo.BiomeInfo.Builder builder) {
		List<Holder<PlacedFeature>> requestedFeaturesList = new ArrayList<Holder<PlacedFeature>>();
		//WorldgenRevisited.LOGGER.error("[WorldgenRevisited] DynamicRemoveFeaturesBiomeModifier JUST ENTERED");
		List<ResourceLocation> unique_resources_to_delete = (List<ResourceLocation>) (Object) List.of((new HashSet(JsonConfigFeatures.RESOURCES_TO_DELETE)).toArray());
		
		for(ResourceLocation requestedResource : unique_resources_to_delete) {
			//WorldgenRevisited.LOGGER.error("[WorldgenRevisited] DynamicRemoveFeaturesBiomeModifier 0, " + requestedResource);

			Registry<PlacedFeature> placedFeaturesRegistry = RegistryUtils.getRegistry(Registries.PLACED_FEATURE);
			//WorldgenRevisited.LOGGER.error("[WorldgenRevisited] DynamicRemoveFeaturesBiomeModifier 1 " + placedFeaturesRegistry.get(requestedResource));
			requestedFeaturesList.add(placedFeaturesRegistry.wrapAsHolder(placedFeaturesRegistry.get(requestedResource)));
			//WorldgenRevisited.LOGGER.error("[WorldgenRevisited] DynamicRemoveFeaturesBiomeModifier 2 " + placedFeaturesRegistry.wrapAsHolder(placedFeaturesRegistry.get(requestedResource)));
		}
		
		//WorldgenRevisited.LOGGER.debug("[WorldgenRevisited] DynamicRemoveFeaturesBiomeModifier");
		if (phase == BiomeModifier.Phase.REMOVE /*&& this.biomes.contains(biome)*/ && biome.is(BiomeTags.IS_OVERWORLD)) {
			BiomeGenerationSettingsBuilder generationSettings = builder.getGenerationSettings();
			
			for(Decoration stage: Decoration.values()) {
			
				List<Holder<PlacedFeature>> registeredFeatures = generationSettings.getFeatures(stage);
				registeredFeatures.removeIf(registeredFeature -> requestedFeaturesList.contains(registeredFeature));
			}
		}
	}

	@Override
	public Codec<? extends BiomeModifier> codec() {
		return InitBiomeModifiers.BM_REMOVE_FEATURES.get();
	}
}
