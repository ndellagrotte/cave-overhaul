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
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeGenerationSettings;
import net.minecraftforge.fml.util.thread.EffectiveSide;
import net.minecraftforge.server.ServerLifecycleHooks;
import wftech.worldgenrevisited.utils.LazyLoadingSafetyWrapper;

public class CreateFeatureFileCommand {

    @SuppressWarnings("unchecked")
	public static void register(CommandDispatcher<CommandSourceStack> commandContext) {
        commandContext.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal("createfeaturefile")
        		.requires(p_138414_ -> p_138414_.hasPermission(2)))
        		.then(Commands.argument("name", MessageArgument.message()).executes(p_138412_ -> {
        			
        
			Component component = MessageArgument.getMessage((CommandContext<CommandSourceStack>)p_138412_, "partialname");
			String whatever = component.getString().split(" ")[0].toLowerCase();
			
            Set<Holder<PlacedFeature>> featureSet = findRelevantFeatures(whatever);
            
        	CommandHelper.sendSystemMessage(Component.literal("Found " + featureSet.size() + " features using search term " + whatever), p_138412_.getSource());

        	int i = 1;
        	for (Holder<PlacedFeature> feature: featureSet) {
        		CommandHelper.sendSystemMessage(
						Component.literal(i + ". " + feature.unwrapKey().get().location().toString()), p_138412_.getSource());
        		 i += 1;

        	}

            return 1;
        })));
    }
    
    private static Set<Holder<PlacedFeature>> findRelevantFeatures(String featureNameRequested) {
    	
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
					if (featureNameRequested.equals(feature.value().toString())) {
						foundFeatures.add(feature);
						//Temp below this line
						//

						
						List<PlacementModifier> placements = feature.value().placement();
						//Temp above this line
						
					}
				}
			}
		}
    	
    	return foundFeatures;
    }
    
}
