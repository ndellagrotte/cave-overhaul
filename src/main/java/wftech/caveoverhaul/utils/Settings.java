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

    // Old World Caves v2 — noise-trace + dilation.
    // Two ridged OpenSimplex2 fields are sampled with domain-warped coords;
    // their intersection traces a sparse 1-voxel lattice of curves. Dilation
    // then expands each traced voxel into a uniform-width tunnel.
    public static float OLD_WORLD_V2_FREQUENCY = 0.022f;
    public static int   OLD_WORLD_V2_OCTAVES = 2;
    public static float OLD_WORLD_V2_LACUNARITY = 2.0f;
    public static float OLD_WORLD_V2_GAIN = 0.5f;
    // High threshold thins the ridged intersection toward a 1-voxel centerline.
    // Dilation below controls the final tunnel width, not this knob — raise
    // this to make the centerline sparser (fewer tunnels), not narrower.
    public static float OLD_WORLD_V2_RIDGE_THRESHOLD = 0.80f;
    // Vertical stretch on noise y-input. Biases traces toward horizontal so
    // tunnels are playable; values above 1.0 make slopes gentler on average.
    public static float OLD_WORLD_V2_Y_SCALE = 2.0f;

    // Dilation radius in blocks (axis-aligned cross). Radius 1 → ~3-wide
    // tunnels everywhere, independent of the centerline's local crossing angle.
    public static int   OLD_WORLD_V2_DILATE_RADIUS = 1;

    // Domain warp — perturbs the sampling coords so tunnels meander instead of
    // tracking the OpenSimplex grid. Strength is max displacement in blocks;
    // frequency sets the warp wavelength (lower → longer, smoother curves).
    public static float OLD_WORLD_V2_WARP_FREQUENCY = 0.015f;
    public static float OLD_WORLD_V2_WARP_STRENGTH = 8.0f;

    // Y-range policy.
    public static int   OLD_WORLD_V2_TOP_CAP = 110;
    public static int   OLD_WORLD_V2_BEDROCK_BUFFER = 6;
    public static int   OLD_WORLD_V2_TOP_FADE_ZONE = 20;
    public static float OLD_WORLD_V2_TOP_FADE_STRENGTH = 0.10f;
}