package wftech.worldgenrevisited.carvertypes.rivers;

import java.util.HashMap;
import java.util.Random;

/*
 * Wall fix:
 * 
 * Instead of going from 1 to 0 (carve to not carve), allow for fuzzy edges where the noise threshold drops as well as the cave size height.
 * So maybe make the cave height drop off slowly, but the noise threshold drop off faster?
 * 
 * height/ysquish = y\ =\ 1\ -\ \frac{1}{1\ +\ e^{\left(1\ \cdot\ \left(-x\ +\ 10\right)\right)}}
 * threshold multiplier (1 to 0, float) = y\ =\ 1\ -\ \frac{1}{1\ +\ e^{\left(2\ \cdot\ \left(-x\ +\ 9\right)\right)}}
 */

import java.util.function.Function;

import org.apache.commons.lang3.mutable.MutableBoolean;
import org.joml.Vector2f;
import org.joml.Vector3f;

import com.mojang.serialization.Codec;

import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.CarvingMask;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.levelgen.Aquifer;
import net.minecraft.world.level.levelgen.DensityFunction;
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
import wftech.worldgenrevisited.fastnoise.FastNoiseLite.DomainWarpType;
import wftech.worldgenrevisited.fastnoise.FastNoiseLite.FractalType;
import wftech.worldgenrevisited.fastnoise.FastNoiseLite.NoiseType;

public class NoiseUndergroundRiverBackupDec8 extends CaveWorldCarver {

	public static int MAX_CAVE_SIZE_Y = 20;
	public static float NOISE_CUTOFF_RIVER = 0.92f;
	public static Block SAFE_ADD_BLOCK = Blocks.YELLOW_STAINED_GLASS;
	
	//public static FastNoiseLite noise = null;
	//public static FastNoiseLite yNoise = null;
	//public static FastNoiseLite caveSizeNoise = null;
	public static FastNoiseLite domainWarp = null;
	public static FastNoiseLite noise = null;
	public static FastNoiseLite noiseShouldCarveBase = null;
	public static FastNoiseLite noiseYLevelBase = null;
	
	private CarvingContext ctx;
	private CaveCarverConfiguration cfg;
	private ChunkAccess level;
	private Function<BlockPos, Holder<Biome>> biome;
	private RandomSource random;
	private Aquifer aquifer;
	private CarvingMask mask;
	private HashMap<String, Float> localThresholdCache;
	
	public static final Direction[] HORIZONTAL_DIRECTIONS = {Direction.EAST, Direction.NORTH, Direction.SOUTH, Direction.WEST};
	
	public NoiseUndergroundRiverBackupDec8(Codec<CaveCarverConfiguration> p_159194_) {
		super(p_159194_);
		// TODO Auto-generated constructor stub
	}
	
	//abstract float getCaveYNoise(float x, float z);
	//abstract float getCaveThicknessNoise(float x, float z);
	
	@Override
	public boolean isStartChunk(CaveCarverConfiguration p_224894_, RandomSource random) {
		return true;
	}
	
	protected void initNoise() {
		
		if(noise != null) {
			return;
		}		

		FastNoiseLite tnoise = new FastNoiseLite();
		tnoise.SetSeed((int) ServerLifecycleHooks.getCurrentServer().getWorldData().worldGenOptions().seed());
		tnoise.SetNoiseType(NoiseType.OpenSimplex2); //SimplexFractal
		tnoise.SetFrequency(0.003f); //CHANGED was 0.003
		tnoise.SetFractalType(FractalType.Ridged);
		//Fractal gain = 0.2, but I think it does nothing for this use case
		tnoise.SetFractalOctaves(1);
		
		/*
		tnoise.SetDomainWarpType(DomainWarpType.OpenSimplex2);
		tnoise.SetDomainWarpAmp(75f);
		tnoise.SetDomainWarpGain(0.4f);
		tnoise.SetDomainWarpOctaves(2);
		tnoise.SetDomainWarpLacunarity(2.7f);
		tnoise.SetDomainWarpFrequency(0.018f);
		*/
		
		/*
		 * Is domain warp broken? WTF?
		 * ^ Yup. It's not true domain warp. Time to use my own solution.
		 */
		/*
		tnoise.SetDomainWarpType(DomainWarpType.OpenSimplex2);
		tnoise.SetDomainWarpAmp(75f); //lowered from 75f to 35f //CHANGED
		//noise.SetDomainWarpGain(0.4f);
		//tnoise.SetDomainWarpOctaves(2);
		tnoise.SetDomainWarpLacunarity(2.7f);
		tnoise.SetDomainWarpFrequency(0.045f); //increased to 0.045 //0.018 is minimum for labarynthine canals //CHANGED
		*/
				
		noise = tnoise;
	}
	
