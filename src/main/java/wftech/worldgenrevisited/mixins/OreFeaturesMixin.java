package wftech.worldgenrevisited.mixins;

import java.util.function.Function;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.OreFeature;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration;
import wftech.worldgenrevisited.Config;

@Mixin(OreFeature.class)
public class OreFeaturesMixin {

	/*public static boolean canPlaceOre(
	 * 		BlockState p_225187_, 
	 * 		Function<BlockPos, BlockState> p_225188_, 
	 * 		RandomSource p_225189_, 
	 * 		OreConfiguration p_225190_, 
	 * 		OreConfiguration.TargetBlockState p_225191_, 
	 * 		BlockPos.MutableBlockPos p_225192_) {
	 */
	//				  canPlaceOre(Lnet/minecraft/world/level/block/state/BlockState;Ljava/util/function/Function;Lnet/minecraft/util/RandomSource;Lnet/minecraft/world/level/levelgen/feature/configurations/OreConfiguration;Lnet/minecraft/world/level/levelgen/feature/configurations/OreConfiguration$TargetBlockState;Lnet/minecraft/core/BlockPos$MutableBlockPos;)Z
	@Inject(method = "canPlaceOre(Lnet/minecraft/world/level/block/state/BlockState;Ljava/util/function/Function;Lnet/minecraft/util/RandomSource;Lnet/minecraft/world/level/levelgen/feature/configurations/OreConfiguration;Lnet/minecraft/world/level/levelgen/feature/configurations/OreConfiguration$TargetBlockState;Lnet/minecraft/core/BlockPos$MutableBlockPos;)Z", 
			at = @At("HEAD"), remap=true, cancellable = true)
	private static void inject(BlockState blockState, Function<BlockPos, BlockState> someFunc, RandomSource randomSource, OreConfiguration oreConfig, OreConfiguration.TargetBlockState targetBlockState, BlockPos.MutableBlockPos blockPos, CallbackInfoReturnable<Boolean> cir) {
		//if (!targetBlockState.target.test(blockState, randomSource)) 
		//if I am above y and looking at deepslate stuff, throw it
		if(Config.ENABLE_DEEPSLATE_ORES_WHEN_DEEPSLATE_IS_DISABLED.get() && Config.ENABLE_DEEPSLATE.get() && targetBlockState.state.getBlock().toString().toLowerCase().replace("_", "").contains("deepslate") && blockPos.getY() > 0) {
			cir.setReturnValue(false);
		}
	}
}
