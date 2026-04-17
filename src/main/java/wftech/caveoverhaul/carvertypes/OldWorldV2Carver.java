package wftech.caveoverhaul.carvertypes;

import java.util.Random;
import java.util.function.Function;

import org.apache.commons.lang3.mutable.MutableBoolean;

import com.mojang.serialization.Codec;

import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.core.Holder;
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
import wftech.caveoverhaul.utils.Globals;
import wftech.caveoverhaul.utils.IMixinHelperNoiseChunk;
import wftech.caveoverhaul.utils.NoiseChunkMixinUtils;
import wftech.caveoverhaul.utils.Settings;

/*
 * Old World Caves v2 — hybrid carver.
 *
 * Fixes three issues with v1 (OldWorldCarverv12):
 *  1. v1's ±π/3 pitch clamp plus a surface-entrance downward bias left most
 *     tunnels descending. v2 clamps to ±π/4 with no directional bias, so
 *     tunnels are playable (≤45°) going up or down.
 *  2. v1's random per-chunk roll produced visible clustering. v2 consults
 *     a smooth low-frequency noise field (OldWorldV2LayerHolder) at chunk
 *     resolution for uniform-yet-varied spawn distribution.
 *  3. v1's getThickness randomized tunnel radius, producing wide variance.
 *     v2 uses a fixed radius so every tunnel is consistently ~3 wide/tall.
 *
 * Node-walk mechanics (random yaw/pitch integration, the ridge-of-carve
 * ellipsoid, river-zone vetoes) mirror v1 because that's what produces the
 * "snaking 3-wide tunnel" look the user wants.
 */
public class OldWorldV2Carver extends CaveWorldCarver {

    private static final float PITCH_CLAMP = (float) (Math.PI / 4.0);

    public OldWorldV2Carver(Codec<CaveCarverConfiguration> codec) {
        super(codec);
    }

    @Override
    public int getRange() {
        return 8;
    }

    @Override
    protected float getThickness(RandomSource random) {
        // Constant — v1 randomized this and that's the width variance the
        // user explicitly wanted eliminated.
        return Settings.OLD_WORLD_V2_TUNNEL_RADIUS;
    }

    @Override
    public boolean carve(
            @NonNull CarvingContext ctx,
            @NonNull CaveCarverConfiguration cfg,
            @NonNull ChunkAccess level,
            @NonNull Function<BlockPos, Holder<Biome>> pos2BiomeMapping,
            @NonNull RandomSource random,
            @NonNull Aquifer unused,
            @NonNull ChunkPos chunkPos,
            @NonNull CarvingMask mask) {

        // Dimension guard — layer holders are overworld-scoped. Same check v1 uses.
        NoiseChunk nc = ((CarvingContextAccessor) (Object) ctx).caveOverhaul$getNoiseChunk();
        if (nc == null || !((IMixinHelperNoiseChunk) (Object) nc).wFCaveOverhaul_Fork$isOverworld()) {
            return true;
        }

        if (!Config.getBoolSetting(Config.KEY_DEBUG_OLD_WORLD_CAVES_V2)) {
            return true;
        }

        // Chunk-gate: smooth noise field decides which chunks may spawn a cluster.
        int cx = chunkPos.getMinBlockX() >> 4;
        int cz = chunkPos.getMinBlockZ() >> 4;
        if (!OldWorldV2LayerHolder.getInstance().shouldSpawnAt(cx, cz)) {
            return true;
        }

        int topCap = Settings.OLD_WORLD_V2_TOP_CAP;
        int bottomCap = Globals.getMinY() + Settings.OLD_WORLD_V2_BEDROCK_BUFFER;
        int yRange = topCap - bottomCap;
        if (yRange <= 0) return true;

        Aquifer airAquifer = new AirOnlyAquifer(
                level,
                random.nextFloat() <= Config.getFloatSetting(Config.KEY_CAVE_AIR_EXPOSURE));

        double x = chunkPos.getBlockX(random.nextInt(16));
        double z = chunkPos.getBlockZ(random.nextInt(16));
        double y = bottomCap + random.nextInt(yRange);

        int tunnels = Settings.OLD_WORLD_V2_TUNNELS_PER_CLUSTER;
        for (int i = 0; i < tunnels; i++) {
            float yaw = random.nextFloat() * ((float) Math.PI * 2F);
            // Symmetric pitch — no downward bias. Roughly ±0.25 radians of
            // initial aim; the clamp keeps the walker from exceeding ±π/4.
            float pitch = (random.nextFloat() - 0.5F) * 0.5F;
            float thickness = getThickness(random);
            int tunnelLength = Settings.OLD_WORLD_V2_TUNNEL_LENGTH_MIN
                    + random.nextInt(Settings.OLD_WORLD_V2_TUNNEL_LENGTH_RANGE);

            addTunnelV2(
                    ctx, cfg, pos2BiomeMapping, random.nextLong(),
                    airAquifer, level, chunkPos,
                    x, y, z,
                    yaw, pitch, thickness,
                    0, tunnelLength, this.getYScale(), mask);
        }

        return true;
    }

