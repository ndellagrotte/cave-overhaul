package wftech.worldgenrevisited.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour.BlockStateBase;
import wftech.worldgenrevisited.Config;


@Mixin(targets="net.minecraft.world.level.block.state.BlockBehaviour$BlockStateBase")
public class BlockStateBaseMixin {

	//Hacky fix. I can't figure out how to make tags dynamic at this point in time, so I'm temporarily
	//doing a mixin as an alternative. If I figure this out later, I'll remove the mixin in favor of a
	//VirtualPackResources solution.
	
	//net/minecraft/world/level/block/state/BlockBehaviour$BlockStateBase
	//boolean net.minecraft.world.level.block.state.BlockBehaviour.BlockStateBase.is(TagKey<Block> p_204337_)
	@Inject(method = "is(Lnet/minecraft/tags/TagKey;)Z", 
			at = @At("RETURN"), remap=true, cancellable = true)
	private void injectIs(TagKey<Block> tagKey, CallbackInfoReturnable<Boolean> cir) {
		BlockStateBase stateBase = (BlockStateBase) (Object) this;
		if(Config.ENABLE_DEEPSLATE_ORES_WHEN_DEEPSLATE_IS_DISABLED.get() && stateBase.getBlock() == Blocks.STONE && tagKey.equals(BlockTags.DEEPSLATE_ORE_REPLACEABLES) && !Config.ENABLE_DEEPSLATE.get()) {
			cir.setReturnValue(true);
		}
	}
}
