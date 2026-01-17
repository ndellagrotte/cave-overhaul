package wftech.caveoverhaul.utils;

import wftech.caveoverhaul.carvertypes.*;
import wftech.caveoverhaul.carvertypes.rivers.*;

public class NoiseChunkMixinUtils {

	public static NURDynamicLayer getRiverLayer(int x, int y, int z) {
		return NURLayerHolder.INSTANCE.getRiverLayerInternal(x, y, z);
		//return ThreadLocalNURLayerHolderManager.getCurrentHolder().getRiverLayer(topY, x, y, z);
	}

	public static boolean shouldSetToStone(int x, int y, int z) {
		return NURLayerHolder.INSTANCE.shouldSetToStone(x, y, z);
		//return ThreadLocalNURLayerHolderManager.getCurrentHolder().shouldSetToStone(topY, x, y, z);
	}

	public static boolean shouldSetToAirRivers(int x, int y, int z) {
		return NURLayerHolder.INSTANCE.shouldSetToAirRivers(x, y, z);
		//return ThreadLocalNURLayerHolderManager.getCurrentHolder().shouldSetToAirRivers(topY, x, y, z);
	}

	public static boolean shouldSetToAirCaverns(int x, int y, int z) {
		return NCLayerHolder.INSTANCE.shouldCarve(x, y, z);
		//return ThreadLocalNCLayerHolderManager.getCurrentHolder().shouldCarve(x, y, z);
	}

	public static boolean isAirBlock(int x, int y, int z){
		if(NoiseChunkMixinUtils.shouldSetToAirRivers(x, y, z)) {
			return true;
		} else return NoiseChunkMixinUtils.shouldSetToAirCaverns(x, y, z);
    }

	public static boolean isStoneBlock(int x, int y, int z){
		return NoiseChunkMixinUtils.shouldSetToStone(x, y, z);
	}
}