package wftech.worldgenrevisited.virtualpack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Pair;

import net.minecraft.resources.ResourceLocation;
import wftech.worldgenrevisited.WorldgenRevisited;
import wftech.worldgenrevisited.Config;
import wftech.worldgenrevisited.utils.RegistryUtils;

/*
 * Ah fuck
 */

public class JsonConfigTagFix {
	
	public static List<ResourceLocation> RESOURCES_TO_DELETE = new ArrayList<ResourceLocation>();
	public static List<ResourceLocation> RESOURCES_TO_ADD = new ArrayList<ResourceLocation>();
	public static Set<String> USED_NAMES = new HashSet<String>();

	public static void createAdjustedFeatures() {

		Map<ResourceLocation, Float> locationMultiplierLookupTable = generateResourceMultiplierLookupTable();
		List<Pair<ResourceLocation, JsonElement>> jsonsRaw = new ArrayList<Pair<ResourceLocation, JsonElement>>();
		
		for(ResourceLocation location: locationMultiplierLookupTable.keySet()) {
			//WorldgenRevisited.LOGGER.error("[WorldgenRevisited] AddPackFindersEventWatcher Trying to load JSONs -> " + location);
			JsonElement jsonRepresentingFeaturePlacement = RegistryUtils.RESOURCE_LOCATION_JSON_MAP.get(location);
			if(jsonRepresentingFeaturePlacement != null) {
				jsonsRaw.add(new Pair(location, jsonRepresentingFeaturePlacement));
				//WorldgenRevisited.LOGGER.error("[WorldgenRevisited] AddPackFindersEventWatcher AddPackFindersEventWatcher Found entry for " + location);
			} else {
				WorldgenRevisited.LOGGER.error("Failed to find entry for feature " + location + ". Please double-check the name.");
			}
		}
		
		//VirtualPackResources vpResource = new VirtualPackResources(WorldgenRevisited.MOD_ID, false);

		//WorldgenRevisited.LOGGER.error("[WorldgenRevisited] AddPackFindersEventWatcher Trying to load JSONs 2");
		for(Pair<ResourceLocation, JsonElement> rlocElementPair: jsonsRaw) {
			//WorldgenRevisited.LOGGER.error("[WorldgenRevisited] AddPackFindersEventWatcherTrying to load JSONs ===> " + rlocElementPair);
			JsonElement newCopy = rlocElementPair.getSecond().deepCopy();
			//WorldgenRevisited.LOGGER.error("[WorldgenRevisited] AddPackFindersEventWatcher Copied!");
			try {
				JsonArray placementArray = newCopy.getAsJsonObject().get("placement").getAsJsonArray();
				
				ResourceLocation originalResourceLocation = rlocElementPair.getFirst();
				float multiplier = locationMultiplierLookupTable.get(originalResourceLocation);
				
				RESOURCES_TO_DELETE.add(originalResourceLocation);
				String basePath = "remapped_" + originalResourceLocation.getNamespace() + "_" + originalResourceLocation.getPath();
				String newPath = basePath;
				for(int i = 0; i < 100000; i++) {
					if(USED_NAMES.contains(newPath)) {
						newPath = basePath + i;
					} else {
						break;
					}
				}
				
				String longNewPath = "worldgen/placed_feature/" + newPath + ".json";
				ResourceLocation longNewResourceLocation = new ResourceLocation("worldgenrevisited", longNewPath);
				ResourceLocation newResourceLocation = new ResourceLocation("worldgenrevisited", newPath);
				RESOURCES_TO_ADD.add(newResourceLocation);
				/*
				newCopy.getAsJsonObject().remove("feature");
				newCopy.getAsJsonObject().addProperty("feature", newResourceLocation.toString());
				*/
				//WorldgenRevisited.LOGGER.error("[WorldgenRevisited] AddPackFindersEventWatcher Updated JSON (pre-placement patch) " + newCopy);
				
				//boolean editOccurred = attemptUpdatePlacements(placementArray, multiplier);
				boolean editOccurred = false;
				
				//WorldgenRevisited.LOGGER.error("[WorldgenRevisited] AddPackFindersEventWatcher Sending new JSON -> " + newCopy.toString());
				VirtualPackResources.STREAM_MAP.put(longNewResourceLocation, new MemoryBasedIoSupplier(newCopy.toString(), newResourceLocation.toString()));

				//new MemoryBasedIoSupplier(newCopy.toString(), newResourceLocation.toString());
				//Reader reader = resource.openAsReader()
				
				if(!editOccurred) {

					WorldgenRevisited.LOGGER.error("Failed to find any frequency-related placement options for feature json " + rlocElementPair.getSecond());
					WorldgenRevisited.LOGGER.error("While most features have frequency options, some do not. Likewise, some mods might use "
							+ "their own custom frequency options. Please use /dumpfeature "
							+ "<feature name> (not yet implemented) to produce an editable file under <mod directory>/worldgenrevisited and manually edit the file "
							+ "there. Then, please add this feature to the list of features to remove (optional). WorldgenRevisited will then "
							+ "load your edited feature in place of the original feature. You can add new frequency options to any "
							+ "feature, even if they do not come with placement options by default.");
				
				}
				
				
			} catch (IllegalStateException e){

				WorldgenRevisited.LOGGER.error("IllegalStateException: Unsupported format or malformed datapack feature " 
						+ rlocElementPair.getFirst() + " with json " + rlocElementPair.getSecond());
				WorldgenRevisited.LOGGER.error("If you feel that this error message was caused by an unsupported format, please"
						+ " leave a comment on the WorldgenRevisited modpage so that I can add support in a later update. Use "
						+ "/dumpfeature <featurename> (not yet implemented) to produce a copy of the original for editing purposes. The file will "
						+ "be deposited under <mod directory>/worldgenrevisited. ");
				e.printStackTrace();
				
			}
			
			//vpResource.addResourceLocation(rlocElementPair.getFirst(), newCopy);
		}
	}
	
	public static Map<ResourceLocation, Float> generateResourceMultiplierLookupTable(){
		Map<ResourceLocation, Float> lookupTable = new HashMap<ResourceLocation, Float>();
		
		List<String> requestedChangesRaw = Config.CHANGE_ORE_FREQUENCY_LIST.get();
		
		for(String requestedChangeRaw: requestedChangesRaw) {
			String[] parts = requestedChangeRaw.split("=");
			String unprocessedResourceLocation = parts[0];
			float multiplier = Float.parseFloat(parts[1]);
			lookupTable.put(new ResourceLocation(unprocessedResourceLocation), multiplier);
		}
		
		return lookupTable;
	}
	
	public static void addRequestedResourcesToDeleteToList(){
		List<String> requestedChangesRaw = Config.REMOVE_FEATURES_LIST.get();
		
		for(String requestedChangeRaw: requestedChangesRaw) {
			RESOURCES_TO_DELETE.add(new ResourceLocation(requestedChangeRaw));
		}
	}
	
}
