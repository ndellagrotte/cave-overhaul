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

	@Unique
	private boolean caveOverhaul$initialized = false;

	@Unique
	private boolean caveOverhaul$isOverworld = false;

	@Unique
	private int caveOverhaul$minY = 0;

	@Unique
	private int caveOverhaul$lavaOffset = 9;

	@Inject(method="<init>(ILnet/minecraft/world/level/levelgen/RandomState;IILnet/minecraft/world/level/levelgen/NoiseSettings;Lnet/minecraft/world/level/levelgen/DensityFunctions$BeardifierOrMarker;Lnet/minecraft/world/level/levelgen/NoiseGeneratorSettings;Lnet/minecraft/world/level/levelgen/Aquifer$FluidPicker;Lnet/minecraft/world/level/levelgen/blending/Blender;)V",
			at=@At("RETURN"))
	private void constructorMixin(int int1, RandomState randomstate, int int2, int int3, NoiseSettings noiseSettings,
								  DensityFunctions.BeardifierOrMarker beardifier, NoiseGeneratorSettings noiseGenSettings, Aquifer.FluidPicker fluidPicker,
								  Blender blender, CallbackInfo ci){

		this.wFCaveOverhaul_Fork$setNGS(noiseGenSettings);
		this.wFCaveOverhaul_Fork$setNS(noiseSettings);

		// Pre-compute values that don't change per-block
		this.caveOverhaul$minY = noiseGenSettings.noiseSettings().minY();
		this.caveOverhaul$isOverworld = WorldGenUtils.checkIfLikelyOverworld(noiseGenSettings);
		this.caveOverhaul$lavaOffset = (int) Config.getFloatSetting(Config.KEY_LAVA_OFFSET);

		// Initialize global state once per chunk
		Globals.setMinY(this.caveOverhaul$minY);
		NoisetypeDomainWarp.init(this.caveOverhaul$minY);
		Globals.init();

		this.caveOverhaul$initialized = true;
	}

	@Inject(method="getInterpolatedState()Lnet/minecraft/world/level/block/state/BlockState;", at=@At("RETURN"), cancellable=true)
	private void getInterpolatedStateMixin(CallbackInfoReturnable<BlockState> cir) {
		// Fast path: skip if not overworld
		if (!this.caveOverhaul$isOverworld) {
			return;
		}

		NoiseChunk thisChunk = (NoiseChunk) (Object) this;

		int x = thisChunk.blockX();
		int y = thisChunk.blockY();
		int z = thisChunk.blockZ();

		// Fast path: skip if above surface
		int topY = thisChunk.preliminarySurfaceLevel(x, z) - 8;
		if (y >= topY) {
			return;
		}

		// Cache minY locally to avoid repeated field access
		int minY = this.caveOverhaul$minY;

		// Fast path: skip bottom of world
		Block originalBlock = cir.getReturnValue() != null ? cir.getReturnValue().getBlock() : Blocks.STONE;
		if (y <= minY + 9 && (originalBlock == Blocks.LAVA || originalBlock == Blocks.AIR)) {
			return;
		}

		// Main cave/river logic
		BlockState preferredState = NoiseChunkMixinUtils.computePreferredState(x, y, z);

		if (preferredState != null) {
			// Replace air with lava at bottom of world (use cached values)
			if (preferredState.isAir() && y <= minY + this.caveOverhaul$lavaOffset) {
				preferredState = Blocks.LAVA.defaultBlockState();
			}
			cir.setReturnValue(preferredState);
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