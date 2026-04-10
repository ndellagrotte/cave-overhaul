package wftech.caveoverhaul.mixins;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.ScatteredOreFeature;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import wftech.caveoverhaul.Config;
import wftech.caveoverhaul.utils.OreReplacementUtil;

@Mixin(ScatteredOreFeature.class)
public class ScatteredOreFeatureMixin {

    @WrapOperation(
            method = "place",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/level/WorldGenLevel;setBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;I)Z")
    )
    private boolean wrapScatteredOreSetBlock(
            WorldGenLevel level, BlockPos pos, BlockState state, int flags,
            Operation<Boolean> original
    ) {
        if (!Config.getBoolSetting(Config.KEY_ORE_AIR_EXPOSURE_ONLY)) {
            return original.call(level, pos, state, flags);
        }

        BlockState result = OreReplacementUtil.maybeDowngradeOre(state, pos, level::getBlockState);

        if (result == null) {
            return false;
        }

        return original.call(level, pos, result, flags);
    }
}