    /*
     * Node-walk tunnel carver. Structure mirrors OldWorldCarverv12.addTunnel12 —
     * integration step, reach-budget check, ellipsoid write loop — with three
     * intentional departures from v1:
     *   - pitch clamp is tighter (±π/4 vs ±π/3)
     *   - no surfaceEntrance parameter and no pitch seeding toward the ground
     *   - thickness is constant, propagated unchanged to any branch recursions
     */
    private void addTunnelV2(
            CarvingContext context,
            CaveCarverConfiguration configuration,
            Function<BlockPos, Holder<Biome>> biomeFunction,
            long seed,
            Aquifer aquifer,
            ChunkAccess chunkPrimer,
            ChunkPos originChunkPos,
            double initialX,
            double initialY,
            double initialZ,
            float yaw,
            float pitch,
            float thickness,
            int curNode,
            int endNode,
            double yScale,
            CarvingMask carvingMask) {

        Random random = new Random(seed);

        double startX = originChunkPos.getMiddleBlockX();
        double startZ = originChunkPos.getMiddleBlockZ();
        int minBlockX = chunkPrimer.getPos().getMinBlockX();
        int minBlockZ = chunkPrimer.getPos().getMinBlockZ();

        float pitchChange = 0.0f;
        float pitchChangeRate = 0.0f;

        if (endNode <= 0) {
            int maxEndNode = this.getRange() * 16 - 16;
            endNode = maxEndNode - random.nextInt(maxEndNode / 4);
        }

        int branchNode = random.nextInt(endNode / 2) + endNode / 4;
        random.nextInt(6);

        while (curNode < endNode) {
            double yStepApprox = 1.5 + (double) (Mth.sin((float) curNode * 3.1415927f / (float) endNode) * thickness);
            double yStep = yStepApprox * yScale;
            float yawChangeRate = Mth.cos(pitch);
            float pitchChangeRateY = Mth.sin(pitch);
            initialX += Mth.cos(yaw) * yawChangeRate;
            initialY += pitchChangeRateY;
            initialZ += Mth.sin(yaw) * yawChangeRate;
            pitch += pitchChangeRate * 0.1f;
            // Tighter than v1's ±π/3 — keeps slopes comfortably walkable.
            pitch = Mth.clamp(pitch, -PITCH_CLAMP, PITCH_CLAMP);
            yaw += pitchChange * 0.1f;
            pitchChangeRate *= 0.9f;
            pitchChange *= 0.75f;
            pitchChangeRate += (random.nextFloat() - random.nextFloat()) * random.nextFloat() * 2.0f;
            pitchChange += (random.nextFloat() - random.nextFloat()) * random.nextFloat() * 4.0f;

            if (curNode == branchNode && thickness > 1.0f && endNode > 0) {
                addTunnelV2(
                        context, configuration, biomeFunction, random.nextLong(), aquifer, chunkPrimer, originChunkPos,
                        initialX, initialY, initialZ,
                        yaw - ((float) Math.PI / 3.0f) - random.nextFloat() * 0.5f,
                        (random.nextFloat() - 0.5f) * 0.25f,
                        thickness, curNode, endNode, 1.0, carvingMask);
                addTunnelV2(
                        context, configuration, biomeFunction, random.nextLong(), aquifer, chunkPrimer, originChunkPos,
                        initialX, initialY, initialZ,
                        yaw + ((float) Math.PI / 3.0f) + random.nextFloat() * 0.5f,
                        (random.nextFloat() - 0.5f) * 0.25f,
                        thickness, curNode, endNode, 1.0, carvingMask);
            }

            if (random.nextInt(4) != 0) {
                double deltaX = initialX - startX;
                double deltaZ = initialZ - startZ;
                double deltaDiameter = endNode - curNode;
                double maxDiameter = thickness + 2.0f + 16.0f;

                if (deltaX * deltaX + deltaZ * deltaZ - deltaDiameter * deltaDiameter > maxDiameter * maxDiameter) {
                    ++curNode;
                    continue;
                }

                if (initialX >= startX - 16.0 - yStepApprox * 2.0
                        && initialZ >= startZ - 16.0 - yStepApprox * 2.0
                        && initialX <= startX + 16.0 + yStepApprox * 2.0
                        && initialZ <= startZ + 16.0 + yStepApprox * 2.0) {

                    int minX = Mth.floor(initialX - yStepApprox) - minBlockX - 1;
                    int maxX = Mth.floor(initialX + yStepApprox) - minBlockX + 1;
                    int minY = Mth.floor(initialY - yStep) - 1;
                    int maxY = Mth.floor(initialY + yStep) + 1;
                    int minZ = Mth.floor(initialZ - yStepApprox) - minBlockZ - 1;
                    int maxZ = Mth.floor(initialZ + yStepApprox) - minBlockZ + 1;

                    if (minX < 0) minX = 0;
                    if (maxX > 16) maxX = 16;
                    if (minY < (Globals.getMinY() - 1)) minY = (Globals.getMinY() - 1);
                    if (maxY > 320) maxY = 320;
                    if (minZ < 0) minZ = 0;
                    if (maxZ > 16) maxZ = 16;

                    ChunkPos chunkPos = chunkPrimer.getPos();
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

                                if (yTargetSize <= -0.7
                                        || xTargetSize * xTargetSize + yTargetSize * yTargetSize + zTargetSize * zTargetSize >= 1.0)
                                    continue;

                                if (NoiseChunkMixinUtils.getRiverLayer(blockX, yIter, blockZ) != null) continue;
                                if (NoiseChunkMixinUtils.shouldSetToStone(blockX, yIter, blockZ)) continue;
                                if (NoiseChunkMixinUtils.getRiverLayer(blockX, yIter + 1, blockZ) != null) continue;

                                mutableBlockPos.set(blockX, yIter, blockZ);
                                this.carveBlock(context, configuration, chunkPrimer, biomeFunction, carvingMask,
                                        mutableBlockPos, mutableBlockPos1, aquifer, shouldCarve);
                            }
                        }
                    }
                }

                ++curNode;
            }
        }
    }
}
