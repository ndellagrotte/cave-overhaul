package wftech.cavesrevisited.carvertypes;

import net.minecraft.world.level.levelgen.carver.CanyonCarverConfiguration;
import net.minecraft.world.level.levelgen.carver.CanyonWorldCarver;
import net.minecraft.world.level.levelgen.carver.CaveCarverConfiguration;
import net.minecraft.world.level.levelgen.carver.CaveWorldCarver;
import net.minecraft.world.level.levelgen.carver.WorldCarver;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import wftech.cavesrevisited.CavesRevisited;

public class InitCarverTypes {

	public static void registerDeferred(IEventBus eventBus) {
		WORLD_CARVERS.register(eventBus);
	}

	public static final DeferredRegister<WorldCarver<?>> WORLD_CARVERS =
            DeferredRegister.create(ForgeRegistries.WORLD_CARVERS, CavesRevisited.MOD_ID);

	public final static RegistryObject<? extends CaveWorldCarver> MYCELIUM_CAVE = 
			WORLD_CARVERS.register("mycelium_cave", () -> new OldWorldCarverv12(CaveCarverConfiguration.CODEC));

	/*
	public final static RegistryObject<? extends CaveWorldCarver> MYCELIUM_CAVE = 
			WORLD_CARVERS.register("mycelium_cave", () -> new OldWorldCarverv12(CaveCarverConfiguration.CODEC));
	*/

	public final static RegistryObject<? extends CaveWorldCarver> VANILLA_CAVE = 
			WORLD_CARVERS.register("vanilla_cave", () -> new VanillaCave(CaveCarverConfiguration.CODEC));

	public final static RegistryObject<? extends CanyonWorldCarver> VANILLA_CANYON = 
			WORLD_CARVERS.register("vanilla_canyon", () -> new VanillaCanyon(CanyonCarverConfiguration.CODEC));

}
