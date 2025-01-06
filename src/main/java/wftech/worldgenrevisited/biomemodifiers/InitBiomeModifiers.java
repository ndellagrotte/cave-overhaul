package wftech.worldgenrevisited.biomemodifiers;


import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.carver.ConfiguredWorldCarver;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraftforge.common.world.BiomeModifier;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import wftech.worldgenrevisited.WorldgenRevisited;

//https://forge.gemwire.uk/wiki/Biome_Modifiers
//https://forums.minecraftforge.net/topic/116895-1192-cannot-get-custom-biome-modifier-to-work/
public class InitBiomeModifiers {

	public static void registerDeferred(IEventBus eventBus) {
		BIOME_MODIFIER_SERIALIZERS.register(eventBus);
	}
	
	public static final DeferredRegister<Codec<? extends BiomeModifier>> BIOME_MODIFIER_SERIALIZERS =
            DeferredRegister.create(ForgeRegistries.Keys.BIOME_MODIFIER_SERIALIZERS, WorldgenRevisited.MOD_ID);
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static final RegistryObject<Codec<AddCarversBiomeModifier>> BM_ADD_CARVERS = BIOME_MODIFIER_SERIALIZERS.register("add_carver", 
			() -> RecordCodecBuilder.create(
			        builder -> builder.group(
			            Biome.LIST_CODEC.fieldOf("biomes").forGetter(AddCarversBiomeModifier::biomes), 
			            ConfiguredWorldCarver.LIST_CODEC.fieldOf("carvers").forGetter(AddCarversBiomeModifier::carvers),
			            GenerationStep.Decoration.CODEC.fieldOf("step").forGetter(AddCarversBiomeModifier::step)
			        ).apply((Applicative) builder, AddCarversBiomeModifier::new)));
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static final RegistryObject<Codec<RemoveCarversBiomeModifier>> BM_REMOVE_CARVERS = BIOME_MODIFIER_SERIALIZERS.register("remove_carver", 
			() -> RecordCodecBuilder.create(
			        builder -> builder.group(
			            Biome.LIST_CODEC.fieldOf("biomes").forGetter(RemoveCarversBiomeModifier::biomes), 
			            ConfiguredWorldCarver.LIST_CODEC.fieldOf("carvers").forGetter(RemoveCarversBiomeModifier::carvers),
			            GenerationStep.Decoration.CODEC.fieldOf("step").forGetter(RemoveCarversBiomeModifier::step)
			        ).apply((Applicative) builder, RemoveCarversBiomeModifier::new)));
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static final RegistryObject<Codec<DynamicAddFeaturesBiomeModifier>> BM_ADD_FEATURES = BIOME_MODIFIER_SERIALIZERS.register("dynamic_add_features", 
			() -> RecordCodecBuilder.create(
			        builder -> builder.group(
			            Biome.LIST_CODEC.fieldOf("biomes").forGetter(DynamicAddFeaturesBiomeModifier::biomes), 
			            PlacedFeature.LIST_CODEC.fieldOf("features").forGetter(DynamicAddFeaturesBiomeModifier::features),
			            GenerationStep.Decoration.CODEC.fieldOf("step").forGetter(DynamicAddFeaturesBiomeModifier::step)
			        ).apply((Applicative) builder, DynamicAddFeaturesBiomeModifier::new)));
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static final RegistryObject<Codec<DynamicRemoveFeaturesBiomeModifier>> BM_REMOVE_FEATURES = BIOME_MODIFIER_SERIALIZERS.register("dynamic_remove_features", 
			() -> RecordCodecBuilder.create(
			        builder -> builder.group(
			            Biome.LIST_CODEC.fieldOf("biomes").forGetter(DynamicRemoveFeaturesBiomeModifier::biomes), 
			            PlacedFeature.LIST_CODEC.fieldOf("features").forGetter(DynamicRemoveFeaturesBiomeModifier::features),
			            GenerationStep.Decoration.CODEC.fieldOf("step").forGetter(DynamicRemoveFeaturesBiomeModifier::step)
			        ).apply((Applicative) builder, DynamicRemoveFeaturesBiomeModifier::new)));

}
