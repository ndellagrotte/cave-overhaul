package wftech.caveoverhaul.utils;

import wftech.caveoverhaul.CaveOverhaul;

public class CaveDataLogger {

	private static final ThreadLocal<ChunkStats> STATS = ThreadLocal.withInitial(ChunkStats::new);

	private static class ChunkStats {
		int chunkX;
		int chunkZ;
		int caveBlocks;
		int riverLiquidBlocks;
		int riverAirBlocks;
		boolean hasData;
		boolean hasCoords;
	}

	public static void onNewChunk() {
		ChunkStats stats = STATS.get();
		if (stats.hasData) {
			flush(stats);
		}
		stats.caveBlocks = 0;
		stats.riverLiquidBlocks = 0;
		stats.riverAirBlocks = 0;
		stats.hasData = false;
		stats.hasCoords = false;
	}

	public static void ensureChunkCoords(int blockX, int blockZ) {
		ChunkStats stats = STATS.get();
		if (!stats.hasCoords) {
			stats.chunkX = blockX >> 4;
			stats.chunkZ = blockZ >> 4;
			stats.hasCoords = true;
		}
	}

	public static void recordCaveBlock() {
		ChunkStats stats = STATS.get();
		stats.caveBlocks++;
		stats.hasData = true;
	}

	public static void recordRiverLiquid() {
		ChunkStats stats = STATS.get();
		stats.riverLiquidBlocks++;
		stats.hasData = true;
	}

	public static void recordRiverAir() {
		ChunkStats stats = STATS.get();
		stats.riverAirBlocks++;
		stats.hasData = true;
	}

	private static void flush(ChunkStats stats) {
		long seed = 0;
		try {
			if (FabricUtils.server != null) {
				seed = FabricUtils.server.getWorldData().worldGenOptions().seed();
			}
		} catch (Exception ignored) {
		}

		int riverBlocks = stats.riverLiquidBlocks + stats.riverAirBlocks;
		CaveOverhaul.LOGGER.info(
				"[CAVEDATA] chunk={},{} cave_blocks={} river_blocks={} river_liquid={} river_air={} seed={}",
				stats.chunkX, stats.chunkZ,
				stats.caveBlocks, riverBlocks,
				stats.riverLiquidBlocks, stats.riverAirBlocks,
				seed
		);
	}

	public static void flushAll() {
		ChunkStats stats = STATS.get();
		if (stats.hasData) {
			flush(stats);
		}
		stats.hasData = false;
	}
}
