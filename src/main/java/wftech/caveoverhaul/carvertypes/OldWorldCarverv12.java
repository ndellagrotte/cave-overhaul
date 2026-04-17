package wftech.caveoverhaul.carvertypes;

import java.util.Random;
import java.util.function.Function;

import org.apache.commons.lang3.mutable.MutableBoolean;

import com.mojang.serialization.Codec;

import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.SectionPos;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.chunk.CarvingMask;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.Aquifer;
import net.minecraft.world.level.levelgen.NoiseChunk;
import net.minecraft.world.level.levelgen.carver.CarvingContext;
import net.minecraft.world.level.levelgen.carver.CaveCarverConfiguration;
import net.minecraft.world.level.levelgen.carver.CaveWorldCarver;
import org.jspecify.annotations.NonNull;
import wftech.caveoverhaul.AirOnlyAquifer;
import wftech.caveoverhaul.Config;
import wftech.caveoverhaul.mixins.CarvingContextAccessor;
import wftech.caveoverhaul.mixins.CaveCarverConfigurationAccessor;
import wftech.caveoverhaul.mixins.CaveWorldCarverAccessor;
import wftech.caveoverhaul.utils.Globals;
import wftech.caveoverhaul.utils.IMixinHelperNoiseChunk;
import wftech.caveoverhaul.utils.NoiseChunkMixinUtils;

public class OldWorldCarverv12 extends CaveWorldCarver {

    /*
     * Can't do literal 1.16.5- caves due to the new heights
     * With the introduction of deepslate, there's a great chance to rebalance cave densities around
     * the deepslate introduction layer. It'll create a sense of how deep the player is :)
     */
    public OldWorldCarverv12(Codec<CaveCarverConfiguration> p_159194_) {
        super(p_159194_);
    }

    public int getCaveY(RandomSource randomSource, boolean shallow) {
        if (shallow) {
            // Biased toward surface (Y=40-120). Uses double-nextInt for natural
            // distribution skewed toward the high end (most values near 80-120).
            // Must not query target chunk heightmap — see generateRoomCluster().
            return 120 - randomSource.nextInt(randomSource.nextInt(80) + 1);
        }
        return randomSource.nextInt(randomSource.nextInt(120 + Math.abs(Globals.getMinY())) + 8) - Math.abs(Globals.getMinY());
    }

    @Override
    protected float getThickness(RandomSource p_230359_1_) {
        return p_230359_1_.nextFloat() * 2.0f + p_230359_1_.nextFloat();
    }

    public void generateRoomCluster(CarvingContext context,
                                    CaveCarverConfiguration config,
                                    ChunkAccess chunk,
                                    Function<BlockPos, Holder<Biome>> posToBiomeMapping,
                                    RandomSource random,
                                    Aquifer aquifer,
                                    ChunkPos chunkPos,
                                    CarvingMask mask,
                                    int minHeight,
                                    int maxHeight,
                                    boolean shallow,
                                    boolean surfaceEntrance) {

        double x = chunkPos.getBlockX(random.nextInt(16));
        double coord_y;
        if (shallow) {
            coord_y = this.getCaveY(random, true);
        } else {
            coord_y = this.getCaveY(random, false);
        }
        double z = chunkPos.getBlockZ(random.nextInt(16));

        config.horizontalRadiusMultiplier.sample(random);
        config.verticalRadiusMultiplier.sample(random);
        double floorLevel = ((CaveCarverConfigurationAccessor) config).getFloorLevel().sample(random);

        CarveSkipChecker skipChecker = (world, x1, y1, z1, depth) -> CaveWorldCarverAccessor.shouldSkip(x1, y1, z1, floorLevel);

        int numRooms = 2 + random.nextInt(2); // 2-3 base
        if (random.nextInt(3) == 0) {
            numRooms += 1 + random.nextInt(2); // 33% chance of 1-2 extra = sometimes 3-5
        }

        if (shallow || random.nextInt(2) == 0) {
            double yScale = config.yScale.sample(random);
            float roomWidth = 1.0F + random.nextFloat() * 6.0F;
            createRoom(context, config, chunk, posToBiomeMapping, aquifer, x, coord_y, z, roomWidth, yScale, mask, skipChecker);
        }

        for (int i = 0; i < numRooms; ++i) {
            float angle = random.nextFloat() * ((float) Math.PI * 2F);
            float yOffset;
            if (surfaceEntrance) {
                // Bias tunnels downward so surface entrances lead underground
                yOffset = -(random.nextFloat() * 0.15F + 0.05F); // -0.05 to -0.2 radians
            } else {
                yOffset = (random.nextFloat() - 0.5F) / 2.0F;
            }
            float tunnelThickness = getThickness(random);
            int tunnelLength = 50 + random.nextInt(40); // 50-89 nodes

            this.addTunnel12(
                    context,
                    config,
                    posToBiomeMapping,
                    random.nextLong(),
                    aquifer,
                    chunk,
                    chunkPos,
                    x,
                    coord_y,
                    z,
                    angle,
                    yOffset,
                    tunnelThickness,
                    0,
                    tunnelLength,
                    this.getYScale(),
                    mask,
                    surfaceEntrance);
        }
    }