	protected void initNoiseYLevel() {
		
		if(noise != null) {
			return;
		}		

		FastNoiseLite tnoise = new FastNoiseLite();
		tnoise.SetSeed((int) ServerLifecycleHooks.getCurrentServer().getWorldData().worldGenOptions().seed() + 51);
		tnoise.SetNoiseType(NoiseType.OpenSimplex2); //SimplexFractal
		tnoise.SetFrequency(0.002f);
				
		noiseYLevelBase = tnoise;
	}
	
	
	protected int getCaveY(float noiseValue) {
		float min = -3;
		float max = 0;
		float diffSize = max - min;
		return (int) (noiseValue * (diffSize)) + (int) min;
	}
	
	protected void initDomainWarp() {
		
		FastNoiseLite tnoise = new FastNoiseLite();
		tnoise.SetSeed((int) ServerLifecycleHooks.getCurrentServer().getWorldData().worldGenOptions().seed());
		tnoise.SetNoiseType(NoiseType.OpenSimplex2);
		tnoise.SetFrequency(0.025f);
		tnoise.SetFractalLacunarity(1.1f);
		tnoise.SetFractalGain(1.6f);
		domainWarp = tnoise;
	}
	
	protected void initShouldCarveNoise() {
		
		FastNoiseLite tnoise = new FastNoiseLite();
		tnoise.SetSeed((int) ServerLifecycleHooks.getCurrentServer().getWorldData().worldGenOptions().seed() + 50);
		tnoise.SetNoiseType(NoiseType.OpenSimplex2);
		tnoise.SetFrequency(0.0015f);
		noiseShouldCarveBase = tnoise;
	}
	
	protected float getShouldCarveNoise(int x, int z) {
		if(noiseShouldCarveBase == null) {
			initShouldCarveNoise();
		}
		
		return noiseShouldCarveBase.GetNoise(x, z);
	}
	
	protected float getCaveYNoise(int x, int z) {
		if(noiseYLevelBase == null) {
			initNoiseYLevel();
		}
		
		return noiseYLevelBase.GetNoise(x, z);
	}
	
	protected Block getLiquidType() {
		return Blocks.WATER;
	}
	
