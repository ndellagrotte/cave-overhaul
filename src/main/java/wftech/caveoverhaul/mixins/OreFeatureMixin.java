package wftech.caveoverhaul.mixins;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.levelgen.feature.OreFeature;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import wftech.caveoverhaul.Config;
import wftech.caveoverhaul.utils.OreReplacementUtil;

@Mixin(OreFeature.class)
public class OreFeatureMixin {

    @WrapOperation(
            method = "doPlace",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/level/chunk/LevelChunkSection;setBlockState(IIILnet/minecraft/world/level/block/state/BlockState;Z)Lnet/minecraft/world/level/block/state/BlockState;")
    )
    private BlockState wrapOreSetBlockState(
            LevelChunkSection section, int localX, int localY, int localZ, BlockState state, boolean lock,
            Operation<BlockState> original,
            @Local(argsOnly = true, ordinal = 0) WorldGenLevel level,
            @Local BlockPos.MutableBlockPos mutablePos
    ) {
        if (!Config.getBoolSetting(Config.KEY_ORE_AIR_EXPOSURE_ONLY)) {
            return original.call(section, localX, localY, localZ, state, lock);
        }

        BlockState result = OreReplacementUtil.maybeDowngradeOre(state, mutablePos, level::getBlockState);

        if (result == null) {
            // Don't place — return current state as if nothing changed
            return section.getBlockState(localX, localY, localZ);
        }

        return original.call(section, localX, localY, localZ, result, lock);
    }
}
