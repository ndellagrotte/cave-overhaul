package wftech.worldgenrevisited.mixins;

import java.util.NoSuchElementException;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.electronwill.nightconfig.core.AbstractCommentedConfig;
import com.electronwill.nightconfig.core.file.CommentedFileConfig;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.SurfaceRules;
import net.minecraftforge.fml.loading.FMLPaths;
import wftech.worldgenrevisited.WorldgenRevisited;
import wftech.worldgenrevisited.Config;

@Mixin(targets = "net.minecraft.world.level.levelgen.SurfaceRules$BlockRuleSource")
public class SurfaceRulesMixin {
	
	/*
	 * Stupidly ugly, there was a strange issue with .get() or whatever it was. Thus,
	 * the ugly...
	 */
	@ModifyVariable(method = "<init>(Lnet/minecraft/world/level/block/state/BlockState;)V", 
			at = @At("HEAD"), remap=true, ordinal = 0)
	private static BlockState injectMakeStateRule(BlockState blockState) { //CallbackInfoReturnable<BlockState> cir
		
		if(blockState.getBlock() == Blocks.DEEPSLATE) {
    	
			try {
				CommentedFileConfig foundConfig = CommentedFileConfig.of(FMLPaths.CONFIGDIR.get().resolve(WorldgenRevisited.TOML_FILE_NAME));
				foundConfig.load();
				AbstractCommentedConfig cfg = null;
				boolean enable_deepslate = true;
				boolean found_deepslate_trigger = false;
				for(String key: foundConfig.valueMap().keySet()) {
	
					cfg = foundConfig.get(key);
					
					//oof, ow, ouch, right in the code
					for(String key2: cfg.valueMap().keySet()) {
						if(key2.equals(Config.KEY_ENABLE_DEEPSLATE)) {
	
							enable_deepslate = cfg.get(key2);
							found_deepslate_trigger = true;
							
							if(!enable_deepslate) {
								WorldgenRevisited.LOGGER.debug("[WorldgenRevisited] Disabling default worldgen deepslate placements.");
								return Blocks.STONE.defaultBlockState();
							} else {
								WorldgenRevisited.LOGGER.debug("[WorldgenRevisited] Not disabling default worldgen deepslate placements :)");
							}
							break;
						}
					}
				}
				
				if(!found_deepslate_trigger) {
					WorldgenRevisited.LOGGER.error("[WorldgenRevisited] Failed to pre-load worldgenrevisited.toml. If this is your first run or the toml file has been deleted, you can ignore this error.");
				}
				
			} catch (NullPointerException e){
				WorldgenRevisited.LOGGER.error("[WorldgenRevisited] Failed to load config " + WorldgenRevisited.TOML_FILE_NAME + ". Looked for key " + Config.KEY_ENABLE_DEEPSLATE + ". Likely a broken config. Try deleting the config and relaunching your client/server to form a fresh config.");
				e.printStackTrace();
			} catch (NoSuchElementException e) {
	        	WorldgenRevisited.LOGGER.error("WorldgenRevisited config not yet generated! Making a new config.");
			}
		}
		
		return blockState;
	}
}
