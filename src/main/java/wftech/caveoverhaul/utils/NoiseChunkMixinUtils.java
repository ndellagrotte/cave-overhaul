package wftech.caveoverhaul.utils;

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
				|| NCLayerHolder.INSTANCE.shouldCarve(x, y, z);
	}
}
//	public static boolean shouldSetToAirRivers(int x, int y, int z) {
//		return NURLayerHolder.INSTANCE.shouldSetToAirRivers(x, y, z);
//		//return ThreadLocalNURLayerHolderManager.getCurrentHolder().shouldSetToAirRivers(topY, x, y, z);
//	}
