package wftech.caveoverhaul.mixins;

import net.minecraft.world.level.levelgen.NoiseChunk;
import net.minecraft.world.level.levelgen.carver.CarvingContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(CarvingContext.class)
public interface CarvingContextAccessor {

    @Accessor("noiseChunk")
    NoiseChunk caveOverhaul$getNoiseChunk();
}
