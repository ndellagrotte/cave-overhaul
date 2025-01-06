package wftech.worldgenrevisited.carvertypes;

import java.util.function.Function;

import org.apache.commons.lang3.mutable.MutableBoolean;

import com.mojang.serialization.Codec;

import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.core.Holder;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.chunk.CarvingMask;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.levelgen.Aquifer;
import net.minecraft.world.level.levelgen.Heightmap.Types;
import net.minecraft.world.level.levelgen.carver.CarvingContext;
import net.minecraft.world.level.levelgen.carver.CaveCarverConfiguration;
import net.minecraft.world.level.levelgen.carver.CaveWorldCarver;
import net.minecraft.world.level.levelgen.carver.WorldCarver;
import net.minecraftforge.server.ServerLifecycleHooks;
import wftech.worldgenrevisited.AirOnlyAquifer;
import wftech.worldgenrevisited.Config;
import wftech.worldgenrevisited.WorldgenRevisited;
import wftech.worldgenrevisited.fastnoise.FastNoiseLite;
import wftech.worldgenrevisited.fastnoise.FastNoiseLite.FractalType;
import wftech.worldgenrevisited.fastnoise.FastNoiseLite.NoiseType;

public abstract class NoiseCavernBaseBackup extends CaveWorldCarver {

	public static int MAX_CAVE_SIZE_Y = 20;
	
	public static FastNoiseLite noise = null;
	public static FastNoiseLite domainWarp = null;
	public static FastNoiseLite yNoise = null;
	public static FastNoiseLite caveSizeNoise = null;
	private CarvingContext ctx;
	private CaveCarverConfiguration cfg;
	private ChunkAccess level;
	private Function<BlockPos, Holder<Biome>> biome;
	private RandomSource random;
	private Aquifer aquifer;
	private CarvingMask mask;
	
	public NoiseCavernBaseBackup(Codec<CaveCarverConfiguration> p_159194_) {
		super(p_159194_);
		// TODO Auto-generated constructor stub
	}
	
