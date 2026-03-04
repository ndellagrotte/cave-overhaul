package wftech.caveoverhaul.mixins;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.levelgen.feature.DripstoneUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import wftech.caveoverhaul.carvertypes.rivers.NURLayerHolder;

@Mixin(DripstoneUtils.class)
public class DripstoneUtilsMixin {

	@Inject(method = "isEmptyOrWater(Lnet/minecraft/world/level/LevelAccessor;Lnet/minecraft/core/BlockPos;)Z",
			at = @At("HEAD"), cancellable = true)
	private static void preventInRiverZone(LevelAccessor level, BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
		if (isInNURZone(pos.getX(), pos.getY(), pos.getZ())) {
			cir.setReturnValue(false);
		}
	}

	@Inject(method = "isEmptyOrWaterOrLava(Lnet/minecraft/world/level/LevelAccessor;Lnet/minecraft/core/BlockPos;)Z",
			at = @At("HEAD"), cancellable = true)
	private static void preventInRiverZoneLava(LevelAccessor level, BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
		if (isInNURZone(pos.getX(), pos.getY(), pos.getZ())) {
			cir.setReturnValue(false);
		}
	}

	@Inject(method = "placeDripstoneBlockIfPossible",
			at = @At("HEAD"), cancellable = true)
	private static void preventDripstoneBlockInRiver(LevelAccessor level, BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
		if (isInNURZone(pos.getX(), pos.getY(), pos.getZ())) {
			cir.setReturnValue(false);
		}
	}

	private static boolean isInNURZone(int x, int y, int z) {
		NURLayerHolder holder = NURLayerHolder.getInstance();
		return holder.getRiverLayer(x, y, z) != null || holder.shouldSetToAirRivers(x, y, z);
	}
}
