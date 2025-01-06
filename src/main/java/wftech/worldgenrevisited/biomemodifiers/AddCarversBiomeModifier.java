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
import net.minecraft.world.level.levelgen.carver.ConfiguredWorldCarver;
import net.minecraftforge.common.world.BiomeGenerationSettingsBuilder;
import net.minecraftforge.common.world.BiomeModifier;
import net.minecraftforge.common.world.ModifiableBiomeInfo;
import wftech.worldgenrevisited.WorldgenRevisited;
import wftech.worldgenrevisited.utils.RegistryUtils;
import wftech.worldgenrevisited.virtualpack.AddPackFindersEventWatcher;
import wftech.worldgenrevisited.virtualpack.JsonConfigCarvers;

public record AddCarversBiomeModifier(
		HolderSet<Biome> biomes,
		HolderSet<ConfiguredWorldCarver<?>> carvers,
		GenerationStep.Decoration step) implements BiomeModifier {		
	
	public static List<Holder<ConfiguredWorldCarver>> POSTGEN_ADD_ULTRALARGE_NOISE_FEATURES = new ArrayList<>();
	
	@Override
	public void modify(Holder<Biome> biome, Phase phase, ModifiableBiomeInfo.BiomeInfo.Builder builder) {
		/*
		if (phase == BiomeModifier.Phase.ADD && this.biomes.contains(biome)) {
			BiomeGenerationSettingsBuilder generationSettings = builder.getGenerationSettings();
			this.carvers.forEach(holder -> generationSettings.addCarver(Carving.AIR, (Holder<ConfiguredWorldCarver<?>>)holder));
		}
		*/
		List<Holder<ConfiguredWorldCarver>> addedCarvers = new ArrayList<Holder<ConfiguredWorldCarver>>();
		
		List<ResourceLocation> unique_resources_to_add = (List<ResourceLocation>) (Object) List.of((new HashSet(JsonConfigCarvers.RESOURCES_TO_ADD)).toArray());
		List<Holder<ConfiguredWorldCarver>> newCarversList = new ArrayList<Holder<ConfiguredWorldCarver>>();
		
		for(ResourceLocation requestedResource : unique_resources_to_add) {
			Registry<ConfiguredWorldCarver> configuredCarversRegistry = (Registry<ConfiguredWorldCarver>) (Object) RegistryUtils.getRegistry(Registries.CONFIGURED_CARVER);
			newCarversList.add(configuredCarversRegistry.wrapAsHolder(configuredCarversRegistry.get(requestedResource)));
			POSTGEN_ADD_ULTRALARGE_NOISE_FEATURES.add(configuredCarversRegistry.wrapAsHolder(configuredCarversRegistry.get(requestedResource)));
		}
		
		if (phase == BiomeModifier.Phase.ADD /*&& this.biomes.contains(biome)*/ && biome.is(BiomeTags.IS_OVERWORLD)) {
			BiomeGenerationSettingsBuilder generationSettings = builder.getGenerationSettings();
			newCarversList.forEach(holder -> {
				//WorldgenRevisited.LOGGER.info("[WorldgenRevisited] AddCarversBiomeModifiers Adding " + holder);
				generationSettings.addCarver(Carving.AIR, (Holder<ConfiguredWorldCarver<?>>) (Object) holder);
			});
			addedCarvers.addAll(newCarversList);
		}
		
		/*
		 * Replace this with the 0 = 1.12 retrogen, 1 = 1.16 definition
		 * That means I'll need to work via addPackFindersEtc?
		 * Update removeCarvers because I remove 3 carvers...
		 */
		if (phase == BiomeModifier.Phase.ADD /*&& this.biomes.contains(biome)*/ && biome.is(BiomeTags.IS_OVERWORLD)) {
			BiomeGenerationSettingsBuilder generationSettings = builder.getGenerationSettings();
			for(Holder<ConfiguredWorldCarver<?>> holder: this.carvers()) {
				if(newCarversList.indexOf(newCarversList) == -1) {
					generationSettings.addCarver(Carving.AIR, (Holder<ConfiguredWorldCarver<?>>)holder);
				}
			}
			//this.carvers.forEach(holder -> generationSettings.addCarver(Carving.AIR, (Holder<ConfiguredWorldCarver<?>>)holder));
		}
	}

	@Override
	public Codec<? extends BiomeModifier> codec() {
		return InitBiomeModifiers.BM_ADD_CARVERS.get();
	}
	
}