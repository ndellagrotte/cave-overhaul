package wftech.worldgenrevisited.carvertypes;

import com.mojang.serialization.Codec;

import net.minecraft.world.level.levelgen.carver.CaveCarverConfiguration;
import net.minecraftforge.server.ServerLifecycleHooks;
import wftech.worldgenrevisited.fastnoise.FastNoiseLite;
import wftech.worldgenrevisited.fastnoise.FastNoiseLite.FractalType;
import wftech.worldgenrevisited.fastnoise.FastNoiseLite.NoiseType;

public class NoiseCavernBottomLayer2 extends NoiseCavernBaseFixFromNewCaves {

	public NoiseCavernBottomLayer2(Codec<CaveCarverConfiguration> p_159194_) {
		super(p_159194_);
	}
	
	/*
	 * -64 to 0, doubling up to expand the amount of fun caves near the bottom
	 */
	private float minY = -64;
	private float maxY = 0;

	//Transforms a given y-level noise to the cave-to-be-carved's y floor
	@Override
	protected int getCaveY(float noiseValue) {
		//Original
		//return (int) (noiseValue * (64f));
		return (int) (((maxY - minY) * noiseValue) + minY);
	}
	
	/*
	 * Noise portions down here for easy copy and pasting
	 */
	public static FastNoiseLite yNoise = null;
	public static FastNoiseLite caveSizeNoise = null;
	public static int seedOffset = 3;
	
	private void initYNoise() {

		int seed = (int) ServerLifecycleHooks.getCurrentServer().getWorldData().worldGenOptions().seed(); //(int) this.ctx.randomState().legacyLevelSeed();
		seed += seedOffset;
		
		FastNoiseLite tnoise = new FastNoiseLite();
		tnoise.SetSeed(seed);
		tnoise.SetNoiseType(NoiseType.OpenSimplex2);
		tnoise.SetFrequency(0.01f);
		//tnoise.SetFractalType(FractalType.FBM);
		tnoise.SetFractalType(FractalType.FBm);
		tnoise.SetFractalGain(2.5f);
		tnoise.SetFractalOctaves(2);
		tnoise.SetFractalLacunarity(0.1f);
		
		yNoise = tnoise;
	}
	
	public void initCaveHeightMap() {
		
		int seed = (int) ServerLifecycleHooks.getCurrentServer().getWorldData().worldGenOptions().seed(); //(int) this.ctx.randomState().legacyLevelSeed();
		seed += seedOffset + 1;
		
		FastNoiseLite tnoise = new FastNoiseLite();
		tnoise.SetSeed(seed);
		tnoise.SetNoiseType(NoiseType.OpenSimplex2); //SimplexFractal
		tnoise.SetFrequency(0.015f); //was 0.01
		tnoise.SetFractalType(FractalType.FBm);
		tnoise.SetFractalGain(1.3f); //seems to top out at 3.5 though
		tnoise.SetFractalOctaves(2);
		tnoise.SetFractalLacunarity(0.2f); //<-- 0.1?
		
		caveSizeNoise = tnoise;
	}
	
	public static void initCaveHeightMapStatic() {
		
		int seed = (int) ServerLifecycleHooks.getCurrentServer().getWorldData().worldGenOptions().seed(); //(int) this.ctx.randomState().legacyLevelSeed();
		seed += seedOffset + 1;
		
		FastNoiseLite tnoise = new FastNoiseLite();
		tnoise.SetSeed(seed);
		tnoise.SetNoiseType(NoiseType.OpenSimplex2); //SimplexFractal
		tnoise.SetFrequency(0.015f); //was 0.01
		tnoise.SetFractalType(FractalType.FBm);
		tnoise.SetFractalGain(1.3f); //seems to top out at 3.5 though
		tnoise.SetFractalOctaves(2);
		tnoise.SetFractalLacunarity(0.2f); //<-- 0.1?
		
		caveSizeNoise = tnoise;
	}

	@Override
	float getCaveYNoise(float x, float z) {
		if(yNoise == null) {
			initYNoise();
		}
		
		return yNoise.GetNoise(x, z);
	}


	@Override
	float getCaveThicknessNoise(float x, float z) {
		if(caveSizeNoise == null) {
			initCaveHeightMap();
		}
		
		return caveSizeNoise.GetNoise(x, z);
	}

}
