package wftech.worldgenrevisited.hooks;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.MobSpawnSettings.SpawnerData;
import net.minecraft.world.level.entity.LevelEntityGetter;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.entity.living.MobSpawnEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.event.level.LevelEvent.PotentialSpawns;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.server.ServerLifecycleHooks;
import wftech.worldgenrevisited.Config;
import wftech.worldgenrevisited.WorldgenRevisited;

@Mod.EventBusSubscriber(modid = WorldgenRevisited.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class PotentialSpawnsEvent {
	
	public static Set<EntityType> SEEN_SPAWNS = new HashSet<EntityType>();
	public static Map<ResourceLocation, Integer> MAX_COUNTS_PER_LEVEL = new HashMap<ResourceLocation, Integer>();

	//Was LevelEvent.PotentialSpawns
	@SubscribeEvent
	public static void doSpawnThing(MobSpawnEvent.PositionCheck event) {
		//WorldgenRevisited.LOGGER.error("[WorldgenRevisited::PotentialSpawnsEvent] Dev -> ============================== ");
		
		LevelAccessor access = event.getLevel();
		
		//WorldgenRevisited.LOGGER.error("[WorldgenRevisited] PotentialSpawnsEvent current access " + access.dimensionType());
		for(Level level: ServerLifecycleHooks.getCurrentServer().getAllLevels()) {
			//Level level = (Level) access;
			if(level.dimensionType() != access.dimensionType()) {
				continue;
			}
			LevelEntityGetter<Entity> entityGetter = level.getEntities();
			int count = 0;
			for(Entity entity: entityGetter.getAll()) {
				if(entity instanceof Mob && entity.isAlive() && entity.isAddedToWorld()) {
					count += 1;
				}
			}

			//WorldgenRevisited.LOGGER.error("[WorldgenRevisited] PotentialSpawnsEvent current level " + level.dimensionType() + ", " + count);
			
			boolean found_counts = false;
			for(String entry: Config.MAX_SPAWNS_PER_LEVEL.get()) {
				String[] parts = entry.split("=");
				if(parts[0].equals("*") || parts[0].equals(level.dimension().location().toString())) {
					MAX_COUNTS_PER_LEVEL.put(level.dimension().location(), Integer.parseInt(parts[1]));
					found_counts = true;
					if(count >= Integer.parseInt(parts[1])) {
						//WorldgenRevisited.LOGGER.error("[WorldgenRevisited] Denying!");
						event.setResult(Event.Result.DENY);
						return;
					}
					//WorldgenRevisited.LOGGER.error("[WorldgenRevisited] Not denying");
					
				}
			}
			
			if (!found_counts) {
				MAX_COUNTS_PER_LEVEL.put(level.dimension().location(), -1);
			}
			
		}
		

		
		
		List<SpawnerData> removeData = new ArrayList<SpawnerData>();
		
		
		Holder<EntityType<?>> entityType = ForgeRegistries.ENTITY_TYPES.getHolder(event.getEntity().getType()).get();
		if((event.getSpawnType() == MobSpawnType.CHUNK_GENERATION || event.getSpawnType() == MobSpawnType.NATURAL) 
				&& Config.SPAWNS_TO_DISABLE.get().stream().anyMatch(x -> entityType.unwrapKey().get().toString().contains(x))) {
			event.setResult(Event.Result.DENY);
			
		}
		
		/*
		List<SpawnerData> removeData = new ArrayList<SpawnerData>();
		
		Holder<EntityType<?>> entityType = ForgeRegistries.ENTITY_TYPES.getHolder(event.getEntity().getType()).get();
		
		
		
		for(SpawnerData spawnerData: event.getSpawnerDataList()) {
			//WorldgenRevisited.LOGGER.error("[WorldgenRevisited::PotentialSpawnsEvent] Dev -> " + spawnerData);
			SEEN_SPAWNS.add(spawnerData.type);
			if(Config.SPAWNS_TO_DISABLE.get().stream().anyMatch(x -> spawnerData.toString().contains(x))) {
				removeData.add(spawnerData);
			}
		}
		
		for(SpawnerData data: removeData) {
			event.removeSpawnerData(data);
		}
		*/
	}
}
