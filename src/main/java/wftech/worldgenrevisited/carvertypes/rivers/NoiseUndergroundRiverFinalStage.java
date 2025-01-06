package wftech.worldgenrevisited.carvertypes.rivers;

import java.util.HashMap;
import java.util.Random;
import java.util.function.Function;

import com.mojang.serialization.Codec;

import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.core.Holder;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.chunk.CarvingMask;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.Aquifer;
import net.minecraft.world.level.levelgen.carver.CarvingContext;
import net.minecraft.world.level.levelgen.carver.CaveCarverConfiguration;
import net.minecraft.world.level.levelgen.carver.CaveWorldCarver;
import wftech.worldgenrevisited.Config;
import wftech.worldgenrevisited.WorldgenRevisited;

public class NoiseUndergroundRiverFinalStage extends CaveWorldCarver {

	private CarvingContext ctx;
	private CaveCarverConfiguration cfg;
	private ChunkAccess level;
	private Function<BlockPos, Holder<Biome>> biome;
	private RandomSource random;
	private CarvingMask mask;
	private HashMap<String, Float> localThresholdCache;

	public NoiseUndergroundRiverFinalStage(Codec<CaveCarverConfiguration> p_159194_) {
		super(p_159194_);
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
		
		MutableBlockPos mPos = new BlockPos.MutableBlockPos();
		for(int i = 0; i < 16; i++) {
			for(int j = 0; j < 16; j++) {
				for(int y = 128; y > -64; y--) {
					mPos.set(earlyXPos + i, y, earlyZPos + j);
					if(level.getBlockState(mPos).getBlock() == NoiseUndergroundRiver.SAFE_ADD_BLOCK) {	
						
						if(y <= 0 && Config.ENABLE_DEEPSLATE.get()) {
							level.setBlockState(mPos, Blocks.DEEPSLATE.defaultBlockState(), false);
						} else {
							level.setBlockState(mPos, Blocks.STONE.defaultBlockState(), false);
						}
					}
				}
			}
		}
		
		return true;
	}
}
