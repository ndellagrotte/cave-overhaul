package wftech.worldgenrevisited.commands;

import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameRules;
import net.minecraftforge.fml.util.thread.EffectiveSide;
import net.minecraftforge.server.ServerLifecycleHooks;
import wftech.worldgenrevisited.utils.LazyLoadingSafetyWrapper;

public class CommandHelper {

	public static void sendSystemMessage(Component component, CommandSourceStack cmd) {

		/*
		 * If console sends it, it gets sent to ops but not console
		 * If op sends it, it gets sent to ops and console
		 */
    	if(EffectiveSide.get().isServer()) {
    		ServerLifecycleHooks.getCurrentServer().sendSystemMessage(component);
	        if (ServerLifecycleHooks.getCurrentServer().getGameRules().getBoolean(GameRules.RULE_SENDCOMMANDFEEDBACK)) {
	            for (ServerPlayer serverplayer : ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayers()) {
	                if (!ServerLifecycleHooks.getCurrentServer().getPlayerList().isOp(serverplayer.getGameProfile())) continue;
	                serverplayer.sendSystemMessage(component);
	            }
	        }
	        //if (cmd.source != ServerLifecycleHooks.getCurrentServer() && ServerLifecycleHooks.getCurrentServer().getGameRules().getBoolean(GameRules.RULE_LOGADMINCOMMANDS)) {
	        	
	        //}
    	} else {
        	LazyLoadingSafetyWrapper.sendLocalMessage(component);
    	}
    	
	}
}
