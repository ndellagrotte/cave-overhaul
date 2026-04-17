package wftech.caveoverhaul.utils;

public class Settings {
    public static int MAX_CAVE_SIZE_Y = 12;
    public static float CAVE_HEIGHT_SIGMOID_CENTER = 6f;
    public static float CAVE_HEIGHT_SIGMOID_STEEPNESS = 2f;

    // Cut-off for the domain-warped structure noise in NCDynamicLayer.
    // A voxel inside the cave slab is carved when the warped noise exceeds this value.
    // Warped FastNoiseLite output lives roughly in [-1, 1]; useful values for this
    // knob are a narrow band around zero. Lower -> more voxels pass -> denser, more
    // connected caves. Higher -> sparser, more fragmented caves. This is the single
    // biggest density knob for the noise-cave system.
    public static float NOISE_CAVE_STRUCTURE_THRESHOLD = 0.15f;

    // Old World Caves v2 — hybrid architecture.
    // A low-frequency noise field is sampled at chunk resolution to gate which
    // chunks may spawn a cluster (see OldWorldV2LayerHolder); a v1-style carver
    // then generates the actual tunnel geometry at those locations.

    // Gate frequency (in chunk units). Lower -> broader zones; higher -> grainier.
    // At 0.08 a zone spans roughly 8–16 chunks before the noise flips sign.
    public static float OLD_WORLD_V2_GATE_FREQUENCY = 0.08f;
    // Gate threshold. OpenSimplex2 output sits roughly in [-1, 1]; a threshold
    // of 0.0 passes ~50% of chunks, 0.3 passes ~25%, 0.5 passes ~15%. This is
    // the main spawn-density knob.
    public static float OLD_WORLD_V2_GATE_THRESHOLD = 0.30f;

    // Per-cluster shape. v2 carves one cluster per passing chunk.
    public static int   OLD_WORLD_V2_TUNNELS_PER_CLUSTER = 2;
    public static int   OLD_WORLD_V2_TUNNEL_LENGTH_MIN = 50;
    public static int   OLD_WORLD_V2_TUNNEL_LENGTH_RANGE = 40;
    // Fixed tunnel radius — v1 randomized this per tunnel which caused the
    // width variance the user wants to eliminate. A constant keeps every
    // tunnel a consistent ~3 blocks wide.
    public static float OLD_WORLD_V2_TUNNEL_RADIUS = 1.5f;

    // Y-range policy for cluster origins.
    public static int   OLD_WORLD_V2_TOP_CAP = 110;
    public static int   OLD_WORLD_V2_BEDROCK_BUFFER = 6;
}