package wftech.caveoverhaul;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.world.level.levelgen.*;

public class WorldGenUtils {

	//Store overworld NGS
	private static final List<NoiseGeneratorSettings> OVERWORLD_NGS_CANDIDATES = new ArrayList<>();

	public static void addNGS(NoiseGeneratorSettings NGS){

		OVERWORLD_NGS_CANDIDATES.add(NGS);
	}

	public static boolean checkIfLikelyOverworld(NoiseGeneratorSettings settings) {
		for(NoiseGeneratorSettings candidate: OVERWORLD_NGS_CANDIDATES){
			if (settings == candidate){
				return true;
			}
		}
		return false;

	}
}