package wftech.caveoverhaul.mixins;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LightChunk;
import net.minecraft.world.level.dimension.BuiltinDimensionTypes;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.Aquifer;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.NoiseChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import wftech.caveoverhaul.Config;
import wftech.caveoverhaul.WorldGenUtils;
import wftech.caveoverhaul.utils.FabricUtils;
import wftech.caveoverhaul.utils.IMixinHelperNoiseChunk;
import wftech.caveoverhaul.utils.NoiseChunkMixinUtils;
import wftech.caveoverhaul.CaveOverhaul;

@Mixin(Aquifer.NoiseBasedAquifer.class)
public class NoiseBasedAquiferMixin {


    @Inject(method="computeSubstance(Lnet/minecraft/world/level/levelgen/DensityFunction$FunctionContext;D)Lnet/minecraft/world/level/block/state/BlockState;",
            at = @At("RETURN"), cancellable=true, remap=true)
    private void getInterpolatedStateMixin(DensityFunction.FunctionContext df, double unkDouble, CallbackInfoReturnable<BlockState> cir) {
        //don't forget to check if I'm in the overworld or not!

        //boolean USE_AQUIFER_PATCH = Config.settings.get(Config.KEY_USE_AQUIFER_PATCH) == 1f;
        boolean USE_AQUIFER_PATCH = Config.getBoolSetting(Config.KEY_USE_AQUIFER_PATCH);
        if(!USE_AQUIFER_PATCH) {
            return;
        }

        //cheeselands, funnylands, skylands...
        //it's all the same!
        boolean CHEESE_LANDS_ENABLED = false;
        if(!CHEESE_LANDS_ENABLED) {
            if (cir.getReturnValue() == null || !(cir.getReturnValue().is(Blocks.WATER) || cir.getReturnValue().is(Blocks.LAVA))) {
                return;
            }
        }


        if(df == null){
            return;
        }


        int x = df.blockX();
        int y = df.blockY();
        int z = df.blockZ();

        NoiseChunk thisChunk = ((AquiferAccessor) (Object) this).getNoiseChunk();
        //boolean isLikelyOverworld = WorldGenUtils.checkIfLikelyOverworld( ((NoiseChunkAccessor) thisChunk).getNoiseSettings() );
        boolean isLikelyOverworld = WorldGenUtils.checkIfLikelyOverworld(((IMixinHelperNoiseChunk) (Object) thisChunk).getNGS());
        if(!isLikelyOverworld) {
            return;
        }

        if (y <= (-64 + 9) && cir.getReturnValue().getBlock() == Blocks.LAVA){
            return;
        }

        int topY = thisChunk.preliminarySurfaceLevel(x, z);
        topY = topY - 8;

        if(y >= topY) {
            return;
        }

        boolean is_air_w = NoiseChunkMixinUtils.isAirBlock(x + 1, y, z);
        boolean is_air_e = NoiseChunkMixinUtils.isAirBlock(x - 1, y, z);
        boolean is_air_n = NoiseChunkMixinUtils.isAirBlock(x, y, z + 1);
        boolean is_air_s = NoiseChunkMixinUtils.isAirBlock(x, y, z - 1);
        boolean is_air_d = NoiseChunkMixinUtils.isAirBlock(x, y - 1, z);

        if (is_air_d || is_air_w || is_air_e || is_air_n || is_air_s){

            if (FabricUtils.server != null) {
                Level level = null;

                if (FabricUtils.overworld == null) {
                    for (Level tlevel: FabricUtils.server.getAllLevels()){

                        if (tlevel.dimensionTypeRegistration().unwrap().left().isEmpty()){
                            return;
                        }

                        if (tlevel.dimensionTypeRegistration().unwrap().left().get() == BuiltinDimensionTypes.OVERWORLD){
                            level = tlevel;
                            break;
                        }
                    }

                    FabricUtils.overworld = level;
                } else {
                    level = FabricUtils.overworld;
                }

                if (level == null){
                    return;
                }

                if (y >= level.getSeaLevel() - 25) {
                    return;
                }
            }
            //((NoiseChunkAccessor) (Object) thisChunk).invokeGetSurfaceLevel(new BlockPos(x, y, z).asLong());
            cir.setReturnValue(Blocks.STONE.defaultBlockState());
            cir.cancel();
        }
    }
    // m_207104_(Lnet/minecraft/world/level/levelgen/DensityFunction$FunctionContext;D)Lnet/minecraft/world/level/block/state/BlockState;
}