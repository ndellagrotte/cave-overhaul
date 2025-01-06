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
import wftech.worldgenrevisited.virtualpack.JsonConfigCarvers;

public record RemoveCarversBiomeModifier(
		HolderSet<Biome> biomes,
		HolderSet<ConfiguredWorldCarver<?>> carvers,
		GenerationStep.Decoration step) implements BiomeModifier {		

	@Override
	public void modify(Holder<Biome> biome, Phase phase, ModifiableBiomeInfo.BiomeInfo.Builder builder) {
		/*
		if (phase == BiomeModifier.Phase.REMOVE && this.biomes.contains(biome)) {
			BiomeGenerationSettingsBuilder generationSettings = builder.getGenerationSettings();
			List<Holder<ConfiguredWorldCarver<?>>> registeredCarvers = generationSettings.getCarvers(Carving.AIR);
			registeredCarvers.removeIf(registeredCarver -> this.carvers.contains(registeredCarver));
		}
		*/
		
		List<ResourceLocation> unique_resources_to_remove = (List<ResourceLocation>) (Object) List.of((new HashSet(JsonConfigCarvers.RESOURCES_TO_DELETE)).toArray());
		List<Holder<ConfiguredWorldCarver>> newCarversList = new ArrayList<Holder<ConfiguredWorldCarver>>();
		
		for(ResourceLocation requestedResource : unique_resources_to_remove) {
			Registry<ConfiguredWorldCarver> configuredCarversRegistry = (Registry<ConfiguredWorldCarver>) (Object) RegistryUtils.getRegistry(Registries.CONFIGURED_CARVER);
			newCarversList.add(configuredCarversRegistry.wrapAsHolder(configuredCarversRegistry.get(requestedResource)));
		}
		
		if (phase == BiomeModifier.Phase.REMOVE /*&& this.biomes.contains(biome)*/ && biome.is(BiomeTags.IS_OVERWORLD)) {
			BiomeGenerationSettingsBuilder generationSettings = builder.getGenerationSettings();
			List<Holder<ConfiguredWorldCarver<?>>> registeredCarvers = generationSettings.getCarvers(Carving.AIR);
			registeredCarvers.removeIf(registeredCarver -> {
				//WorldgenRevisited.LOGGER.info("[WorldgenRevisited] RemoveCarversBiomeModifier Should I remove " + registeredCarver + "? " + newCarversList.contains(registeredCarver));
				return newCarversList.contains(registeredCarver);});
		}
		
		/*
		if (phase == BiomeModifier.Phase.ADD && this.biomes.contains(biome)) {
			BiomeGenerationSettingsBuilder generationSettings = builder.getGenerationSettings();
			for(Holder<ConfiguredWorldCarver<?>> holder: this.carvers()) {
				if(newCarversList.indexOf(newCarversList) == -1) {
					generationSettings.addCarver(Carving.AIR, (Holder<ConfiguredWorldCarver<?>>)holder);
				}
			}
			//this.carvers.forEach(holder -> generationSettings.addCarver(Carving.AIR, (Holder<ConfiguredWorldCarver<?>>)holder));
		}
		*/
		
		if (phase == BiomeModifier.Phase.REMOVE /*&& this.biomes.contains(biome)*/ && biome.is(BiomeTags.IS_OVERWORLD)) {
			BiomeGenerationSettingsBuilder generationSettings = builder.getGenerationSettings();
			List<Holder<ConfiguredWorldCarver<?>>> registeredCarvers = generationSettings.getCarvers(Carving.AIR);
			registeredCarvers.removeIf(registeredCarver -> this.carvers.contains(registeredCarver) && !newCarversList.contains(registeredCarver));
		}
	}

	@Override
	public Codec<? extends BiomeModifier> codec() {
		return InitBiomeModifiers.BM_REMOVE_CARVERS.get();
	}
}
