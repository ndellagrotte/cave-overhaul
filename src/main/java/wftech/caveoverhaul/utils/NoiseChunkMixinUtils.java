package wftech.caveoverhaul.utils;

import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import wftech.caveoverhaul.carvertypes.*;
import wftech.caveoverhaul.carvertypes.rivers.*;

public class NoiseChunkMixinUtils {

	public static NURDynamicLayer getRiverLayer(int x, int y, int z) {
		return NURLayerHolder.getInstance().getRiverLayer(x, y, z);
	}

	public static boolean shouldSetToStone(int x, int y, int z) {
		return NURLayerHolder.getInstance().shouldSetToStone(x, y, z);
	}

	public static boolean shouldSetToAir(int x, int y, int z) {
		return NURLayerHolder.getInstance().shouldSetToAirRivers(x, y, z)
				|| NCLayerHolder.getInstance().shouldCarve(x, y, z);
	}

	/**
	 * Computes the preferred block state for cave/river generation.
	 * Gets holder instances once and performs all checks with them.
	 * Returns null if no change is needed.
	 */
	public static BlockState computePreferredState(int x, int y, int z) {
		NURLayerHolder riverHolder = NURLayerHolder.getInstance();
		NCLayerHolder caveHolder = NCLayerHolder.getInstance();

		// Check for river liquid first
		NURDynamicLayer riverLayer = riverHolder.getRiverLayer(x, y, z);
		if (riverLayer != null) {
			return riverLayer.getFluidBlock().defaultBlockState();
		}

		// Check for river air before stone, so one layer's boundary
		// doesn't place stone that obstructs another layer's river
		boolean isRiverAir = riverHolder.shouldSetToAirRivers(x, y, z);

		// Check for stone (river support) only if no river wants this as air
		if (!isRiverAir && riverHolder.shouldSetToStone(x, y, z)) {
			return Blocks.STONE.defaultBlockState();
		}

		// Check for air (river air or cave carving)
		if (isRiverAir || caveHolder.shouldCarve(x, y, z)) {
			return Blocks.AIR.defaultBlockState();
		}

		return null;
	}
}
