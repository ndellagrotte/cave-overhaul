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
	 * Optimized check for adjacent air blocks. Gets instances once and checks all neighbors.
	 * Returns true as soon as any neighbor is air (early exit).
	 */
	public static boolean hasAdjacentAir(int x, int y, int z) {
		NURLayerHolder riverHolder = NURLayerHolder.getInstance();
		NCLayerHolder caveHolder = NCLayerHolder.getInstance();

		// Check each neighbor with early exit
		if (shouldSetToAirFast(riverHolder, caveHolder, x, y - 1, z)) return true;
		if (shouldSetToAirFast(riverHolder, caveHolder, x - 1, y, z)) return true;
		if (shouldSetToAirFast(riverHolder, caveHolder, x + 1, y, z)) return true;
		if (shouldSetToAirFast(riverHolder, caveHolder, x, y, z - 1)) return true;
		return shouldSetToAirFast(riverHolder, caveHolder, x, y, z + 1);
	}

	/**
	 * Fast air check using pre-fetched holder instances.
	 */
	private static boolean shouldSetToAirFast(NURLayerHolder riverHolder, NCLayerHolder caveHolder, int x, int y, int z) {
		return riverHolder.shouldSetToAirRivers(x, y, z) || caveHolder.shouldCarve(x, y, z);
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

		// Check for stone (river support)
		if (riverHolder.shouldSetToStone(x, y, z)) {
			return Blocks.STONE.defaultBlockState();
		}

		// Check for air (river air or cave carving)
		if (riverHolder.shouldSetToAirRivers(x, y, z) || caveHolder.shouldCarve(x, y, z)) {
			return Blocks.AIR.defaultBlockState();
		}

		return null;
	}
}
