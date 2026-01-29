package wftech.caveoverhaul.mixins;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.*;
import net.minecraft.world.level.levelgen.blending.Blender;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import wftech.caveoverhaul.Config;
import wftech.caveoverhaul.WorldGenUtils;
import wftech.caveoverhaul.carvertypes.NoisetypeDomainWarp;
import wftech.caveoverhaul.carvertypes.rivers.*;
import wftech.caveoverhaul.utils.Globals;
import wftech.caveoverhaul.utils.IMixinHelperNoiseChunk;
import wftech.caveoverhaul.utils.NoiseChunkMixinUtils;

@Mixin(NoiseChunk.class)
public class NoiseChunkMixin implements IMixinHelperNoiseChunk {

    @Unique
    private NoiseGeneratorSettings NGS;

	@Inject(method="<init>(ILnet/minecraft/world/level/levelgen/RandomState;IILnet/minecraft/world/level/levelgen/NoiseSettings;Lnet/minecraft/world/level/levelgen/DensityFunctions$BeardifierOrMarker;Lnet/minecraft/world/level/levelgen/NoiseGeneratorSettings;Lnet/minecraft/world/level/levelgen/Aquifer$FluidPicker;Lnet/minecraft/world/level/levelgen/blending/Blender;)V",
			at=@At("RETURN"))
	private void constructorMixin(int int1, RandomState randomstate, int int2, int int3, NoiseSettings noiseSettings,
								  DensityFunctions.BeardifierOrMarker beardifier, NoiseGeneratorSettings noiseGenSettings, Aquifer.FluidPicker fluidPicker,
								  Blender blender, CallbackInfo ci){

		this.wFCaveOverhaul_Fork$setNGS(noiseGenSettings);
		this.wFCaveOverhaul_Fork$setNS(noiseSettings);

	}

	@Inject(method="getInterpolatedState()Lnet/minecraft/world/level/block/state/BlockState;", at=@At("RETURN"), cancellable=true)
	private void getInterpolatedStateMixin(CallbackInfoReturnable<BlockState> cir) {

		NoiseChunk thisChunk = (NoiseChunk) (Object) this;

		//init layers
		int minY = this.wFCaveOverhaul_Fork$getNGS().noiseSettings().minY();
		Globals.setMinY(minY);
		NoisetypeDomainWarp.init(minY);

		//boolean isLikelyOverworld = WorldGenUtils.checkIfLikelyOverworld(((NoiseChunkAccessor) this).getNoiseSettings());
		boolean isLikelyOverworld = WorldGenUtils.checkIfLikelyOverworld(this.wFCaveOverhaul_Fork$getNGS());
		if(!isLikelyOverworld) {
			return;
		}

		int x = thisChunk.blockX();
		int y = thisChunk.blockY();
		int z = thisChunk.blockZ();

		int topY = thisChunk.preliminarySurfaceLevel(x, z);
		topY = topY - 8;

		if(y >= topY) {
			return;
		}

		/*
		 * RED_STAINED_GLASS = lava
		 * GRAY_STAINED_GLASS = water
		 * YELLOW_STAINED_GLASS = stone
		 * BLACK_STAINED_GLASS = air above rivers
		 */

		/*
		Patch 1.3.2 - aquifer patch
		 */
		Block original_block_chosen;
		if (cir.getReturnValue() != null) {
			original_block_chosen = cir.getReturnValue().getBlock();
		} else {
			original_block_chosen = Blocks.STONE;
		}


		if (original_block_chosen == Blocks.LAVA && y <= (minY + 9)){
			return;
		} else if (original_block_chosen == Blocks.AIR && y <= (minY + 9)){
			return;
		}


		if (Config.getBoolSetting(Config.KEY_USE_AQUIFER_PATCH) && (original_block_chosen == Blocks.WATER || original_block_chosen == Blocks.LAVA)) {
			if (NoiseChunkMixinUtils.shouldSetToAir(x, y - 1, z)) {
				cir.setReturnValue(Blocks.STONE.defaultBlockState());
				cir.cancel();
			} else if (NoiseChunkMixinUtils.shouldSetToAir(x - 1, y, z)) {
				cir.setReturnValue(Blocks.STONE.defaultBlockState());
				cir.cancel();
			} else if (NoiseChunkMixinUtils.shouldSetToAir(x + 1, y, z)) {
				cir.setReturnValue(Blocks.STONE.defaultBlockState());
				cir.cancel();
			} else if (NoiseChunkMixinUtils.shouldSetToAir(x, y, z - 1)) {
				cir.setReturnValue(Blocks.STONE.defaultBlockState());
				cir.cancel();
			} else if (NoiseChunkMixinUtils.shouldSetToAir(x, y, z + 1)) {
				cir.setReturnValue(Blocks.STONE.defaultBlockState());
				cir.cancel();
			}
		}

		NURDynamicLayer riverLayer = NoiseChunkMixinUtils.getRiverLayer(x, y, z);
		BlockState preferredState = null;
		if (riverLayer != null) {
			preferredState = riverLayer.getFluidBlock().defaultBlockState();
		} else if (NoiseChunkMixinUtils.shouldSetToStone(x, y, z)) {
			preferredState = Blocks.STONE.defaultBlockState();
		} else if (NoiseChunkMixinUtils.shouldSetToAir(x, y, z)) {
			preferredState = Blocks.AIR.defaultBlockState();
		}

		if (preferredState != null) {
			Globals.init();
            int y_offset = (int) Config.getFloatSetting(Config.KEY_LAVA_OFFSET);
			if (preferredState.isAir() && y <= Globals.getMinY() + y_offset) {
				preferredState = Blocks.LAVA.defaultBlockState();
			}
			cir.setReturnValue(preferredState);
			cir.cancel();
		}
	}

	/*
	For adding variables to NoiseChunk
	 */

	@Override
	public void wFCaveOverhaul_Fork$setNGS(NoiseGeneratorSettings NGS) {
		this.NGS = NGS;
	}

	@Override
	public NoiseGeneratorSettings wFCaveOverhaul_Fork$getNGS() {
		return this.NGS;
	}

	@Override
	public void wFCaveOverhaul_Fork$setNS(NoiseSettings NS) {
        // private DedicatedServer server;
    }
}