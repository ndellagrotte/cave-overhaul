package wftech.caveoverhaul.mixins;

import net.minecraft.world.level.levelgen.OreVeinifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(OreVeinifier.VeinType.class)
public interface VeinTypeAccessor {

    //final int minY;, maxY
    @Accessor("minY")
    int minY();

}
