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

    // Old World Caves v2 — noise geometry.
    // Two ridged OpenSimplex2 fields are sampled at each block; their intersection
    // traces 1D curves through 3D space, producing a labyrinthine tunnel network.
    public static float OLD_WORLD_V2_FREQUENCY = 0.022f;
    public static int   OLD_WORLD_V2_OCTAVES = 2;
    public static float OLD_WORLD_V2_LACUNARITY = 2.0f;
    public static float OLD_WORLD_V2_GAIN = 0.5f;
    // Main density knob. Lower -> denser, wider tunnels. Higher -> sparser, thinner.
    public static float OLD_WORLD_V2_RIDGE_THRESHOLD = 0.70f;
    // Vertical stretch applied to noise y-input. Biases tunnel orientation toward
    // horizontal; values above 1.0 make tunnel slopes gentler on average.
    public static float OLD_WORLD_V2_Y_SCALE = 2.0f;

    // Old World Caves v2 — y-range policy.
    public static int   OLD_WORLD_V2_TOP_CAP = 110;
    public static int   OLD_WORLD_V2_BEDROCK_BUFFER = 6;
    public static int   OLD_WORLD_V2_TOP_FADE_ZONE = 20;
    public static float OLD_WORLD_V2_TOP_FADE_STRENGTH = 0.15f;
}