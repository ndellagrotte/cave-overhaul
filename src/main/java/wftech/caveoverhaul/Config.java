package wftech.caveoverhaul;

import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.*;

/*
Fake YAML because I'm a fake person
 */
public class Config {

    private static final Map<String, Boolean> boolSettings = new HashMap<>();
    private static final Map<String, Float> floatSettings = new HashMap<>();

    public static String KEY_CAVE_CHANCE = "cave_chance";
    public static String KEY_CAVE_AIR_EXPOSURE = "cave_air_exposure_chance";
    public static String KEY_CANYON_UPPER_CHANCE = "canyon_upper_chance";
    public static String KEY_CANYON_UPPER_AIR_EXPOSURE = "canyon_air_exposure_chance";
    public static String KEY_CANYON_LOWER_CHANCE = "canyon_lower_chance";
    public static String KEY_GENERATE_CAVERNS = "generate_minecraft_caverns";
    public static String KEY_USE_AQUIFER_PATCH = "use_aquifer_patch";

    //1.3.4
    public static String KEY_LAVA_RIVER_ENABLE = "enable_lava_rivers";
    public static String KEY_WATER_RIVER_ENABLE = "enable_water_rivers";
    public static String KEY_LAVA_OFFSET = "bottom_lava_offset";
    //public static String KEY_ENABLE_CAVES_BELOW_MINUS_Y64 = "enable_caves_below_minus_y64";
    //public static String KEY_USE_LEGACY_OVERWORLD_DETECTION = "use_legacy_overworld_detection";

    private static final String[] validKeys = {
            KEY_CAVE_CHANCE,
            KEY_CAVE_AIR_EXPOSURE,
            KEY_CANYON_UPPER_CHANCE,
            KEY_CANYON_LOWER_CHANCE,
            KEY_CANYON_UPPER_AIR_EXPOSURE,
            KEY_GENERATE_CAVERNS,
            KEY_USE_AQUIFER_PATCH,

            //1.3.4
            KEY_LAVA_RIVER_ENABLE,
            KEY_WATER_RIVER_ENABLE,
            KEY_LAVA_OFFSET
    };

    private static final String[] boolKeys = {
            KEY_GENERATE_CAVERNS,
            KEY_USE_AQUIFER_PATCH,

            //1.3.4
            KEY_LAVA_RIVER_ENABLE,
            KEY_WATER_RIVER_ENABLE,
    };

    private static final Map<String, Boolean> DEFAULT_BOOL_VALUES = new HashMap<>();
    private static final Map<String, Float> DEFAULT_FLOAT_VALUES = new HashMap<>();

    // Validation ranges for float settings: [min, max]
    private static final Map<String, float[]> FLOAT_RANGES = new HashMap<>();
    static {
        FLOAT_RANGES.put(KEY_CAVE_CHANCE, new float[]{0f, 1f});
        FLOAT_RANGES.put(KEY_CAVE_AIR_EXPOSURE, new float[]{0f, 1f});
        FLOAT_RANGES.put(KEY_CANYON_UPPER_CHANCE, new float[]{0f, 1f});
        FLOAT_RANGES.put(KEY_CANYON_UPPER_AIR_EXPOSURE, new float[]{0f, 1f});
        FLOAT_RANGES.put(KEY_CANYON_LOWER_CHANCE, new float[]{0f, 1f});
        FLOAT_RANGES.put(KEY_LAVA_OFFSET, new float[]{0f, 64f});
    }

    public static boolean getBoolSetting(String key){
        return boolSettings.getOrDefault(key, DEFAULT_BOOL_VALUES.getOrDefault(key, false));
    }

    public static float getFloatSetting(String key){
        return floatSettings.getOrDefault(key, DEFAULT_FLOAT_VALUES.getOrDefault(key, 0f));
    }

    private static boolean isValidKey(String key){
        for(String entry: validKeys){
            if (entry.equals(key)){
                return true;
            }
        }

        return false;
    }

