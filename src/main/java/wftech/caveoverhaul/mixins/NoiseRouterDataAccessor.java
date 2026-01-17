package wftech.caveoverhaul.mixins;

import net.minecraft.core.HolderGetter;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.NoiseRouterData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(NoiseRouterData.class)
public interface NoiseRouterDataAccessor {

    @Invoker("getFunction")
    static DensityFunction getFunction(HolderGetter<DensityFunction> holderGetter, ResourceKey<DensityFunction> resourceKey) {
        throw new AssertionError();
    }

    @Invoker("noiseGradientDensity")
    static DensityFunction noiseGradientDensity(DensityFunction densityFunction, DensityFunction densityFunction2) {
        throw new AssertionError();
    }

    //noinspection UnusedReturnValue
    @SuppressWarnings("UnusedReturnValue")
    @Invoker("postProcess")
    static DensityFunction postProcess(DensityFunction function) {
        throw new AssertionError();
    }

    //noinspection UnusedReturnValue
    @SuppressWarnings("UnusedReturnValue")
    @Invoker("slideOverworld")
    static DensityFunction slideOverworld(boolean bl, DensityFunction densityFunction) {
        throw new AssertionError();
    }

    @Accessor("FACTOR")
    static ResourceKey<DensityFunction> FACTOR() {
        throw new AssertionError();
    }

    @Accessor("DEPTH")
    static ResourceKey<DensityFunction> DEPTH() {
        throw new AssertionError();
    }

    @Accessor("SLOPED_CHEESE")
    static ResourceKey<DensityFunction> SLOPED_CHEESE() {
        throw new AssertionError();
    }

//    @Accessor("NOODLE")
//    static ResourceKey<DensityFunction> NOODLE() {
//        throw new AssertionError();
//    }
//
//    @Accessor("SPAGHETTI_2D_THICKNESS_MODULATOR")
//    static ResourceKey<DensityFunction> SPAGHETTI_2D_THICKNESS_MODULATOR() {
//        throw new AssertionError();
//    }
//
//    @Accessor("SPAGHETTI_2D")
//    static ResourceKey<DensityFunction> SPAGHETTI_2D() {
//        throw new AssertionError();
//    }

    @Accessor("ZERO")
    static ResourceKey<DensityFunction> ZERO() {
        throw new AssertionError();
    }
}