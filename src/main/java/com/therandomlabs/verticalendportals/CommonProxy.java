package com.therandomlabs.verticalendportals;

import java.io.IOException;
import com.therandomlabs.randompatches.util.RPUtils;
import com.therandomlabs.verticalendportals.api.config.NetherPortalTypes;
import com.therandomlabs.verticalendportals.handler.EndPortalActivationHandler;
import com.therandomlabs.verticalendportals.handler.NetherPortalFrameBreakHandler;
import com.therandomlabs.verticalendportals.handler.NetherPortalTeleportHandler;
import com.therandomlabs.verticalendportals.world.VEPTeleporter;
import net.minecraftforge.common.MinecraftForge;

public class CommonProxy {
	public void preInit() {
		VEPConfig.reload();
		VEPTeleporter.register();
	}

	public void init() {
		if(VEPConfig.endPortals.enabled) {
			MinecraftForge.EVENT_BUS.register(EndPortalActivationHandler.class);
		}

		if(VEPConfig.netherPortals.enabled) {
			MinecraftForge.EVENT_BUS.register(NetherPortalTeleportHandler.class);
			MinecraftForge.EVENT_BUS.register(NetherPortalFrameBreakHandler.class);
		}

		try {
			NetherPortalTypes.reload();
		} catch(IOException ex) {
			RPUtils.crashReport("Error while reloading Nether portal types", ex);
		}
	}
}