    private static float clampFloatValue(String key, float value) {
        float[] range = FLOAT_RANGES.get(key);
        if (range == null) {
            return value;
        }
        float min = range[0];
        float max = range[1];
        if (value < min) {
            LoggerFactory.getLogger("caveoverhaul").warn(
                    "[WFs Cave Overhaul] Config value for '{}' ({}) is below minimum ({}), clamping to {}",
                    key, value, min, min);
            return min;
        }
        if (value > max) {
            LoggerFactory.getLogger("caveoverhaul").warn(
                    "[WFs Cave Overhaul] Config value for '{}' ({}) is above maximum ({}), clamping to {}",
                    key, value, max, max);
            return max;
        }
        return value;
    }

    private static void initFile(File file){

        /*
        Create the config file if it's missing
         */

        if (!file.exists()) {

            try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
                writer.write("# All *_air_exposure_chance entries dictate the odds that caves/canyons will carve through the surface\n");
                writer.write("# All *_chance entries dictate the odds that caves/canyons will spawn\n");
                writer.write("# Upper canyons are y < 128 to bedrock\n");
                writer.write("# Lower canyons are y < 48 to bedrock\n");
                writer.write("# The two canyons types overlap :)\n");
                writer.write("#\n");
                writer.write("# Caves refer to old pre-1.18 minecraft tunnels and not the noise caverns added by this mod.\n");
                writer.write("# Caverns refer to the ultra-large caves added in 1.18. Enabling these will have a minor decrease in worldgen performance.\n");
                writer.write("# Likewise, enabling caverns could result in awkward terrain as the noise rules are way different from Cave Overhaul's.\n");
                writer.write("# Enabling minecraft's caverns will re-enable the default worldgen. If you experience any mod conflicts, consider enabling the caverns option.\n");
                writer.write("#\n");
                writer.write("# Lava offset = fill the bottom x air blocks of the world with lava. Change this if the bottom-of-the-world lava looks weird.\n");
                writer.write("# Enable rivers = disable or enable this river type.\n");
                writer.write("#\n");
                writer.write("# The aquifer patch fixes water-related issues, but could impact worldgen speed.\n");
                writer.write("# The format is <key>=<value> with no spaces\n");
                writer.write("# Please use true/false or numbers only. T/F/yes/no/Y/N will not be read properly.\n");
                writer.write("#\n");
                writer.write("# Suggested rates if caverns are enabled: \n");
                writer.write("# cave_air_exposure_chance=0.5\n");
                writer.write("# canyon_air_exposure_chance=0.5\n");
                writer.write("# cave_chance=0.02\n");
                writer.write("# canyon_upper_chance=0.04\n");
                writer.write("# canyon_lower_chance=0.02\n");
                writer.write("\n");
            } catch (IOException e) {
                LoggerFactory.getLogger("caveoverhaul").error("[WFs Cave Overhaul] Failed to create config file.", e);
            }

            addMissingKeys(file, new HashSet<>(Arrays.asList(validKeys)));
        }

    }

    private static HashSet<String> gatherAndInitSettings(File file){

        /*
        Collect known keys
         */
        HashSet<String> discoveredKeysSet = new HashSet<>();
        List<String> boolKeysArr = Arrays.asList(boolKeys);
//        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
//            // Read the content of the file
//            String line;
//            while ((line = reader.readLine()) != null) {
//                if (line.startsWith("#")) {
//                } else if (line.startsWith("[")) {
//                } else {
//                    String[] parts = line.split("=");
//
//                    if (parts.length == 0){
//                        continue;
//                    }
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("#") || line.startsWith("[")) {
                    continue;
                }

                String[] parts = line.split("=");
                if (parts.length == 0) {
                    continue;
                }
                if (isValidKey(parts[0])) {
                    if (boolKeysArr.contains(parts[0])) {
                        boolean value = Boolean.parseBoolean((parts[1].strip().toLowerCase()));
                        boolSettings.put(parts[0], value);
                        discoveredKeysSet.add(parts[0]);
                    } else {
                        float value = Float.parseFloat(parts[1].strip());
                        value = clampFloatValue(parts[0], value);
                        floatSettings.put(parts[0], value);
                        discoveredKeysSet.add(parts[0]);
                    }
                }
            }
        } catch (IOException e) {
            LoggerFactory.getLogger("caveoverhaul").error("[WFs Cave Overhaul] Failed to read config.");
            for (StackTraceElement line : e.getStackTrace()) {
                LoggerFactory.getLogger("caveoverhaul").error(line.toString());
            }
        } catch (NumberFormatException e) {
            LoggerFactory.getLogger("caveoverhaul").error("[WFs Cave Overhaul] Failed to parse config entry.");
            for (StackTraceElement line : e.getStackTrace()) {
                LoggerFactory.getLogger("caveoverhaul").error(line.toString());
            }
        }

        return discoveredKeysSet;

    }

    private static void fixConfig(File file, HashSet<String> missingKeysAsSet){
        /*
        Re-add missing keys or add new missing keys
         */

        if(!missingKeysAsSet.isEmpty()) {

            try (BufferedWriter writer = new BufferedWriter(new FileWriter(file, true))) {
                writer.write("\n# Added missing keys with their default values:\n\n");
            } catch (IOException e) {
                LoggerFactory.getLogger("caveoverhaul").error("[WFs Cave Overhaul] Failed to update config!");
                for(StackTraceElement line: e.getStackTrace()){
                    LoggerFactory.getLogger("caveoverhaul").error(line.toString());
                }
            }

            addMissingKeys(file, missingKeysAsSet);
        }
    }

    private static void addMissingKeys(File file, Set<String> missingKeysAsSet){

        List<String> boolKeysArr = Arrays.asList(boolKeys);

        for (String missingKey : missingKeysAsSet) {
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(file, true))) {
                if (boolKeysArr.contains(missingKey)){
                    boolean val = DEFAULT_BOOL_VALUES.getOrDefault(missingKey, false);
                    writer.write(missingKey + "=" + val + "\n");
                } else {
                    float val = DEFAULT_FLOAT_VALUES.getOrDefault(missingKey, 0f);
                    writer.write(missingKey + "=" + val + "\n");
                }
            } catch (IOException e) {
                LoggerFactory.getLogger("caveoverhaul").error("[WFs Cave Overhaul] Failed to add missing key {} to config!", missingKey);
                for(StackTraceElement line: e.getStackTrace()){
                    LoggerFactory.getLogger("caveoverhaul").error(line.toString());
                }
            }

        }
    }

    private static File generateFile(String relativePath, String fileName){

        File directory = new File(relativePath);
//        if (!directory.exists()) {
//            directory.mkdirs();
//        }

        return new File(directory, fileName);
    }

    private static void initDefaultValues(){
        if (DEFAULT_FLOAT_VALUES.isEmpty() && DEFAULT_BOOL_VALUES.isEmpty()) {
            // Float settings
            DEFAULT_FLOAT_VALUES.put(KEY_CAVE_CHANCE, 0.12f);
            DEFAULT_FLOAT_VALUES.put(KEY_CANYON_UPPER_CHANCE, 0.12f);
            DEFAULT_FLOAT_VALUES.put(KEY_CANYON_LOWER_CHANCE, 0.04f);
            DEFAULT_FLOAT_VALUES.put(KEY_CAVE_AIR_EXPOSURE, 0.1f);
            DEFAULT_FLOAT_VALUES.put(KEY_CANYON_UPPER_AIR_EXPOSURE, 0.3f);
            DEFAULT_FLOAT_VALUES.put(KEY_LAVA_OFFSET, 9f);

            // Boolean settings
            DEFAULT_BOOL_VALUES.put(KEY_GENERATE_CAVERNS, false);
            DEFAULT_BOOL_VALUES.put(KEY_USE_AQUIFER_PATCH, false);
            DEFAULT_BOOL_VALUES.put(KEY_LAVA_RIVER_ENABLE, true);
            DEFAULT_BOOL_VALUES.put(KEY_WATER_RIVER_ENABLE, true);
        }
    }

    public static void initConfig() {


        //Init default values for our keys
        initDefaultValues();

        //Create the directory and file object
        String relativePath = "config";
        String fileName = "wfscaveoverhaul.cfg";
        File file = generateFile(relativePath, fileName);

        //Create the config file if it's missing
        initFile(file);

        //Collect known keys
        HashSet<String> discoveredKeysSet = gatherAndInitSettings(file);

        //Re-add missing keys or add new missing keys
        HashSet<String> missingKeysAsSet = new HashSet<>(Arrays.asList(validKeys));
        missingKeysAsSet.removeAll(discoveredKeysSet);
        fixConfig(file, missingKeysAsSet);

    }
}