	protected boolean isOutOfBounds(int x, int z) {
		float shouldCarveNoise = this.getShouldCarveNoise(x, z);
		return shouldCarveNoise < 0f;
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
		this.localThresholdCache = new HashMap<String, Float>();

		ChunkPos chunkPos = level.getPos();
		int earlyXPos = chunkPos.getBlockX(0);
		int earlyZPos = chunkPos.getBlockZ(0);
		
		//Random _filterRandom = new Random((String.valueOf(earlyXPos) + String.valueOf(earlyZPos)).hashCode());
		
		//float nFloat = _filterRandom.nextFloat();
		
		//if(nFloat <= NOISE_CUTOFF_RIVER) {
		//	return false;
		//}
		
		Aquifer airAquifer = new AirOnlyAquifer(level, false); //random.nextFloat() <= Config.PERC_PIERCE_SURFACE.get()
		this.aquifer = airAquifer;
		//this.aquifer = _aquifer;
		
		BlockPos _basePos = chunkPos.getWorldPosition();

		MutableBlockPos mPos = new BlockPos.MutableBlockPos();
		MutableBlockPos chunkCenter = new BlockPos.MutableBlockPos();
		MutableBlockPos unkPos = new BlockPos.MutableBlockPos();
		MutableBoolean mBool = new MutableBoolean();
		
		
		for(int x_offset = 0; x_offset < 16; x_offset++) {
			for(int z_offset = 0; z_offset < 16; z_offset++) {
				float yLevelNoise = this.getCaveYNoise(earlyXPos, earlyZPos);
				int y = this.getCaveY(yLevelNoise);
				BlockPos bPos = new BlockPos(earlyXPos + x_offset, y, earlyZPos + z_offset);
				
				Block preferredBlock = this.getLiquidType();
				
				if(level.getBlockState(bPos).getBlock() == preferredBlock) {
					return true;
				}
				
				if(this.isOutOfBounds(bPos.getX(), bPos.getZ())) {
					continue;
				}

				//float noise = this.getNoise2D(earlyXPos + x_offset, earlyZPos + z_offset);
				float noise = this.getWarpedNoise(bPos.getX(), bPos.getZ());
				
				//Begin!
				if(noise > NOISE_CUTOFF_RIVER) {
					//WorldgenRevisited.LOGGER.error("[WorldgenRevisited] Carving at " + bPos + " with noise " + noise + ", original " + earlyXPos + ", " + earlyZPos + "; " + nFloat);
					this.recursiveDig(earlyXPos, earlyZPos, bPos, preferredBlock, 0, new Vector2f(), false, level, noise);
				} else {

					MutableBlockPos mbPos = new MutableBlockPos();
					mbPos.set(bPos.getX(), bPos.getY(), bPos.getZ());
					mbPos.set(bPos.getX() + 1, bPos.getY(), bPos.getZ());
					if(this.getWarpedNoise(mbPos.getX(), mbPos.getZ()) > NOISE_CUTOFF_RIVER) {
						
						float yLevelNoise1 = this.getCaveYNoise(mbPos.getX(), mbPos.getZ());
						int y1 = this.getCaveY(yLevelNoise);
						BlockPos adjPos = new BlockPos(bPos.getX(), y1, bPos.getZ());
						Block replacementBlock = SAFE_ADD_BLOCK;
						level.setBlockState(adjPos, replacementBlock.defaultBlockState(), false);
					}
					mbPos.set(bPos.getX() - 1, bPos.getY(), bPos.getZ());
					if(this.getWarpedNoise(mbPos.getX(), mbPos.getZ()) > NOISE_CUTOFF_RIVER) {
						float yLevelNoise1 = this.getCaveYNoise(mbPos.getX(), mbPos.getZ());
						int y1 = this.getCaveY(yLevelNoise);
						BlockPos adjPos = new BlockPos(bPos.getX(), y1, bPos.getZ());
						Block replacementBlock = SAFE_ADD_BLOCK;
						level.setBlockState(adjPos, replacementBlock.defaultBlockState(), false);
					}
					mbPos.set(bPos.getX(), bPos.getY(), bPos.getZ() + 1);
					if(this.getWarpedNoise(mbPos.getX(), mbPos.getZ()) > NOISE_CUTOFF_RIVER) {
						float yLevelNoise1 = this.getCaveYNoise(mbPos.getX(), mbPos.getZ());
						int y1 = this.getCaveY(yLevelNoise);
						BlockPos adjPos = new BlockPos(bPos.getX(), y1, bPos.getZ());
						Block replacementBlock = SAFE_ADD_BLOCK;
						level.setBlockState(adjPos, replacementBlock.defaultBlockState(), false);
					}
					mbPos.set(bPos.getX(), bPos.getY(), bPos.getZ() - 1);
					if(this.getWarpedNoise(mbPos.getX(), mbPos.getZ()) > NOISE_CUTOFF_RIVER) {
						float yLevelNoise1 = this.getCaveYNoise(mbPos.getX(), mbPos.getZ());
						int y1 = this.getCaveY(yLevelNoise);
						BlockPos adjPos = new BlockPos(bPos.getX(), y1, bPos.getZ());
						Block replacementBlock = SAFE_ADD_BLOCK;
						level.setBlockState(adjPos, replacementBlock.defaultBlockState(), false);
					}
				}
				
			}
		}
		
		return true;
	}
	
	
	protected void recursiveDig(int earlyX, int earlyZ, 
			BlockPos curPos, Block placementBlock, 
			int placementsDone, Vector2f momentumVector, boolean lockMomentum, ChunkAccess level, float rawNoise) {
		if(placementsDone > 10) {
			return;
		}
		
		if(level.getBlockState(curPos).getBlock() == placementBlock) {
			return;
		}

		//carve floor
		level.setBlockState(curPos, placementBlock.defaultBlockState(), false);
		//level.markPosForPostprocessing(curPos);
		level.setBlockState(curPos.above(1), Blocks.AIR.defaultBlockState(), false);
		level.setBlockState(curPos.above(2), Blocks.AIR.defaultBlockState(), false);
		
		/*
		int waterDeepest = 1;
		float noise_cutoff_lake_carver = 0.98f;
		if(rawNoise > noise_cutoff_lake_carver) {
			float waterDepthFloat = (1f - noise_cutoff_lake_carver) - (1f - rawNoise); //0 - 0.03
			int waterDepthCarve = (int) (waterDepthFloat * 100f);
			for(int i = 1; i <= waterDepthCarve; i++) {
				level.setBlockState(curPos.below(i), placementBlock.defaultBlockState(), false);
				waterDeepest += 1;
			}
		}
		*/
		
		//Carve roof
		float noiseDelta = rawNoise - NOISE_CUTOFF_RIVER;
		int noiseCutoffCeiling = (int) (noiseDelta * 100);
		noiseCutoffCeiling /= 2;
		int topDelta = noiseCutoffCeiling + 2;
		for(int i = 1; i < noiseCutoffCeiling; i++) {
			level.setBlockState(curPos.above(2 + i), Blocks.AIR.defaultBlockState(), false);
		}
		
		//add supporting blocks below
		BlockPos topPos = curPos.above(topDelta);
		boolean topIsLiquid = level.getBlockState(topPos).liquid();
		if(topIsLiquid) {
			level.setBlockState(topPos, SAFE_ADD_BLOCK.defaultBlockState(), false);
		}
		
		//Another layer even deeper below
		Block replacementBlock = SAFE_ADD_BLOCK;
		level.setBlockState(curPos.below(), replacementBlock.defaultBlockState(), false);
		if(level.getBlockState(curPos.below()).isAir()){
			BlockPos below = curPos.below();
			if(below.getY() <= 0 && Config.ENABLE_DEEPSLATE.get()) {
				//level.setBlockState(below, Blocks.RED_WOOL.defaultBlockState(), false);
			} else {
				//level.setBlockState(below, Blocks.RED_WOOL.defaultBlockState(), false);
			}
		}
		replacementBlock = SAFE_ADD_BLOCK;
		boolean carveBelow = level.getBlockState(curPos.below(2)).isAir();
		level.setBlockState(curPos.below(2), replacementBlock.defaultBlockState(), false);
		if(carveBelow) {
			if(curPos.below(2).getY() <= 0 && Config.ENABLE_DEEPSLATE.get()) {
				//level.setBlockState(curPos.below(2), Blocks.CYAN_WOOL.defaultBlockState(), false);
			} else {
				//level.setBlockState(curPos.below(2), Blocks.CYAN_WOOL.defaultBlockState(), false);
			}
		}
		
		//Checking nextdoor to see if I need to place blocks next to this block, or down below (if a y change occurs)
		boolean placedBlockBelow = false;
		for(Direction direction: HORIZONTAL_DIRECTIONS) {
			BlockPos relPos = curPos.relative(direction);
			//float noiseFound = this.getNoise2D(relPos);
			float noiseFound = this.getWarpedNoise(relPos.getX(), relPos.getZ());
			if(noiseFound > NOISE_CUTOFF_RIVER) {
				//recursiveDig(earlyX, earlyZ, relPos, placementBlock, placementsDone + 1, momentumVector, false, level);
			} else if(!level.getBlockState(relPos).is(BlockTags.STONE_ORE_REPLACEABLES)){
				if(relPos.getY() <= 0 && Config.ENABLE_DEEPSLATE.get()) {
					level.setBlockState(relPos, SAFE_ADD_BLOCK.defaultBlockState(), false);
				} else {
					level.setBlockState(relPos, SAFE_ADD_BLOCK.defaultBlockState(), false);
				}
			}
			
			if(!placedBlockBelow) {
				float shouldCarveNeighbor = this.getShouldCarveNoise(relPos.getX(), relPos.getZ());
				float yLevelNoise = this.getCaveYNoise(relPos.getX(), relPos.getZ());
				int neighborY = this.getCaveY(yLevelNoise);
				if(curPos.getY() < neighborY && shouldCarveNeighbor > NOISE_CUTOFF_RIVER) {
					placedBlockBelow = true;
					BlockPos newRelPos = new BlockPos(curPos.getX(), neighborY, curPos.getZ());
					if(!level.getBlockState(newRelPos).is(BlockTags.STONE_ORE_REPLACEABLES)){
						if(newRelPos.getY() <= 0 && Config.ENABLE_DEEPSLATE.get()) {
							level.setBlockState(newRelPos, SAFE_ADD_BLOCK.defaultBlockState(), false);
						} else {
							level.setBlockState(newRelPos, SAFE_ADD_BLOCK.defaultBlockState(), false);
						}
					}
				}
			}
		}
		
	}
	
