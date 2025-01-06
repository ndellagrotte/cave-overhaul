package wftech.cavesrevisited;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.Aquifer;
import net.minecraft.world.level.levelgen.DensityFunction.FunctionContext;

public class AirOnlyAquifer implements Aquifer {

	protected ChunkAccess level = null;
	
	public AirOnlyAquifer(ChunkAccess level) {
		this.level = level;
	}
	
	public boolean isLiquid(BlockState state) {
		return state.getBlock() == Blocks.LAVA || state.getBlock() == Blocks.WATER;
	}

	@Override
	public BlockState computeSubstance(FunctionContext ctx, double p_208159_) {
		if(this.level == null) {
			return Blocks.CAVE_AIR.defaultBlockState();
		}
		
		BlockState state = this.level.getBlockState(new BlockPos(ctx.blockX(), ctx.blockY(), ctx.blockZ()));
		if(state.getBlock() == Blocks.LAVA || state.getBlock() == Blocks.WATER) {
			return state;
		}

		BlockState state_n = this.level.getBlockState(new BlockPos((ctx.blockX() % 16) + 1 > 15 ? ctx.blockX() : ctx.blockX() + 1, ctx.blockY(), ctx.blockZ()));
		BlockState state_e = this.level.getBlockState(new BlockPos((ctx.blockX() % 16) - 1 < 0 ? ctx.blockX() : ctx.blockX() - 1, ctx.blockY(), ctx.blockZ()));
		BlockState state_w = this.level.getBlockState(new BlockPos(ctx.blockX(), ctx.blockY(), (ctx.blockZ() % 16) + 1 > 15 ? ctx.blockZ() : ctx.blockZ() + 1));
		BlockState state_s = this.level.getBlockState(new BlockPos(ctx.blockX(), ctx.blockY(), (ctx.blockZ() % 16) - 1 < 0 ? ctx.blockZ() : ctx.blockZ() - 1));
		BlockState state_u = this.level.getBlockState(new BlockPos(ctx.blockX(), ctx.blockY() + 1, ctx.blockZ()));
		if(this.isLiquid(state_n) || this.isLiquid(state_s) || this.isLiquid(state_e) || this.isLiquid(state_w) || this.isLiquid(state_u)) {
			return state;
		}
		
		return Blocks.CAVE_AIR.defaultBlockState();
	}

	@Override
	public boolean shouldScheduleFluidUpdate() {
		return false;
	}

}