	private void initNoise() {
		
		if(noise != null) {
			return;
		}
		
		//ServerLifecycleHooks.getCurrentServer().getWorldData().worldGenOptions().seed();

		FastNoiseLite tnoise = new FastNoiseLite();
		tnoise.SetSeed((int) ServerLifecycleHooks.getCurrentServer().getWorldData().worldGenOptions().seed());
		tnoise.SetFractalOctaves(1);
		tnoise.SetNoiseType(NoiseType.OpenSimplex2);
		tnoise.SetFractalGain(0.3f);
		tnoise.SetFrequency(0.025f);
		tnoise.SetFractalType(FractalType.FBm);
				
		noise = tnoise;
		
		this.initDomainWarp();
		this.initYNoise();
		this.initCaveHeightMap();
	}
	

	
	private void initYNoise() {

		int seed = (int) ServerLifecycleHooks.getCurrentServer().getWorldData().worldGenOptions().seed(); //(int) this.ctx.randomState().legacyLevelSeed();
		if(seed >= Integer.MAX_VALUE - 1) {
			seed = 1;
		} else {
			seed += 1;
		}
		
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
	
	private void initCaveHeightMap() {
		
		/*
		FastNoise tnoise = new FastNoise();
		tnoise.SetSeed(2);
		tnoise.SetNoiseType(NoiseType.SimplexFractal);
		tnoise.SetFrequency(0.02f); //was 0.01
		tnoise.SetFractalType(FractalType.FBM);
		tnoise.SetFractalGain(3.1f);
		tnoise.SetFractalOctaves(2);
		tnoise.SetFractalLacunarity(0.5f);
		*/
		
		//REMODEL BELOW AND CHANGE IT! It's going from 1 -> -1 too fast at times. The edges are too sharp!
		
		//type 1
		/*
		FastNoise tnoise = new FastNoise();
		tnoise.SetSeed(2);
		tnoise.SetNoiseType(NoiseType.SimplexFractal);
		tnoise.SetFrequency(0.02f); //was 0.01
		tnoise.SetFractalType(FractalType.FBM);
		tnoise.SetFractalGain(3.1f);
		tnoise.SetFractalOctaves(2);
		tnoise.SetFractalLacunarity(0.5f); //<-- 0.1?
		*/
		
		//type 2
		int seed = (int) ServerLifecycleHooks.getCurrentServer().getWorldData().worldGenOptions().seed(); //(int) this.ctx.randomState().legacyLevelSeed();
		if(seed >= Integer.MAX_VALUE - 3) {
			seed = 3;
		} else {
			seed += 3;
		}
		
		FastNoiseLite tnoise = new FastNoiseLite();
		tnoise.SetSeed(seed);
		tnoise.SetNoiseType(NoiseType.OpenSimplex2); //SimplexFractal
		tnoise.SetFrequency(0.015f); //was 0.01
		tnoise.SetFractalType(FractalType.FBm);
		tnoise.SetFractalGain(1.3f); //seems to top out at 3.5 though
		tnoise.SetFractalOctaves(2);
		tnoise.SetFractalLacunarity(0.2f); //<-- 0.1?
				
				//Also set freq. to 0.01, gain to 4.3, lacunarity to 0.1?
		
		caveSizeNoise = tnoise;
	}
	
	private void initDomainWarp() {
		
		FastNoiseLite tnoise = new FastNoiseLite();
		tnoise.SetSeed((int) ServerLifecycleHooks.getCurrentServer().getWorldData().worldGenOptions().seed());
		//tnoise.SetFractalOctaves(1);
		tnoise.SetNoiseType(NoiseType.OpenSimplex2);
		//tnoise.SetFractalGain(0.3f);
		tnoise.SetFrequency(0.01f);
		//tnoise.SetFractalType(FractalType.FBM);
		//tnoise.SetFractalType(FractalType.FBM);
		
		domainWarp = tnoise;
	}
	
	@Override
	public boolean carve(
		CarvingContext ctx, 
		CaveCarverConfiguration cfg, 
		ChunkAccess level, 
		Function<BlockPos, Holder<Biome>> pos2BiomeMapping, 
		RandomSource random, 
		Aquifer _aquifer, 
		ChunkPos chunkPos_, 
		CarvingMask mask) {

		this.ctx = ctx;
		this.cfg = cfg;
		this.level = level;
		this.biome = pos2BiomeMapping;
		this.random = random;
		this.mask = mask;
		
		if(random.nextFloat() <= 0.1) {
			return true;
		}
		
		Aquifer airAquifer = new AirOnlyAquifer(level, random.nextFloat() <= Config.PERC_PIERCE_SURFACE.get());
		this.aquifer = airAquifer;
		
		this.initNoise();
		ChunkPos chunkPos = level.getPos();
		BlockPos _basePos = chunkPos.getWorldPosition();

		MutableBlockPos mPos = new BlockPos.MutableBlockPos();
		MutableBlockPos chunkCenter = new BlockPos.MutableBlockPos();
		MutableBlockPos unkPos = new BlockPos.MutableBlockPos();
		MutableBoolean mBool = new MutableBoolean();
		
		LevelChunkSection[] sections = level.getSections();
		//WorldgenRevisited.LOGGER.error("[WorldgenRevisited] NoiseCarverTest " + chunkPos + "; " + _basePos + "; " + chunkPos.getBlockZ(5) + "; " + chunkPos.getMinBlockZ());
		
		for(int x_offset = 0; x_offset < 16; x_offset++) {
			for(int z_offset = 0; z_offset < 16; z_offset++) {
				int height = level.getHeight(Types.WORLD_SURFACE_WG, chunkPos.getBlockX(x_offset), chunkPos.getBlockZ(z_offset));
				//for(int y_unadj = height + 64; y_unadj >= 0; y_unadj--) {
				int earlyXPos = chunkPos.getBlockX(x_offset);
				int earlyZPos = chunkPos.getBlockZ(z_offset);
				float caveHeightNoise = caveSizeNoise.GetNoise(earlyXPos, earlyZPos);
				//CHANGED
				caveHeightNoise = ((1f + caveHeightNoise) / 2f) * (float) MAX_CAVE_SIZE_Y;
				//caveHeightNoise = this.norm(caveHeightNoise) * (float) MAX_CAVE_SIZE_Y;
				float caveHeightNoiseSquished = this.ySquish(caveHeightNoise);
				int caveHeight = (int) (caveHeightNoiseSquished * MAX_CAVE_SIZE_Y);
				if(caveHeight <= 0) {
					continue;
				}
				float rawNoiseY = yNoise.GetNoise(earlyXPos, earlyZPos);
				rawNoiseY = this.norm(rawNoiseY);
				rawNoiseY = rawNoiseY > 1 ? 1 : (rawNoiseY < 0 ? 0 : rawNoiseY);
				//int caveY = (int) (rawNoiseY * (64f + 120f));
				int caveY = this.getCaveY(rawNoiseY); //(int) (rawNoiseY * (64f));
				//int caveHeight = (int) (caveSizeNoise.GetNoise(earlyXPos, earlyZPos) * MAX_CAVE_SIZE_Y);
				//caveHeight = caveHeight < 0 ? 0 : (caveHeight > MAX_CAVE_SIZE_Y ? 32 : caveHeight);
				//WorldgenRevisited.LOGGER.error("[WorldgenRevisited] NoiseCarverTest " + caveHeightNoise + " -> " + caveHeightNoiseSquished + " -> " + caveHeight);
				for(int y_unadj = caveY + caveHeight; y_unadj > caveY; y_unadj--) {
				//for(int y_unadj = 74; y_unadj > -56; y_unadj--) {
					int xPos = chunkPos.getBlockX(x_offset);
					int yPos = y_unadj - 64;
					int y_adj = y_unadj - 64;
					int zPos = chunkPos.getBlockZ(z_offset);
					mPos.set(xPos, y_adj, zPos);
					if(level.getBlockState(mPos).isAir()) {
						continue;
					}
					//Use the unadjusted version to prevent weird y-related issues
					float noiseFound = this.getWarpedNoise(xPos, yPos, zPos);
					//float noiseFound = noise.GetNoise(xPos, yPos * 2, zPos);
					//float noiseFound = noise.GetNoise(xPos, yPos, zPos);
					//boolean shouldCarve = noiseFound > 0.12; //original, 0.08 works nicely though
					boolean shouldCarve = noiseFound > this.getNoiseThreshold(); //was 0.08
					if(shouldCarve) {
						
						/*
						 * 
	                                this.carveBlock(context, configuration, chunkPrimer, biomeFunction, carvingMask, mutableBlockPos,
	            	                        mutableBlockPos1, aquifer, shouldCarve);
						 */

						//mPos.set(xPos, yPos, zPos);
						chunkCenter.set(chunkPos.getBlockX(0), yPos, chunkPos.getBlockX(0));
						if(mPos.distManhattan(chunkCenter) > 8) {
							//continue;
						}
						
						//this.carve(ctx, cfg, level, pos2BiomeMapping, random, aquifer, chunkPos, mask);
						try {
							//mask.set(xPos, y_unadj, zPos);
							this.carveBlock(ctx, cfg, level, pos2BiomeMapping, mask, mPos, unkPos, aquifer, mBool);
							LevelAccessor access = level.getWorldForge();
							//							access.setBlock(mPos, Blocks.CAVE_AIR.defaultBlockState(), 2);
							//level.setBlockState(mPos, Blocks.CAVE_AIR.defaultBlockState(), false);
						} catch (ArrayIndexOutOfBoundsException e){
							WorldgenRevisited.LOGGER.error("[WorldgenRevisited] NoiseCarverTest real error");
						}
					}
				}
			}
		}
		
		return true;
	}
	
	protected int getCaveY(float noiseValue) {
		return (int) (noiseValue * (64f));
	}
	
	protected float getNoiseThreshold() {
		//default 0.08
		return 0.08f;
	}
	
	//Apply a sigmoid to the cave height so I quickly go from MAX_HEIGHT to MIN_HEIGHT, but only at the "boundary" of noise; leads to less
	//linear ugliness
	public float ySquish(float noiseHeight) {
		float caveOffset = ((float) MAX_CAVE_SIZE_Y) / 2f; //(float)MAX_CAVE_SIZE_Y/4f; //if 32, becomes 8. Noise is usually a normal distribution with the mean being MAX/2.
		float k = 2f; //1f = 8 tiles from 1 to 0, 2f = 4 tiles, 16f for an outgoing range of [0, 1]
		//Use https://www.desmos.com/calculator
		//desmos equation: y\ =\ 1\ -\ \frac{1}{1\ +\ e^{\left(\left(-x\ +\ 32\right)\right)}}
		int dist = 2 + 1; //2f = 2, 4f = 1, 1f = 8, 3f = 1.5?, then add a +1 to account for edge squish weirdness
		if (noiseHeight > caveOffset + dist || noiseHeight < caveOffset - dist) {
			return 0f;
		}
		
		//float tNoiseHeight = noiseHeight * 10f;
		
		//float minsize = 4f;
		//for [0, 1] use 0.75, else 4
		//caveOffset = caveOffset < minsize ? minsize : caveOffset;
		//return 1f - (float) (1f / (1f + Math.exp(k * (-tNoiseHeight + (caveOffset)))));
		return 1f - (float) (1f / (1f + Math.exp(k * (-noiseHeight + (caveOffset)))));
		
		//1 / (1 + e^ ( 16 * (-0.9 + 0.75) ) )
	}
	
	public float norm(float f) {
		return (1f + f) / 2f;
	}
	
    public int getCaveY(RandomSource p_230361_1_) {
	    return p_230361_1_.nextInt(p_230361_1_.nextInt(p_230361_1_.nextInt(120 + 64) + 1) + 1) - 64;
    }
	
	private float getNoise(int xPos, int yPos, int zPos) {
		return noise.GetNoise(xPos, yPos, zPos);
	}
	
	private float getWarpedNoise(int xPos, int yPos, int zPos) {
		Integer[] offsetsX = {-101, 71, 53, 61, 3, 13};
		//Integer[] offsetsY = {23, 29, 31, 37, 41};
		Integer[] offsetsZ = {101, 67, 59, 41, 5, 7};

		float warpX = xPos;
		float warpY = yPos;
		float warpZ = zPos;
		for(int i = 0; i < 3; i++) {
			//CHANGED
			warpX += domainWarp.GetNoise(warpX, warpY, warpZ) * 25f; //was 5 with pretty incredible results
			warpY += domainWarp.GetNoise(warpX + 20, warpY + 20, warpZ + 20) * 25f;
			warpZ += domainWarp.GetNoise(warpX - 20, warpY - 20, warpZ - 20) * 25f;
			//warpX += domainWarp.GetNoise(warpX + offsetsX[i], zPos + offsetsZ[i]) * 25f;
			//warpY += domainWarp.GetNoise(warpX + offsetsZ[i], zPos + offsetsX[i]) * 25f;
			//warpZ += domainWarp.GetNoise(warpX + offsetsZ[i], zPos + offsetsX[i]) * 25f;
		}
		
		return noise.GetNoise(warpX, warpY, warpZ);
	}

	private void recursiveDig(int x, int y, int z, int numBlocksDug) {
		
	}
	
	//if there's steep y momentum, we dig an extra horizontal area at the bottom. Maybe into a "victory" spot?
	private void momentumDig() {
		
	}

}
