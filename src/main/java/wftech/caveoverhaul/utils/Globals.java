package wftech.caveoverhaul.utils;

import net.fabricmc.loader.api.FabricLoader;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class Globals {
    private static final AtomicInteger minYAtomic = new AtomicInteger(0);
    private static final AtomicBoolean volcanicCavernsCheckComplete = new AtomicBoolean(false);
    private static volatile boolean isVolcanicCavernsLoaded = false;

    public static int getMinY() {
        return minYAtomic.get();
    }

    public static void setMinY(int minY) {
        minYAtomic.set(minY);
    }

    public static boolean isVolcanicCavernsLoaded() {
        init();
        return isVolcanicCavernsLoaded;
    }

    public static void init() {
        if (volcanicCavernsCheckComplete.compareAndSet(false, true)) {
            isVolcanicCavernsLoaded = FabricLoader.getInstance().isModLoaded("volcanic_caverns");
        }
    }

    public static void reset() {
        minYAtomic.set(0);
        volcanicCavernsCheckComplete.set(false);
        isVolcanicCavernsLoaded = false;
    }
}