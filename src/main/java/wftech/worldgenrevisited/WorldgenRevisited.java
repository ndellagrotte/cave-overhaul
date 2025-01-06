package wftech.worldgenrevisited;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.NoSuchElementException;

import org.slf4j.Logger;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.electronwill.nightconfig.core.AbstractCommentedConfig;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.logging.LogUtils;

import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraftforge.common.CreativeModeTabRegistry;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.registries.ForgeRegistry;
import net.minecraftforge.registries.GameData;
import net.minecraftforge.registries.ObjectHolderRegistry;
import net.minecraftforge.registries.RegisterEvent;
import net.minecraftforge.registries.RegistryManager;
import wftech.worldgenrevisited.biomemodifiers.InitBiomeModifiers;
import wftech.worldgenrevisited.carvertypes.InitCarverTypes;
import wftech.worldgenrevisited.commands.ListAllCarversCommand;
import wftech.worldgenrevisited.commands.ListAllFeaturesCommand;
import wftech.worldgenrevisited.commands.ShowFeatureDetailsCommand;
import wftech.worldgenrevisited.hooks.PotentialSpawnsEvent;
import wftech.worldgenrevisited.virtualpack.AddPackFindersEventWatcher;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(WorldgenRevisited.MOD_ID)
public class WorldgenRevisited
{
    // Directly reference a slf4j logger
    public static final Logger LOGGER = LogUtils.getLogger();
    public static final String TOML_FILE_NAME = "worldgenrevisited.toml";
    public static final String MOD_ID = "worldgenrevisited";
    
    public static AbstractCommentedConfig EARLY_LOAD_CONFIG = null;
    
    public WorldgenRevisited()
    {
        IEventBus eventBus = FMLJavaModLoadingContext.get().getModEventBus();
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Config.SPEC, TOML_FILE_NAME);
        //Init.registerDeferred(eventBus);
        InitCarverTypes.registerDeferred(eventBus);
        InitBiomeModifiers.registerDeferred(eventBus);

        //eventBus.addListener(WorldgenRevisited::registerThings2);
        eventBus.addListener(AddPackFindersEventWatcher::watch);
        
        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.register(PotentialSpawnsEvent.class);
        
        CommentedFileConfig tempConfig = CommentedFileConfig.of(FMLPaths.CONFIGDIR.get().resolve(WorldgenRevisited.TOML_FILE_NAME));
        tempConfig.load();
        try {
        	EARLY_LOAD_CONFIG = (AbstractCommentedConfig) tempConfig.get(tempConfig.valueMap().keySet().iterator().next());
        } catch (NoSuchElementException e) {
        	LOGGER.error("WorldgenRevisited config not yet generated! Making a new config.");
        }
    }
    
    /*
     * Listener for event class net.minecraftforge.event.RegisterCommandsEvent 
     * takes an argument that is not a subtype of the 
     * base type interface net.minecraftforge.fml.event.IModBusEvent

     */
	@SubscribeEvent
    public void registerCommands(RegisterCommandsEvent event) {
    	CommandDispatcher dispatch = event.getDispatcher();
    	ListAllFeaturesCommand.register(dispatch);
    	ListAllCarversCommand.register(dispatch);
    	//ShowFeatureDetailsCommand.register(dispatch);
    }
	
	@SuppressWarnings("static-access")
	@SubscribeEvent
    public static void registerThings2(final ModConfigEvent.Loading event) {
		
		List<ResourceLocation> names = RegistryManager.getRegistryNamesForSyncToClient();
		//HashSet<ResourceLocation> keySet = new HashSet<ResourceLocation>(RegistryManager.ACTIVE.registries.keySet());
		names.addAll(RegistryManager.getVanillaRegistryKeys());
        //keySet.addAll();
        LinkedHashSet<ResourceLocation> ordered = new LinkedHashSet<ResourceLocation>(MappedRegistry.getKnownRegistries());
        ordered.retainAll(names);
        ordered.addAll(names.stream().sorted(ResourceLocation::compareNamespaced).toList());
        RuntimeException aggregate = new RuntimeException();
        
        
		//LOGGER.debug("[WorldgenRevisited] Trying to get configured features -> " + RegistryManager.ACTIVE.getRegistry(Registries.PLACED_FEATURE));
		

		/*
        HashSet<ResourceLocation> keySet = new HashSet<ResourceLocation>(RegistryManager.ACTIVE.registries.keySet());
        keySet.addAll(RegistryManager.getVanillaRegistryKeys());
        LinkedHashSet<ResourceLocation> ordered = new LinkedHashSet<ResourceLocation>(MappedRegistry.getKnownRegistries());
        ordered.retainAll(keySet);
        ordered.addAll(keySet.stream().sorted(ResourceLocation::compareNamespaced).toList());
        RuntimeException aggregate = new RuntimeException();
        for (ResourceLocation rootRegistryName : ordered) {
            try {
                ResourceKey registryKey = ResourceKey.createRegistryKey(rootRegistryName);
                ForgeRegistry forgeRegistry = RegistryManager.ACTIVE.getRegistry(rootRegistryName);
                Registry<?> vanillaRegistry = BuiltInRegistries.REGISTRY.get(rootRegistryName);
                RegisterEvent registerEvent = new RegisterEvent(registryKey, forgeRegistry, vanillaRegistry);
                StartupMessageManager.modLoaderConsumer().ifPresent(s -> s.accept("REGISTERING " + registryKey.location()));
                if (forgeRegistry != null) {
                    forgeRegistry.unfreeze();
                }
                ModLoader.get().postEventWrapContainerInModOrder((Event)registerEvent);
                if (forgeRegistry != null) {
                    forgeRegistry.freeze();
                }
                LOGGER.debug(ForgeRegistry.REGISTRIES, "Applying holder lookups: {}", (Object)registryKey.location());
                ObjectHolderRegistry.applyObjectHolders(registryKey.location()::equals);
                LOGGER.debug(ForgeRegistry.REGISTRIES, "Holder lookups applied: {}", (Object)registryKey.location());
            }
            catch (Throwable t) {
                aggregate.addSuppressed(t);
            }
        }
        if (aggregate.getSuppressed().length > 0) {
            LOGGER.fatal("Failed to register some entries, see suppressed exceptions for details", (Throwable)aggregate);
            LOGGER.fatal("Detected errors during registry event dispatch, rolling back to VANILLA state");
            GameData.revertTo(RegistryManager.VANILLA, false);
            LOGGER.fatal("Detected errors during registry event dispatch, roll back to VANILLA complete");
            throw aggregate;
        }
        ForgeHooks.modifyAttributes();
        SpawnPlacements.fireSpawnPlacementEvent();
        CreativeModeTabRegistry.fireCollectionEvent();
		*/
		
		
		
		
		
		
		
        //InitConfigFeatures.init();
    }
}
