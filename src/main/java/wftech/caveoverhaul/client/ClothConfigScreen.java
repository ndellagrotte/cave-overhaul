package wftech.caveoverhaul.client;

import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import wftech.caveoverhaul.Config;

public class ClothConfigScreen {

    public static Screen create(Screen parent) {
        ConfigBuilder builder = ConfigBuilder.create()
                .setParentScreen(parent)
                .setTitle(Component.translatable("config.caveoverhaul.title"));

        ConfigEntryBuilder entryBuilder = builder.entryBuilder();

        // General category
        ConfigCategory general = builder.getOrCreateCategory(Component.translatable("config.caveoverhaul.category.general"));

        general.addEntry(entryBuilder.startBooleanToggle(
                        Component.translatable("config.caveoverhaul.generate_caverns"),
                        Config.getBoolSetting(Config.KEY_GENERATE_CAVERNS))
                .setDefaultValue(Config.getDefaultBoolValue(Config.KEY_GENERATE_CAVERNS))
                .setTooltip(Component.translatable("config.caveoverhaul.generate_caverns.tooltip"))
                .setSaveConsumer(value -> Config.setBoolSetting(Config.KEY_GENERATE_CAVERNS, value))
                .build());

        // Rivers category
        ConfigCategory rivers = builder.getOrCreateCategory(Component.translatable("config.caveoverhaul.category.rivers"));

        rivers.addEntry(entryBuilder.startBooleanToggle(
                        Component.translatable("config.caveoverhaul.enable_lava_rivers"),
                        Config.getBoolSetting(Config.KEY_LAVA_RIVER_ENABLE))
                .setDefaultValue(Config.getDefaultBoolValue(Config.KEY_LAVA_RIVER_ENABLE))
                .setTooltip(Component.translatable("config.caveoverhaul.enable_lava_rivers.tooltip"))
                .setSaveConsumer(value -> Config.setBoolSetting(Config.KEY_LAVA_RIVER_ENABLE, value))
                .build());

        rivers.addEntry(entryBuilder.startBooleanToggle(
                        Component.translatable("config.caveoverhaul.enable_water_rivers"),
                        Config.getBoolSetting(Config.KEY_WATER_RIVER_ENABLE))
                .setDefaultValue(Config.getDefaultBoolValue(Config.KEY_WATER_RIVER_ENABLE))
                .setTooltip(Component.translatable("config.caveoverhaul.enable_water_rivers.tooltip"))
                .setSaveConsumer(value -> Config.setBoolSetting(Config.KEY_WATER_RIVER_ENABLE, value))
                .build());

        float[] lavaOffsetRange = Config.getFloatRange(Config.KEY_LAVA_OFFSET);
        rivers.addEntry(entryBuilder.startFloatField(
                        Component.translatable("config.caveoverhaul.lava_offset"),
                        Config.getFloatSetting(Config.KEY_LAVA_OFFSET))
                .setDefaultValue(Config.getDefaultFloatValue(Config.KEY_LAVA_OFFSET))
                .setMin(lavaOffsetRange[0])
                .setMax(lavaOffsetRange[1])
                .setTooltip(Component.translatable("config.caveoverhaul.lava_offset.tooltip"))
                .setSaveConsumer(value -> Config.setFloatSetting(Config.KEY_LAVA_OFFSET, value))
                .build());

        // Caves category
        ConfigCategory caves = builder.getOrCreateCategory(Component.translatable("config.caveoverhaul.category.caves"));

        float[] caveChanceRange = Config.getFloatRange(Config.KEY_CAVE_CHANCE);
        caves.addEntry(entryBuilder.startFloatField(
                        Component.translatable("config.caveoverhaul.cave_chance"),
                        Config.getFloatSetting(Config.KEY_CAVE_CHANCE))
                .setDefaultValue(Config.getDefaultFloatValue(Config.KEY_CAVE_CHANCE))
                .setMin(caveChanceRange[0])
                .setMax(caveChanceRange[1])
                .setTooltip(Component.translatable("config.caveoverhaul.cave_chance.tooltip"))
                .setSaveConsumer(value -> Config.setFloatSetting(Config.KEY_CAVE_CHANCE, value))
                .build());

        float[] caveAirRange = Config.getFloatRange(Config.KEY_CAVE_AIR_EXPOSURE);
        caves.addEntry(entryBuilder.startFloatField(
                        Component.translatable("config.caveoverhaul.cave_air_exposure"),
                        Config.getFloatSetting(Config.KEY_CAVE_AIR_EXPOSURE))
                .setDefaultValue(Config.getDefaultFloatValue(Config.KEY_CAVE_AIR_EXPOSURE))
                .setMin(caveAirRange[0])
                .setMax(caveAirRange[1])
                .setTooltip(Component.translatable("config.caveoverhaul.cave_air_exposure.tooltip"))
                .setSaveConsumer(value -> Config.setFloatSetting(Config.KEY_CAVE_AIR_EXPOSURE, value))
                .build());

        float[] verticalStretchRange = Config.getFloatRange(Config.KEY_CAVE_VERTICAL_STRETCH);
        caves.addEntry(entryBuilder.startFloatField(
                        Component.translatable("config.caveoverhaul.cave_vertical_stretch"),
                        Config.getFloatSetting(Config.KEY_CAVE_VERTICAL_STRETCH))
                .setDefaultValue(Config.getDefaultFloatValue(Config.KEY_CAVE_VERTICAL_STRETCH))
                .setMin(verticalStretchRange[0])
                .setMax(verticalStretchRange[1])
                .setTooltip(Component.translatable("config.caveoverhaul.cave_vertical_stretch.tooltip"))
                .setSaveConsumer(value -> Config.setFloatSetting(Config.KEY_CAVE_VERTICAL_STRETCH, value))
                .build());

        // Canyons category
        ConfigCategory canyons = builder.getOrCreateCategory(Component.translatable("config.caveoverhaul.category.canyons"));

        float[] canyonUpperRange = Config.getFloatRange(Config.KEY_CANYON_UPPER_CHANCE);
        canyons.addEntry(entryBuilder.startFloatField(
                        Component.translatable("config.caveoverhaul.canyon_upper_chance"),
                        Config.getFloatSetting(Config.KEY_CANYON_UPPER_CHANCE))
                .setDefaultValue(Config.getDefaultFloatValue(Config.KEY_CANYON_UPPER_CHANCE))
                .setMin(canyonUpperRange[0])
                .setMax(canyonUpperRange[1])
                .setTooltip(Component.translatable("config.caveoverhaul.canyon_upper_chance.tooltip"))
                .setSaveConsumer(value -> Config.setFloatSetting(Config.KEY_CANYON_UPPER_CHANCE, value))
                .build());

        float[] canyonLowerRange = Config.getFloatRange(Config.KEY_CANYON_LOWER_CHANCE);
        canyons.addEntry(entryBuilder.startFloatField(
                        Component.translatable("config.caveoverhaul.canyon_lower_chance"),
                        Config.getFloatSetting(Config.KEY_CANYON_LOWER_CHANCE))
                .setDefaultValue(Config.getDefaultFloatValue(Config.KEY_CANYON_LOWER_CHANCE))
                .setMin(canyonLowerRange[0])
                .setMax(canyonLowerRange[1])
                .setTooltip(Component.translatable("config.caveoverhaul.canyon_lower_chance.tooltip"))
                .setSaveConsumer(value -> Config.setFloatSetting(Config.KEY_CANYON_LOWER_CHANCE, value))
                .build());

        float[] canyonAirRange = Config.getFloatRange(Config.KEY_CANYON_UPPER_AIR_EXPOSURE);
        canyons.addEntry(entryBuilder.startFloatField(
                        Component.translatable("config.caveoverhaul.canyon_air_exposure"),
                        Config.getFloatSetting(Config.KEY_CANYON_UPPER_AIR_EXPOSURE))
                .setDefaultValue(Config.getDefaultFloatValue(Config.KEY_CANYON_UPPER_AIR_EXPOSURE))
                .setMin(canyonAirRange[0])
                .setMax(canyonAirRange[1])
                .setTooltip(Component.translatable("config.caveoverhaul.canyon_air_exposure.tooltip"))
                .setSaveConsumer(value -> Config.setFloatSetting(Config.KEY_CANYON_UPPER_AIR_EXPOSURE, value))
                .build());

        builder.setSavingRunnable(Config::saveConfig);

        return builder.build();
    }
}
