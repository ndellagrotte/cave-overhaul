package wftech.caveoverhaul;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import wftech.caveoverhaul.carvertypes.InitCarverTypesFabric;
import wftech.caveoverhaul.carvertypes.NCLayerHolder;
import wftech.caveoverhaul.carvertypes.NoisetypeDomainWarp;
import wftech.caveoverhaul.carvertypes.rivers.NURLayerHolder;
import wftech.caveoverhaul.utils.Globals;

public class CaveOverhaul implements ModInitializer {

	public static final String MOD_ID = "caveoverhaul";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	@Override
	public void onInitialize() {
		Config.initConfig();
		InitCarverTypesFabric.init();

		ServerLifecycleEvents.SERVER_STOPPED.register(server -> {
			NURLayerHolder.reset();
			NoisetypeDomainWarp.reset();
			NCLayerHolder.reset();
			Globals.reset();
		});
	}
}