	protected boolean shouldAdjustY() {
		return true;
	}

	protected float getNoiseThreshold(float x, float z) {
		//default 0.08
		return 0.08f;
		//New system: Scale the threshold from 1 (do not draw) to 0.08 (draw) based on noise
		
		/*
		String key = x + "," + z;
		if(this.localThresholdCache.containsKey(key)) {
			return this.localThresholdCache.get(key);
		}
		
		float baseNoise = this.getCaveYNoise(x, z);
		float squishedNoise = this.ySquishThreshold(baseNoise * (float) MAX_CAVE_SIZE_Y); //apply MAX_CAVE_SIZE_Y as modeling is done with it enabled
		/*
		 * I now have a value that is 0-1. I need to transform this value to something where
		 * it falls on a scale of 0.8-1, such that 1 = out of bounds (was 0), and 0.8 = in bounds (was 1).
		 * For the original, 0 = out of range, 1 = fully in range
		 */
		/*
		squishedNoise *= (1f - 0.08f); //limits it to 0-0.92, with 0.92 being in range
		squishedNoise = 1f - squishedNoise; //Makes it 0.08 - 1.0, with 0.08 being in range
		
		this.localThresholdCache.put(key, squishedNoise);
		return squishedNoise;
		*/
	}
	
	//Apply a sigmoid to the cave height so I quickly go from MAX_HEIGHT to MIN_HEIGHT, but only at the "boundary" of noise; leads to less
	//linear ugliness
	//ySquish defaults **were** k=2f, dist=2+1
	//Changed to k=1f, dist=8+1 due to the introduction of threshold squishing
	//Threshold squish is k=2, dist = 3+1, caveOffset = 9 (manually chosen based on modeling, using an assumed default cave height of 20.
	//The curve had to flatten to allow time for threshold squishing to work its magic
	public float ySquishThreshold(float noiseHeight) {
		float caveOffset = ((float) MAX_CAVE_SIZE_Y) / 2f; //(float)MAX_CAVE_SIZE_Y/4f; //if 32, becomes 8. Noise is usually a normal distribution with the mean being MAX/2.
		caveOffset = 9f;
		float k = 2f; //1f = 8 tiles from 1 to 0, 2f = 4 tiles, 16f for an outgoing range of [0, 1]
		//Use https://www.desmos.com/calculator
		//desmos equation: y\ =\ 1\ -\ \frac{1}{1\ +\ e^{\left(\left(-x\ +\ 32\right)\right)}}
		//The 3+1 is intentional
		int dist = 3 + 1; //2f = 2, 4f = 1, 1f = 8, 3f = 1.5?, then add a +1 to account for edge squish weirdness
		if (noiseHeight > caveOffset + dist || noiseHeight < caveOffset - dist) {
			return 0f;
		}
		
		return 1f - (float) (1f / (1f + Math.exp(k * (-noiseHeight + (caveOffset)))));
		
	}
	
