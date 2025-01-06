package wftech.worldgenrevisited.commands;

import java.util.HashSet;
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
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.levelgen.carver.ConfiguredWorldCarver;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeGenerationSettings;
import net.minecraft.world.level.levelgen.GenerationStep.Carving;
import net.minecraftforge.fml.util.thread.EffectiveSide;
import net.minecraftforge.server.ServerLifecycleHooks;
import wftech.worldgenrevisited.WorldgenRevisited;
import wftech.worldgenrevisited.utils.LazyLoadingSafetyWrapper;

public class ListAllCarversCommand {

    @SuppressWarnings("unchecked")
	public static void register(CommandDispatcher<CommandSourceStack> commandContext) {
        commandContext.register(
        		(LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal("listallcarvers").requires(p_138414_ -> p_138414_.hasPermission(2))
                		.executes(p_138412_ -> listAllCarversNoFilter(p_138412_)))
        		.then(
    					(Commands.argument("partialname", MessageArgument.message()).executes(x -> listAllCarversWithFilter(x)))
				));
    }
    
    private static int listAllCarversWithFilter(CommandContext<CommandSourceStack> p_138412_) {
        
		Component component;
		try {
			component = MessageArgument.getMessage((CommandContext<CommandSourceStack>)p_138412_, "partialname");
			String searchTerm = component.getString().split(" ")[0].toLowerCase();
			
	        Set<Holder<ConfiguredWorldCarver<?>>> carverSet = findRelevantCarvers(searchTerm);
	        
	        CommandHelper.sendSystemMessage(Component.literal("Found " + carverSet.size() + " carvers using search term " + searchTerm), p_138412_.getSource());

	    	int i = 1;
	    	for (Holder<ConfiguredWorldCarver<?>> carver: carverSet) {
	    		CommandHelper.sendSystemMessage(
						Component.literal(i + ". " + carver.unwrapKey().get().location().toString()), p_138412_.getSource());
	    		 i += 1;

	    	}
		} catch (CommandSyntaxException e) {
			e.printStackTrace();
		}

        return 1;
    }
    
    private static int listAllCarversNoFilter(CommandContext<CommandSourceStack> p_138412_) {
    	
        Set<Holder<ConfiguredWorldCarver<?>>> carverSet = findRelevantCarvers("");
        
        CommandHelper.sendSystemMessage(Component.literal("Found " + carverSet.size() + " carvers"), p_138412_.getSource());

    	int i = 1;
    	
    	for (Holder<ConfiguredWorldCarver<?>> carver: carverSet) {
    		CommandHelper.sendSystemMessage(
					Component.literal(i + ". " + carver.unwrapKey().get().location().toString()), p_138412_.getSource());
    		 i += 1;

    	}

        return 1;
    }
    
    private static Set<Holder<ConfiguredWorldCarver<?>>> findRelevantCarvers(String filter) {

    	
		RegistryAccess registries;

    	
		if(EffectiveSide.get().isClient()) {    	
			Level level = LazyLoadingSafetyWrapper.getClientLevel();    	
			registries = level.registryAccess();    	
			
		} else {    	
			MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
			registries = server.registryAccess();
		}
    	
		Registry<ConfiguredWorldCarver> carverReg = registries.registryOrThrow(Registries.CONFIGURED_CARVER);    	

		Set<Holder<ConfiguredWorldCarver<?>>> foundCarvers = new HashSet<Holder<ConfiguredWorldCarver<?>>>();    	
		
		for(ResourceLocation key: carverReg.keySet()) {
			if(key.toString().toLowerCase().contains(filter)) {
				foundCarvers.add((Holder<ConfiguredWorldCarver<?>>) (Object) carverReg.wrapAsHolder(carverReg.get(key)));
			}
		}
    	
    	return foundCarvers;
    }
    
}
