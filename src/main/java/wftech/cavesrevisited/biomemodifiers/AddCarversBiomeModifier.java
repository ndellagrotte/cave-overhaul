package wftech.cavesrevisited.biomemodifiers;

import com.mojang.serialization.Codec;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.GenerationStep.Carving;
import net.minecraft.world.level.levelgen.carver.ConfiguredWorldCarver;
import net.minecraftforge.common.world.BiomeGenerationSettingsBuilder;
import net.minecraftforge.common.world.BiomeModifier;
import net.minecraftforge.common.world.ModifiableBiomeInfo;

public record AddCarversBiomeModifier(
		HolderSet<Biome> biomes,
		HolderSet<ConfiguredWorldCarver<?>> carvers,
		GenerationStep.Decoration step) implements BiomeModifier {		
	
	@Override
	public void modify(Holder<Biome> biome, Phase phase, ModifiableBiomeInfo.BiomeInfo.Builder builder) {
		if (phase == BiomeModifier.Phase.ADD && this.biomes.contains(biome)) {
			BiomeGenerationSettingsBuilder generationSettings = builder.getGenerationSettings();
			this.carvers.forEach(holder -> generationSettings.addCarver(Carving.AIR, (Holder<ConfiguredWorldCarver<?>>)holder));
		}
	}

	@Override
	public Codec<? extends BiomeModifier> codec() {
		return InitBiomeModifiers.BM_ADD_CARVERS.get();
	}
	
}