	public float ySquish(float noiseHeight) {
		float caveOffset = ((float) MAX_CAVE_SIZE_Y) / 2f; //(float)MAX_CAVE_SIZE_Y/4f; //if 32, becomes 8. Noise is usually a normal distribution with the mean being MAX/2.
		float k = 2f; //1f = 8 tiles from 1 to 0, 2f = 4 tiles, 16f for an outgoing range of [0, 1]
		//Use https://www.desmos.com/calculator
		//desmos equation: y\ =\ 1\ -\ \frac{1}{1\ +\ e^{\left(\left(-x\ +\ 32\right)\right)}}
		int dist = 2 + 1; //2f = 2, 4f = 1, 1f = 8, 3f = 1.5?, then add a +1 to account for edge squish weirdness
		if (noiseHeight > caveOffset + dist || noiseHeight < caveOffset - dist) {
			return 0f;
		}
		
		return 1f - (float) (1f / (1f + Math.exp(k * (-noiseHeight + (caveOffset)))));
		
	}
	
	public static float ySquishSatic(float noiseHeight) {
		//was k = 2f, dist = 2 + 1
		float caveOffset = ((float) MAX_CAVE_SIZE_Y) / 2f; //(float)MAX_CAVE_SIZE_Y/4f; //if 32, becomes 8. Noise is usually a normal distribution with the mean being MAX/2.
		float k = 2f; //1f = 8 tiles from 1 to 0, 2f = 4 tiles, 16f for an outgoing range of [0, 1]
		//Use https://www.desmos.com/calculator
		//desmos equation: y\ =\ 1\ -\ \frac{1}{1\ +\ e^{\left(\left(-x\ +\ 32\right)\right)}}
		int dist = 2 + 1; //2f = 2, 4f = 1, 1f = 8, 3f = 1.5?, then add a +1 to account for edge squish weirdness
		if (noiseHeight > caveOffset + dist || noiseHeight < caveOffset - dist) {
			return 0f;
		}
		
		return 1f - (float) (1f / (1f + Math.exp(k * (-noiseHeight + (caveOffset)))));
		
	}
	
