package wftech.worldgenrevisited.commands;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.MessageArgument;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;
import net.minecraft.world.level.levelgen.placement.PlacementModifierType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeGenerationSettings;
import net.minecraftforge.fml.util.thread.EffectiveSide;
import net.minecraftforge.server.ServerLifecycleHooks;
import wftech.worldgenrevisited.utils.LazyLoadingSafetyWrapper;

public class ShowFeatureDetailsCommand {

    @SuppressWarnings("unchecked")
	public static void register(CommandDispatcher<CommandSourceStack> commandContext) {

        commandContext.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal("showfeaturedetails")
        		.requires(p_138414_ -> p_138414_.hasPermission(2)))
        		.then(Commands.argument("partialname", MessageArgument.message()).executes(p_138412_ -> {
        			
			Component component = MessageArgument.getMessage((CommandContext<CommandSourceStack>)p_138412_, "partialname");
			String searchTerm = component.getString().split(" ")[0].toLowerCase();
			
            Set<Holder<PlacedFeature>> featureSet = findRelevantFeatures(searchTerm);
            
            CommandHelper.sendSystemMessage(Component.literal("Found " + featureSet.size() + " features using regex " + searchTerm), p_138412_.getSource());

        	int i = 1;
        	for (Holder<PlacedFeature> feature: featureSet) {
        		
        		
        		List<PlacementModifier> placementModifiers = feature.value().placement();
        		
        		CommandHelper.sendSystemMessage(
						Component.literal(i + ". " + feature.unwrapKey().get().location().toString()), p_138412_.getSource());
        		
        		for(PlacementModifier modifier: placementModifiers) {
					PlacementModifierType<?> type = modifier.type();

					CommandHelper.sendSystemMessage(
							Component.literal(
									"Modifier: " + modifier.toString()
									), p_138412_.getSource());
	        		
        		}
    			i += 1;

        	}

            return 1;
        })));
    }
    
    private static Set<Holder<PlacedFeature>> findRelevantFeatures(String regexPattern) {
    	
    	Pattern pattern = Pattern.compile(regexPattern);

		RegistryAccess registries;
		if(EffectiveSide.get().isClient()) {
			Level level = LazyLoadingSafetyWrapper.getClientLevel();
			registries = level.registryAccess();
			
		} else {
			MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
			registries = server.registryAccess();
		}

		Registry<Biome> biomeReg = registries.registryOrThrow(Registries.BIOME);

		Set<Holder<PlacedFeature>> foundFeatures = new HashSet<Holder<PlacedFeature>>();
		
		for(ResourceLocation key: biomeReg.keySet()) {
			Biome biome = biomeReg.get(key);
			BiomeGenerationSettings bgs = biome.getGenerationSettings();
			
			for(HolderSet<PlacedFeature> featureSet: bgs.features()) {
				for(int i = 0; i < featureSet.size(); i++) {
					Holder<PlacedFeature> feature = featureSet.get(i);
					if (pattern.matcher(feature.value().toString()).find()) {
						foundFeatures.add(feature);

						PlacementModifier modifier = feature.value().placement().get(0);
						PlacementModifierType<?> type = modifier.type();

						if(type == PlacementModifierType.RARITY_FILTER) {

							//HECK
							
						} else if (type == PlacementModifierType.COUNT) {
							
						}
						
					}
				}
			}
		}
    	
    	return foundFeatures;
    }
    
}
