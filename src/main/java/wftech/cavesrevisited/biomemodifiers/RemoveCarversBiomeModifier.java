package wftech.cavesrevisited.biomemodifiers;

import java.util.List;

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

public record RemoveCarversBiomeModifier(
		HolderSet<Biome> biomes,
		HolderSet<ConfiguredWorldCarver<?>> carvers,
		GenerationStep.Decoration step) implements BiomeModifier {		

	@Override
	public void modify(Holder<Biome> biome, Phase phase, ModifiableBiomeInfo.BiomeInfo.Builder builder) {
		if (phase == BiomeModifier.Phase.REMOVE && this.biomes.contains(biome)) {
			BiomeGenerationSettingsBuilder generationSettings = builder.getGenerationSettings();
			List<Holder<ConfiguredWorldCarver<?>>> registeredCarvers = generationSettings.getCarvers(Carving.AIR);
			registeredCarvers.removeIf(registeredCarver -> this.carvers.contains(registeredCarver));
		}
	}

	@Override
	public Codec<? extends BiomeModifier> codec() {
		return InitBiomeModifiers.BM_REMOVE_CARVERS.get();
	}
}
