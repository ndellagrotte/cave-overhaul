package wftech.caveoverhaul.utils;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import wftech.caveoverhaul.Config;

import java.util.Map;
import java.util.function.Function;

public class OreReplacementUtil {

    private static Map<Block, BlockState> oreToCoal;

    private static Map<Block, BlockState> getOreToCoal() {
        if (oreToCoal == null) {
            oreToCoal = Map.ofEntries(
                    Map.entry(Blocks.DIAMOND_ORE, Blocks.COAL_ORE.defaultBlockState()),
                    Map.entry(Blocks.DEEPSLATE_DIAMOND_ORE, Blocks.DEEPSLATE_COAL_ORE.defaultBlockState()),
                    Map.entry(Blocks.GOLD_ORE, Blocks.COAL_ORE.defaultBlockState()),
                    Map.entry(Blocks.DEEPSLATE_GOLD_ORE, Blocks.DEEPSLATE_COAL_ORE.defaultBlockState()),
                    Map.entry(Blocks.EMERALD_ORE, Blocks.COAL_ORE.defaultBlockState()),
                    Map.entry(Blocks.DEEPSLATE_EMERALD_ORE, Blocks.DEEPSLATE_COAL_ORE.defaultBlockState()),
                    Map.entry(Blocks.LAPIS_ORE, Blocks.COAL_ORE.defaultBlockState()),
                    Map.entry(Blocks.DEEPSLATE_LAPIS_ORE, Blocks.DEEPSLATE_COAL_ORE.defaultBlockState()),
                    Map.entry(Blocks.REDSTONE_ORE, Blocks.COAL_ORE.defaultBlockState()),
                    Map.entry(Blocks.DEEPSLATE_REDSTONE_ORE, Blocks.DEEPSLATE_COAL_ORE.defaultBlockState())
            );
        }
        return oreToCoal;
    }

    /**
     * Check if a valuable ore should be downgraded based on air adjacency.
     * Returns the BlockState to place, or null if the ore should not be placed at all.
     */
    public static BlockState maybeDowngradeOre(BlockState ore, BlockPos pos, Function<BlockPos, BlockState> blockGetter) {
        if (!Config.getBoolSetting(Config.KEY_ORE_AIR_EXPOSURE_ONLY)) {
            return ore;
        }

        BlockState coalReplacement = getOreToCoal().get(ore.getBlock());
        if (coalReplacement == null) {
            return ore;
        }

        if (hasAdjacentAir(pos, blockGetter)) {
            return ore;
        }

        if (Config.getBoolSetting(Config.KEY_ORE_COAL_REPLACEMENT)) {
            return coalReplacement;
        }
        return null;
    }

    private static final int[][] OFFSETS = {{1,0,0},{-1,0,0},{0,1,0},{0,-1,0},{0,0,1},{0,0,-1}};

    private static boolean hasAdjacentAir(BlockPos pos, Function<BlockPos, BlockState> blockGetter) {
        BlockPos.MutableBlockPos neighbor = new BlockPos.MutableBlockPos();
        for (int[] offset : OFFSETS) {
            neighbor.set(pos.getX() + offset[0], pos.getY() + offset[1], pos.getZ() + offset[2]);
            BlockState state = blockGetter.apply(neighbor);
            if (state != null && state.isAir()) {
                return true;
            }
        }
        return false;
    }
}
