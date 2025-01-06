package wftech.caveoverhaul.utils;
import net.fabricmc.loader.api.FabricLoader;

public class Globals {
    public static int minY = 0;
    public static boolean volcanicCavernsCheckComplete = false;
    public static boolean isVolcanicCavernsLoaded = false;
    public static void init(){
        if (volcanicCavernsCheckComplete) {
            return;
        }

        volcanicCavernsCheckComplete = true;
        isVolcanicCavernsLoaded = FabricLoader.getInstance().isModLoaded("volcanic_caverns");
    }
}