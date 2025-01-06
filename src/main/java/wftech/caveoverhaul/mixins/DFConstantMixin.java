package wftech.caveoverhaul.mixins;

import com.mojang.logging.LogUtils;
import net.minecraft.server.dedicated.DedicatedServer;
import net.minecraft.world.level.levelgen.DensityFunctions;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(DensityFunctions.TwoArgumentSimpleFunction.class)
public interface DFConstantMixin {


    @Redirect(method = "create(Lnet/minecraft/world/level/levelgen/DensityFunctions$TwoArgumentSimpleFunction$Type;Lnet/minecraft/world/level/levelgen/DensityFunction;Lnet/minecraft/world/level/levelgen/DensityFunction;)Lnet/minecraft/world/level/levelgen/DensityFunctions$TwoArgumentSimpleFunction;",
            at = @At(value = "INVOKE",
                    target = "Lorg/slf4j/Logger;warn(Ljava/lang/String;)V"))
    private static void injected(Logger instance, String s) {
        //log spam galore!
        //Patching the density function using DensityFunctions.Constant results in a non-continuous function, which
        //mc apparently checks for. Due to this check, the log is normally filled with very, very, very large errors
        //both in volume and the size of each error (since it dumps the entire fucking density function).
        //Unless the log.warn call is @redirected, of course :)
    }
}
