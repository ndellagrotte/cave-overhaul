package wftech.caveoverhaul;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.Aquifer;
import net.minecraft.world.level.levelgen.DensityFunction.FunctionContext;
import net.minecraft.world.level.levelgen.Heightmap.Types;
import net.minecraft.world.level.material.FluidState;
import org.jspecify.annotations.NonNull;
import wftech.caveoverhaul.utils.Globals;
import wftech.caveoverhaul.utils.NoiseChunkMixinUtils;

public class AirOnlyAquifer implements Aquifer {

	private final ChunkAccess level;
	private final boolean exposeToAir;

	public AirOnlyAquifer(ChunkAccess level, boolean exposeToAir) {
		this.level = level;
		this.exposeToAir = exposeToAir;
	}

	private boolean isLiquid(BlockState state) {
		FluidState fluidState = state.getFluidState();
		return fluidState.is(FluidTags.WATER) || fluidState.is(FluidTags.LAVA);
	}

	private boolean sameChunk(int x1, int z1, int x2, int z2) {
		return (x1 >> 4) == (x2 >> 4) && (z1 >> 4) == (z2 >> 4);
	}

	private boolean isNearSurface(int x, int y, int z) {
		if (y >= level.getHeight(Types.WORLD_SURFACE_WG, x, z) - 1) return true;
		if (y >= level.getHeight(Types.WORLD_SURFACE_WG, x + 1, z) - 2) return true;
		if (y >= level.getHeight(Types.WORLD_SURFACE_WG, x - 1, z) - 2) return true;
		if (y >= level.getHeight(Types.WORLD_SURFACE_WG, x, z + 1) - 2) return true;
        return y >= level.getHeight(Types.WORLD_SURFACE_WG, x, z - 1) - 2;
    }

	private boolean hasAdjacentLiquidInSameChunk(int x, int y, int z, BlockPos.MutableBlockPos mutable) {
		// Check cardinal directions
		if (sameChunk(x, z, x - 1, z) && isLiquid(level.getBlockState(mutable.set(x - 1, y, z)))) return true;
		if (sameChunk(x, z, x + 1, z) && isLiquid(level.getBlockState(mutable.set(x + 1, y, z)))) return true;
		if (sameChunk(x, z, x, z - 1) && isLiquid(level.getBlockState(mutable.set(x, y, z - 1)))) return true;
        return sameChunk(x, z, x, z + 1) && isLiquid(level.getBlockState(mutable.set(x, y, z + 1)));
    }

	private boolean hasLiquidAbove(int x, int y, int z, BlockPos.MutableBlockPos mutable) {
		if (isLiquid(level.getBlockState(mutable.set(x, y + 1, z)))) return true;
		if (isLiquid(level.getBlockState(mutable.set(x, y + 2, z)))) return true;
        return isLiquid(level.getBlockState(mutable.set(x, y + 3, z)));
    }

	@Override
	public BlockState computeSubstance(@NonNull FunctionContext ctx, double density) {
		if (level == null) {
			return Blocks.AIR.defaultBlockState();
		}

		int x = ctx.blockX();
		int y = ctx.blockY();
		int z = ctx.blockZ();

		int yOffset = (int) Config.getFloatSetting(Config.KEY_LAVA_OFFSET);
		if (y <= (Globals.minY + yOffset)) {
			return Blocks.LAVA.defaultBlockState();
		}

		BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();
		BlockState state = level.getBlockState(mutable.set(x, y, z));

		if (state.getBlock() == Blocks.LAVA || state.getBlock() == Blocks.WATER) {
			return state;
		}

		if (!exposeToAir && isNearSurface(x, y, z)) {
			return state;
		}

		if (hasAdjacentLiquidInSameChunk(x, y, z, mutable)) {
			return state;
		}

		if (hasLiquidAbove(x, y, z, mutable)) {
			return state;
		}

		if (NoiseChunkMixinUtils.getRiverLayer(x, y, z) != null) {
			return state;
		}
		if (NoiseChunkMixinUtils.getRiverLayer(x, y + 1, z) != null) {
			return state;
		}
		if (NoiseChunkMixinUtils.shouldSetToStone(x, y, z)) {
			return state;
		}

		return Blocks.AIR.defaultBlockState();
	}

	@Override
	public boolean shouldScheduleFluidUpdate() {
		return false;
	}
}