	public float norm(float f) {
		return (1f + f) / 2f;
	}
	
    public int getCaveY(RandomSource p_230361_1_) {
	    return p_230361_1_.nextInt(p_230361_1_.nextInt(p_230361_1_.nextInt(120 + 64) + 1) + 1) - 64;
    }

	
	protected float getWarpedNoise(int xPos, int zPos) {
			
		if(domainWarp == null) {
			initDomainWarp();
		}
		
		Integer[] offsetsX = {-101, 71, 53, 61, 3, 13};
		//Integer[] offsetsY = {23, 29, 31, 37, 41};
		Integer[] offsetsZ = {101, 67, 59, 41, 5, 7};

		float warpX = xPos;
		float warpZ = zPos;
		for(int i = 0; i < 2; i++) {
			//CHANGED
			//Not applying an offset to warpX is intentional.
			//The location for warpX can be anywhere, so it's ok that there's no offset. It hsould have no skew change or anything.
			warpX += domainWarp.GetNoise(warpX + 20, warpZ + 20) * 2f; //was 5 with pretty incredible results
			warpZ += domainWarp.GetNoise(warpX - 20, warpZ - 20) * 2f;
		}
		
		return this.getNoise2D((int) warpX, (int) warpZ);
	}
	
	protected float getNoise2D(int xPos, int zPos) {
		return this.getCaveDetailsNoise2D(xPos, zPos);
	}
	
	protected float getNoise2D(BlockPos pos) {
		return this.getCaveDetailsNoise2D(pos.getX(), pos.getZ());
	}

	protected float getCaveDetailsNoise(float x, float y, float z) {
		if(noise == null) {
			initNoise();
		}
		
		return noise.GetNoise(x, y, z);
	}

	protected float getCaveDetailsNoise2D(float x, float z) {
		if(noise == null) {
			initNoise();
		}
		
		return noise.GetNoise(x, z);
	}

}
