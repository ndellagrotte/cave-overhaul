package wftech.worldgenrevisited.mixins;

import java.util.NoSuchElementException;

import org.apache.commons.lang3.StringUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.electronwill.nightconfig.core.AbstractCommentedConfig;
import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.electronwill.nightconfig.core.utils.ConfigWrapper;

import net.minecraft.data.worldgen.SurfaceRuleData;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.SurfaceRules;
import net.minecraftforge.fml.config.ConfigTracker;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.loading.FMLPaths;
import wftech.worldgenrevisited.WorldgenRevisited;
import wftech.worldgenrevisited.Config;

@Mixin(SurfaceRuleData.class)
public class SurfaceRuleDataMixin {

	
	//private static SurfaceRules.RuleSource makeStateRule(Block p_194811_) {
	@Inject(method = "makeStateRule(Lnet/minecraft/world/level/block/Block;)Lnet/minecraft/world/level/levelgen/SurfaceRules$RuleSource;", 
			at = @At("RETURN"), remap=true, cancellable = true)
	private static void injectMakeStateRule(Block block, CallbackInfoReturnable<SurfaceRules.RuleSource> cir) { //CallbackInfoReturnable<BlockState> cir

		if(block == Blocks.DEEPSLATE) {
	    	
			try {
				CommentedFileConfig foundConfig = CommentedFileConfig.of(FMLPaths.CONFIGDIR.get().resolve(WorldgenRevisited.TOML_FILE_NAME));
				foundConfig.load();
				AbstractCommentedConfig cfg = null;
				boolean enable_deepslate = true;
				boolean found_deepslate_trigger = false; 
				for(String key: foundConfig.valueMap().keySet()) {

					cfg = foundConfig.get(key);
					
					for(String key2: cfg.valueMap().keySet()) {
						if(key2.equals(Config.KEY_ENABLE_DEEPSLATE)) {

							enable_deepslate = cfg.get(key2);
							found_deepslate_trigger = true;
							//WorldgenRevisited.LOGGER.info("[WorldgenRevisited] Found the deepslate key!");
							
							if(!enable_deepslate) {
								//While I'm here...
								WorldgenRevisited.LOGGER.info("[WorldgenRevisited] Disabling the deepslate rule.");
								cir.setReturnValue(SurfaceRules.state(Blocks.STONE.defaultBlockState()));
							} else {

								WorldgenRevisited.LOGGER.info("[WorldgenRevisited] Not disabling the deepslate rule :)");
							}
							break;
						}
					}
					enable_deepslate = cfg.get(Config.KEY_ENABLE_DEEPSLATE);
					
					found_deepslate_trigger = true;
					
					if(!enable_deepslate) {
						//While I'm here...
				    	//BlockTagsProvider btp = new BlockTagsProvider(null);
				    	//btp.tag(BlockTags.DEEPSLATE_ORE_REPLACEABLES).add(Blocks.STONE).add(Blocks.GRANITE).add(Blocks.DIORITE).add(Blocks.ANDESITE);
						cir.setReturnValue(SurfaceRules.state(Blocks.STONE.defaultBlockState()));
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
	}
}