    protected boolean shouldCarve(CarvingContext ctx, CaveCarverConfiguration cfg, ChunkAccess level, RandomSource random, ChunkPos chunkPos) {
        float flt = random.nextFloat();
        return flt <= Config.getFloatSetting(Config.KEY_CAVE_CHANCE);
    }

    @Override
    public boolean carve(
            @NonNull CarvingContext ctx,
            @NonNull CaveCarverConfiguration cfg,
            @NonNull ChunkAccess level,
            @NonNull Function<BlockPos, Holder<Biome>> pos2BiomeMapping,
            @NonNull RandomSource random,
            @NonNull Aquifer disabled,
            @NonNull ChunkPos chunkPos,
            @NonNull CarvingMask mask) {

        // Dimension guard: the NCLayerHolder / NURLayerHolder / NoisetypeDomainWarp singletons
        // used downstream are server-scoped and seeded from the overworld NoiseGeneratorSettings.
        // If a data pack attaches this carver to a Nether/End/custom biome, bail out silently.
        NoiseChunk nc = ((CarvingContextAccessor) (Object) ctx).caveOverhaul$getNoiseChunk();
        if (nc == null || !((IMixinHelperNoiseChunk) (Object) nc).wFCaveOverhaul_Fork$isOverworld()) {
            return true;
        }

        if (!Config.getBoolSetting(Config.KEY_DEBUG_OLD_WORLD_CAVES)) return true;

        if (!this.shouldCarve(ctx, cfg, level, random, chunkPos)) {
            return true;
        }

        int minHeight = SectionPos.sectionToBlockCoord(this.getRange() * 2 - 1);
        int maxHeight = random.nextInt(random.nextInt(random.nextInt(this.getCaveBound()) + 1) + 1) + random.nextInt(2, 8); // was +1 at the end
        //Aquifer airAquifer = new AirOnlyAquifer(level, random.nextFloat() <=  0.15f);
        Aquifer airAquifer = new AirOnlyAquifer(level, random.nextFloat() <= Config.getFloatSetting(Config.KEY_CAVE_AIR_EXPOSURE));

        for (int k = 0; k < maxHeight; ++k) {
            this.generateRoomCluster(
                    ctx, cfg, level, pos2BiomeMapping, random,
                    airAquifer, chunkPos, mask, minHeight, maxHeight, false, false);
        }

        maxHeight = random.nextInt(random.nextInt(random.nextInt(this.getCaveBound()) + 1) + 1) + random.nextInt(2); // was +1 at the end
        for (int k = 0; k < maxHeight; ++k) {
            boolean surfaceEntrance = random.nextFloat() <= 0.30f;
            this.generateRoomCluster(
                    ctx, cfg, level, pos2BiomeMapping, random,
                    airAquifer, chunkPos, mask, minHeight, maxHeight, true, surfaceEntrance);
        }

        return true;
    }

    @Override
    public int getRange() {
        return 8;
    }

