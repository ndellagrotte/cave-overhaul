package wftech.caveoverhaul.utils;

import wftech.caveoverhaul.carvertypes.*;
import wftech.caveoverhaul.carvertypes.rivers.*;

public class NoiseChunkMixinUtils {

	public static NURDynamicLayer getRiverLayer(int x, int y, int z) {
		return NURLayerHolder.INSTANCE.getRiverLayerInternal(x, y, z);
	}

	public static boolean shouldSetToStone(int x, int y, int z) {
		return NURLayerHolder.INSTANCE.shouldSetToStone(x, y, z);
	}

	public static boolean shouldSetToAir(int x, int y, int z) {
		if (NURLayerHolder.INSTANCE.shouldSetToAirRivers(x, y, z)) {
			return true;
		}
		return NCLayerHolder.INSTANCE.shouldCarve(x, y, z);
	}
}
//	public static boolean shouldSetToAirRivers(int x, int y, int z) {
//		return NURLayerHolder.INSTANCE.shouldSetToAirRivers(x, y, z);
//		//return ThreadLocalNURLayerHolderManager.getCurrentHolder().shouldSetToAirRivers(topY, x, y, z);
//	}
