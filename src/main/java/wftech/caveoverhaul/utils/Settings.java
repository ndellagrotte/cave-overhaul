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
}