    protected void addTunnel12(
            CarvingContext context,
            CaveCarverConfiguration configuration,
            Function<BlockPos, Holder<Biome>> biomeFunction,
            long seed,
            Aquifer _aquifer,
            ChunkAccess chunkPrimer,
            ChunkPos originChunkPos,
            double initialX,
            double initialY,
            double initialZ,
            float yaw,
            float pitch,
            float unkModifier,
            int curNode,
            int endNode,
            double length,
            CarvingMask carvingMask,
            boolean surfaceEntrance) {

        Random random = new Random(seed);
        Aquifer aquifer;
        if (surfaceEntrance) {
            // Surface entrance tunnels must be able to carve near the surface
            aquifer = new AirOnlyAquifer(chunkPrimer, true);
        } else {
            aquifer = new AirOnlyAquifer(chunkPrimer, random.nextFloat() <= Config.getFloatSetting(Config.KEY_CAVE_AIR_EXPOSURE));
        }

//      MutableBlockPos mbPosCheckAir = new MutableBlockPos();
//		List<BlockPos> airPosList = new ArrayList<>();

        // Distance-budget check is anchored on the ORIGIN (neighbor) chunk so tunnels
        // can't wander outside the 8-chunk carver reach. Clamping writes at :290-311
        // still uses the HOME chunk's footprint.
        double startX = originChunkPos.getMiddleBlockX();
        double startZ = originChunkPos.getMiddleBlockZ();
        int minBlockX = chunkPrimer.getPos().getMinBlockX();
        int minBlockZ = chunkPrimer.getPos().getMinBlockZ();

        float pitchChange = 0.0f;
        float pitchChangeRate = 0.0f;

        if (endNode <= 0) {
            int maxendNode = this.getRange() * 16 - 16; //was this.range
            endNode = maxendNode - random.nextInt(maxendNode / 4);
        }

        boolean flag2 = false;

        if (curNode == -1) {
            curNode = endNode / 2;
            flag2 = true;
        }

        int j = random.nextInt(endNode / 2) + endNode / 4;
        random.nextInt(6);

        while (curNode < endNode) {
            //1.5 for the first term by default
            double yStepApprox = 1.5 + (double) (Mth.sin((float) curNode * 3.1415927f / (float) endNode) * unkModifier);
            double yStep = yStepApprox * length;
            float yawChangeRate = Mth.cos(pitch);
            float pitchChangeRateY = Mth.sin(pitch);
            initialX += Mth.cos(yaw) * yawChangeRate;
            initialY += pitchChangeRateY;
            initialZ += Mth.sin(yaw) * yawChangeRate;
            pitch += pitchChangeRate * 0.1f;
            pitch = Mth.clamp(pitch, -(float)(Math.PI / 3.0), (float)(Math.PI / 3.0));
            yaw += pitchChange * 0.1f;
            pitchChangeRate *= 0.9f;
            pitchChange *= 0.75f;
            pitchChangeRate += (random.nextFloat() - random.nextFloat()) * random.nextFloat() * 2.0f;
            pitchChange += (random.nextFloat() - random.nextFloat()) * random.nextFloat() * 4.0f;

            if (!flag2 && curNode == j && unkModifier > 1.0f && endNode > 0) {
                this.addTunnel12(
                        context, configuration, biomeFunction, random.nextLong(), aquifer, chunkPrimer, originChunkPos, initialX, initialY, initialZ,
                        yaw - ((float)Math.PI / 3.0f) - random.nextFloat() * 0.5f,
                        (random.nextFloat() - 0.5f) * 0.25f,
                        unkModifier / 3.0f, curNode, endNode, 1.0, carvingMask, false);
                this.addTunnel12(
                        context, configuration, biomeFunction, random.nextLong(), aquifer, chunkPrimer, originChunkPos, initialX, initialY, initialZ,
                        yaw + ((float)Math.PI / 3.0f) + random.nextFloat() * 0.5f,
                        (random.nextFloat() - 0.5f) * 0.25f,
                        unkModifier / 3.0f, curNode, endNode, 1.0, carvingMask, false);
            }

            if (flag2 || random.nextInt(4) != 0) {
                double deltaX = initialX - startX;
                double deltaZ = initialZ - startZ;
                double deltaDiameter = endNode - curNode;
                double maxDiameter = unkModifier + 2.0f + 16.0f;

                if (deltaX * deltaX + deltaZ * deltaZ - deltaDiameter * deltaDiameter > maxDiameter * maxDiameter) {
                    ++curNode;
                    continue;
                }
//
//				mbPosCheckAir.set(initialX, initialY, initialZ);
//				boolean flagInAir = chunkPrimer.getBlockState(mbPosCheckAir).isAir();
//				if(flagInAir) {
//					airPosList.add(new BlockPos((int) initialX, (int) initialY, (int) initialZ));
//				}

                if (initialX >= startX - 16.0 - yStepApprox * 2.0 && initialZ >= startZ - 16.0 - yStepApprox * 2.0 && initialX <= startX + 16.0 + yStepApprox * 2.0 && initialZ <= startZ + 16.0 + yStepApprox * 2.0) {
                    int minX = Mth.floor(initialX - yStepApprox) - minBlockX - 1;
                    int maxX = Mth.floor(initialX + yStepApprox) - minBlockX + 1;

                    int minY = Mth.floor(initialY - yStep) - 1;
                    int maxY = Mth.floor(initialY + yStep) + 1;

                    int minZ = Mth.floor(initialZ - yStepApprox) - minBlockZ - 1;
                    int maxZ = Mth.floor(initialZ + yStepApprox) - minBlockZ + 1;

                    if (minX < 0) {
                        minX = 0;
                    }

                    if (maxX > 16) {
                        maxX = 16;
                    }

                    if (minY < (Globals.getMinY() - 1)) {
                        minY = (Globals.getMinY() - 1);
                    }

                    if (maxY > 320) {
                        maxY = 320;
                    }

                    if (minZ < 0) {
                        minZ = 0;
                    }

                    if (maxZ > 16) {
                        maxZ = 16;
                    }

                    boolean isInOcean = false;

                    ChunkPos chunkPos = chunkPrimer.getPos();

                    if (!isInOcean) {
                        MutableBlockPos mutableBlockPos = new MutableBlockPos();
                        MutableBlockPos mutableBlockPos1 = new MutableBlockPos();

                        for (int xIter = minX; xIter < maxX; ++xIter) {
                            double xTargetSize = ((double) (xIter + minBlockX) + 0.5 - initialX) / yStepApprox;
                            int blockX = chunkPos.getBlockX(xIter);

                            for (int zIter = minZ; zIter < maxZ; ++zIter) {
                                int blockZ = chunkPos.getBlockZ(zIter);
                                double zTargetSize = ((double) (zIter + minBlockZ) + 0.5 - initialZ) / yStepApprox;


                                if (xTargetSize * xTargetSize + zTargetSize * zTargetSize >= 1.0) continue;

                                MutableBoolean shouldCarve = new MutableBoolean(false);

                                for (int yIter = maxY; yIter > minY; --yIter) {

                                    double yTargetSize = ((double) (yIter - 1) + 0.5 - initialY) / yStep;

                                    if (yTargetSize <= -0.7 ||
                                            xTargetSize * xTargetSize + yTargetSize * yTargetSize + zTargetSize * zTargetSize >= 1.0)
                                        continue;


									/*
	        	                    if(NoiseChunkMixinUtils.shouldSetToLava(128, blockX, yIter, blockZ)) {
	        	                        continue;
	        	            		} else if(NoiseChunkMixinUtils.shouldSetToWater(128, blockX, yIter, blockZ)) {
	        	                        continue;
	        	            		} else if(NoiseChunkMixinUtils.shouldSetToStone(128, blockX, yIter, blockZ)) {
	        	                        continue;
	        	            		} else if(NoiseChunkMixinUtils.shouldSetToLava(128, blockX, yIter + 1, blockZ)) {
	        	                        continue;
	        	            		} else if(NoiseChunkMixinUtils.shouldSetToWater(128, blockX, yIter + 1, blockZ)) {
	        	                        continue;
	        	            		}
									 */

                                    if (NoiseChunkMixinUtils.getRiverLayer(blockX, yIter, blockZ) != null) {
                                        continue;
                                    } else if (NoiseChunkMixinUtils.shouldSetToStone(blockX, yIter, blockZ)) {
                                        continue;
                                    } else if (NoiseChunkMixinUtils.getRiverLayer(blockX, yIter + 1, blockZ) != null) {
                                        continue;
                                    }

                                    mutableBlockPos.set(blockX, yIter, blockZ);

                                    this.carveBlock(context, configuration, chunkPrimer, biomeFunction, carvingMask, mutableBlockPos,
                                            mutableBlockPos1, aquifer, shouldCarve);
                                }
                            }
                        }

                        if (flag2) break;
                    }
                }

                ++curNode;
            }
        }

    }


}