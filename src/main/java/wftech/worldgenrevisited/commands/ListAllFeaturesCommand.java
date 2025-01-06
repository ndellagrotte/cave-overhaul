package wftech.worldgenrevisited.commands;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

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
import net.minecraft.world.level.levelgen.carver.ConfiguredWorldCarver;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeGenerationSettings;
import net.minecraftforge.fml.util.thread.EffectiveSide;
import net.minecraftforge.server.ServerLifecycleHooks;
import wftech.worldgenrevisited.WorldgenRevisited;
import wftech.worldgenrevisited.utils.LazyLoadingSafetyWrapper;

public class ListAllFeaturesCommand {
    @SuppressWarnings("unchecked")
	public static void register(CommandDispatcher<CommandSourceStack> commandContext) {
        commandContext.register(
        		(LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal("listallfeatures").requires(p_138414_ -> p_138414_.hasPermission(2))
                		.executes(p_138412_ -> listAllFeaturesNoFilter(p_138412_)))
        		.then(
    					(Commands.argument("partialname", MessageArgument.message()).executes(x -> listAllFeaturesWithFilter(x)))
				));
    }
    
    private static int listAllFeaturesWithFilter(CommandContext<CommandSourceStack> p_138412_) {
        
		Component component;
		try {
			component = MessageArgument.getMessage((CommandContext<CommandSourceStack>)p_138412_, "partialname");
			String searchTerm = component.getString().split(" ")[0].toLowerCase();
			
	        Set<Holder<PlacedFeature>> FeatureSet = findRelevantFeatures(searchTerm);
	        
	        CommandHelper.sendSystemMessage(Component.literal("Found " + FeatureSet.size() + " features using search term " + searchTerm), p_138412_.getSource());

	    	int i = 1;
	    	for (Holder<PlacedFeature> feature: FeatureSet) {
	    		CommandHelper.sendSystemMessage(
						Component.literal(i + ". " + feature.unwrapKey().get().location().toString()), p_138412_.getSource());
	    		 i += 1;

	    	}
		} catch (CommandSyntaxException e) {
			e.printStackTrace();
		}

        return 1;
    }
    
    private static int listAllFeaturesNoFilter(CommandContext<CommandSourceStack> p_138412_) {
    	
        Set<Holder<PlacedFeature>> FeatureSet = findRelevantFeatures("");
        
        CommandHelper.sendSystemMessage(Component.literal("Found " + FeatureSet.size() + " features"), p_138412_.getSource());

    	int i = 1;
    	for (Holder<PlacedFeature> feature: FeatureSet) {
    		CommandHelper.sendSystemMessage(
					Component.literal(i + ". " + feature.unwrapKey().get().location().toString()), p_138412_.getSource());
    		 i += 1;

    	}

        return 1;
    }
    
    private static Set<Holder<PlacedFeature>> findRelevantFeatures(String filter) {
    	
		RegistryAccess registries;
		if(EffectiveSide.get().isClient()) {
			Level level = LazyLoadingSafetyWrapper.getClientLevel();
			registries = level.registryAccess();
			
		} else {
			MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
			registries = server.registryAccess();
		}

		Registry<PlacedFeature> featureReg = registries.registryOrThrow(Registries.PLACED_FEATURE);

		Set<Holder<PlacedFeature>> foundFeatures = new HashSet<Holder<PlacedFeature>>();
		
		for(ResourceLocation key: featureReg.keySet()) {
			if(key.toString().toLowerCase().contains(filter)) {
				foundFeatures.add((Holder<PlacedFeature>) (Object) featureReg.wrapAsHolder(featureReg.get(key)));
			}
		}
    	
    	return foundFeatures;
    }
    
}
