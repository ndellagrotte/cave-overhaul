package wftech.cavesrevisited;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import wftech.cavesrevisited.biomemodifiers.InitBiomeModifiers;
import wftech.cavesrevisited.carvertypes.InitCarverTypes;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(CavesRevisited.MOD_ID)
public class CavesRevisited
{
    // Directly reference a slf4j logger
    public static final Logger LOGGER = LogUtils.getLogger();
    public static final String MOD_ID = "cavesrevisited";
    
    public CavesRevisited()
    {
        IEventBus eventBus = FMLJavaModLoadingContext.get().getModEventBus();
        //Init.registerDeferred(eventBus);
        InitCarverTypes.registerDeferred(eventBus);
        InitBiomeModifiers.registerDeferred(eventBus);
        MinecraftForge.EVENT_BUS.register(this);
    }
}
