package wftech.worldgenrevisited;

import java.util.ArrayList;
import java.util.List;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.ConfigValue;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class Config {

    public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec SPEC;

    public static final ForgeConfigSpec.ConfigValue<Integer> WORLDGEN_TYPE;
    public static final ConfigValue<List<String>> REMOVE_FEATURES_LIST;
    //public static final ConfigValue<List<String>> REMOVE_CARVERS_LIST;
    //public static final ConfigValue<List<String>> ADD_CUSTOM_FEATURES_FEATURES_LIST;
    //public static final ConfigValue<List<String>> ADD_CUSTOM_CARVERS_FEATURES_LIST;
    public static final ConfigValue<List<String>> CHANGE_ORE_FREQUENCY_LIST;
    public static final ConfigValue<List<String>> CHANGE_MULTIPLIED_CARVER_FREQUENCY_LIST;
    public static final ConfigValue<List<String>> CHANGE_REPLACED_CARVER_FREQUENCY_LIST;
    //public static final ConfigValue<List<String>> CHANGE_CARVER_FREQUENCY_LIST;
    public static final ConfigValue<Boolean> ENABLE_DEEPSLATE;
    //public static final ConfigValue<Boolean> ENABLE_CLEAN_CAVES;
    public static final ConfigValue<Boolean> ENABLE_CHEESE_AND_SPAGHETTI_CAVES;
    public static final ConfigValue<Boolean> ENABLE_DEEPSLATE_ORES_WHEN_DEEPSLATE_IS_DISABLED;
    public static final ConfigValue<Double> PERC_PIERCE_SURFACE;
    public static final ConfigValue<Double> PERC_PIERCE_SURFACE_CANYON;
    public static final ConfigValue<List<String>> SPAWNS_TO_DISABLE;
    public static final ConfigValue<List<String>> MAX_SPAWNS_PER_LEVEL;

    public static final String KEY_WORLDGEN_TYPE = "worldgen_type";
    public static final String KEY_ENABLE_DEEPSLATE = "enable_deepslate";
    public static final String KEY_PERC_PIERCE_SURFACE = "percentage_caves_piece_surface";
    public static final String KEY_PERC_PIERCE_SURFACE_CANYON = "percentage_canyon_piece_surface";
    public static final String KEY_ENABLE_CHEESE_AND_SPAGHETTI_CAVES = "enable_cheese_and_spaghetti_caves";
    public static final String KEY_REMOVE_FEATURES = "features_to_remove";
    public static final String KEY_ORE_FREQUENCY_CHANGE = "feature_frequencies";
    public static final String KEY_CARVER_MULTIPLIED_FREQUENCY_CHANGE = "carver_multiply_frequencies";
    public static final String KEY_CARVER_REPLACED_FREQUENCY_CHANGE = "carver_override_frequencies";
    public static final String KEY_ENABLE_DEEPSLATE_ORES_WHEN_DEEPSLATE_IS_DISABLED = "enable_deepslate_ores_when_deepslate_is_disabled";

    //Spawns
    public static final String KEY_SPAWNS_TO_DISABLE = "spawns_to_disable";
    public static final String KEY_MAX_SPAWNS_PER_LEVEL = "max_mob_spawns_per_level";
    public static final String KEY_ADJUST_SPAWNS = "adjust_mob_spawns";
    public static final String KEY_MOB_SPAWN_CLUSTER_SIZE = "mob_spawn_cluster_size";
    public static final String KEY_MOB_RESTRICT_SPAWNS = "restrict_mob_spawns_to_biomes";
    
    static {
        BUILDER.push("Configs for WorldgenRevisited");

        WORLDGEN_TYPE = BUILDER.comment("-1 = No carvers, not even vanilla. 0 = 1.12 retrogen, 1 = 1.16, 2 = current version vanilla.\n"
        		+ "-2 for **HIGHLY** experimental noise caves. These require a beefy computer at the moment, but work is being done to optimize these caves.\n"
        		+ "1.12 mode is suggested as 1.16 has issues regarding tunnels not connecting properly.\nDefault=1")
                .define(KEY_WORLDGEN_TYPE, 2);
        
        BUILDER.comment("");

        ENABLE_CHEESE_AND_SPAGHETTI_CAVES = BUILDER.comment("Enable or disable cheese and spaghetti caves (1.18+ caves).\n"
        		+ "These caves are the larger caverns and connecting caves found in modern Minecraft. \nDefault=true")
                .define(KEY_ENABLE_CHEESE_AND_SPAGHETTI_CAVES, true);
        
        BUILDER.comment("");

        /*
        ENABLE_CLEAN_CAVES = BUILDER.comment("(WIP, non-functional) Change default stone type along tunnel walls to stone/andesite/deepslate (if deepslate is enabled). "
        		+ "Only affects WorldgenRevisited tunnels and caves. "
        		+ "Non-CR tunnels and caves will still show non-stone/deepslate rock types. Default=false")
                .define("enable_clean_caves", false);
		*/

        ENABLE_DEEPSLATE = BUILDER.comment("Eable/disable deepslate generation.\nDefault=true")
    			.define(KEY_ENABLE_DEEPSLATE, true);
        
        BUILDER.comment("");

        ENABLE_DEEPSLATE_ORES_WHEN_DEEPSLATE_IS_DISABLED = BUILDER.comment("Enable/disable deepslate ores when deepslate generation is disable. \nDefault=false")
    			.define(KEY_ENABLE_DEEPSLATE_ORES_WHEN_DEEPSLATE_IS_DISABLED, false);
        
        BUILDER.comment("");

        /*
        ADD_CUSTOM_CARVERS_FEATURES_LIST = BUILDER.comment("(WIP, non-functional) Encode new cave carvers or change the behavior of "
        		+ "existing carvers, such as canyon gen. Default=[]")
    			.define("carvers_to_add", new ArrayList<String>());
		*/

        /*
        REMOVE_CARVERS_LIST = BUILDER.comment("Remove specific carvers. Follows the format of 'features_to_remove.'"
        		+ "Use /listallcarvers <partial name> to list active carvers. Example: [\"worldgenrevisited:oldcanyonconfigured\"] Default=[]")
        		.define("carvers_to_remove", new ArrayList<String>());

        CHANGE_CARVER_FREQUENCY_LIST = BUILDER.comment("Adjusts feature frequencies. Use /listallcarvers <partial carver name> to "
        		+ "list active carvers. Example: [\"worldgenrevisited:oldcanyonconfigured=3\",\"minecraft:carver123=0.5\"] Default=[]")
    			.define("carver_frequencies", new ArrayList<String>());
		*/

        /*
        ADD_CUSTOM_FEATURES_FEATURES_LIST = BUILDER.comment("(WIP, non-functional) Encode new cave features or change the behavior of "
        		+ "existing features, such as ore gen. Default=[]")
    			.define("fearures_to_add", new ArrayList<String>());
		*/

        REMOVE_FEATURES_LIST = BUILDER.comment("World features (including ore groups, stone groups, non-cave features, etc) to remove.\n"
        		+ "Use /listallfeatures <partial name> to list active features. Tuff and granite might not be fully removed due to \n"
        		+ "an existing bug. Features from certain mods might not be removed properly at this time due to an existing bug. \n"
        		+ "Example entry: [\"minecraft:ore_diorite_upper\", \"minecraft:ore_diorite_lower\", \"minecraft:trees_savanna\"]. \nDefault=[]")
        		.define(KEY_REMOVE_FEATURES, new ArrayList<String>());
        
        BUILDER.comment("");

        CHANGE_ORE_FREQUENCY_LIST = BUILDER.comment("Change ore frequencies. Use /listallfeatures <likely ore name> to get the ore key.\n"
        		+ "Example entry: [\"minecraft:ore_diorite_upper=3\", \"minecraft:ore_diorite_lower=0.2\"],\n"
        		+ "where 3 will 3x the ore and 0.2 will 0.2x the ore. Specific feature types have a maximum frequency. Setting\n"
        		+ "a value above that frequency will cap the frequency to that feature's maximum automatically. Only works on \n"
        		+ "ore/underground features at this time. Might not work on modded ores. \nDefault=[]")
        		.defineList(KEY_ORE_FREQUENCY_CHANGE, new ArrayList(), x -> true );
    			//.define("feature_frequencies", new ArrayList<String>());
        
        BUILDER.comment("");

        CHANGE_MULTIPLIED_CARVER_FREQUENCY_LIST = BUILDER.comment("Change carver/cave frequencies. Use /listallcarvers to get a list of active carvers.\n"
        		+ "This option does not impact the frequencies of large/organic looking caves. This option "
        		+ "only affects canyons and tunnels. Acts like feature frequency multipliers, will 3x or "
        		+ "0.5x the default carver rate. Can be used to customize worldgen type carver frequencies. Default=[]")
        		.defineList(KEY_CARVER_MULTIPLIED_FREQUENCY_CHANGE, new ArrayList(), x -> true );
        
        BUILDER.comment("");

        CHANGE_REPLACED_CARVER_FREQUENCY_LIST = BUILDER.comment("Override carver/cave frequencies. Use /listallcarvers to get a list of active carvers with their probabilities.\n"
        		+ "Requires the exact probability. For instance, \"worldgenrevisited:v12_caves=0.4\" for 1.12 style caves (default is 0.15, sets to 0.4). "
        		+ "Useful defaults: minecraft:canyon=0.01, minecraft:cave=0.15, (extra underground) minecraft:cave=0.07, "
        		+ "minecraft:nether_cave=0.2. Can be used to customize worldgen type carver frequencies. Default=[]")
        		.defineList(KEY_CARVER_REPLACED_FREQUENCY_CHANGE, new ArrayList(), x -> true );
        
        BUILDER.comment("");

        SPAWNS_TO_DISABLE = BUILDER.comment("List of mob spawns to disable. Use /listallmobs <partial> to get a list of all mobs. Supports fuzzy matches, "
        		+ "so if you want to disable all aquaculture mobs alongside specifically vampirism:hunter, you can use [\"aquaculture\", \"vampirism:hunter\"].\n"
        		+ "Default=[]")
        		.defineList(KEY_SPAWNS_TO_DISABLE, new ArrayList(), x -> true );
        
        BUILDER.comment("");

        MAX_SPAWNS_PER_LEVEL = BUILDER.comment("Use <dimension>=<cap> to set a cap on the number of mobs per dimension/level. No cap = no max. Dimensions = the Overworld, "
        		+ "the Nether, the End, the Twilight Forest, etc. /listalldimensions to list all dimensions/levels. /showcurrentmobcount to list the current number of "
        		+ "mobs in a dimension.\nNote that unloaded chunks will not count towards this cap. This cap includes ALL mobs. Use \"*=<count>\" to set a cap "
        		+ "for all dimensions at once.\n"
        		+ "Default=[]")
        		.defineList(KEY_MAX_SPAWNS_PER_LEVEL, new ArrayList(), x -> true );

        BUILDER.comment("");


        PERC_PIERCE_SURFACE = BUILDER.comment("% of caves that should pierce world surface (does not apply to large caverns). "
        		+ "Default=0.3")
        		.define(KEY_PERC_PIERCE_SURFACE, 0.3);
        
        BUILDER.comment("");

        PERC_PIERCE_SURFACE_CANYON = BUILDER.comment("% of canyons that should pierce world surface (does not apply to large caverns). "
        		+ "Default=0.3")
        		.define(KEY_PERC_PIERCE_SURFACE_CANYON, 0.3);
        
        BUILDER.pop();
        SPEC = BUILDER.build();
    }
    
}
