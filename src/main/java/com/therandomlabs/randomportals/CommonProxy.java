package com.therandomlabs.randomportals;

import java.io.IOException;
import com.therandomlabs.randompatches.util.RPUtils;
import com.therandomlabs.randomportals.api.config.FrameSizes;
import com.therandomlabs.randomportals.api.config.NetherPortalTypes;
import com.therandomlabs.randomportals.frame.NetherPortalFrames;
import com.therandomlabs.randomportals.frame.endportal.EndPortalFrames;
import com.therandomlabs.randomportals.handler.EndPortalActivationHandler;
import com.therandomlabs.randomportals.handler.NetherPortalFrameBreakHandler;
import com.therandomlabs.randomportals.handler.NetherPortalTeleportHandler;
import com.therandomlabs.randomportals.world.RPOTeleporter;
import net.minecraftforge.common.MinecraftForge;

public class CommonProxy {
	public void preInit() {
		RPOConfig.reload();
		RPOTeleporter.register();
	}

	public void init() {
		if(RPOConfig.endPortals.enabled) {
			MinecraftForge.EVENT_BUS.register(EndPortalActivationHandler.class);
		}

		if(RPOConfig.netherPortals.enabled) {
			MinecraftForge.EVENT_BUS.register(NetherPortalTeleportHandler.class);
			MinecraftForge.EVENT_BUS.register(NetherPortalFrameBreakHandler.class);
		}

		EndPortalFrames.registerSizes();
		NetherPortalFrames.registerSizes();
		FrameSizes.reload();

		try {
			NetherPortalTypes.reload();
		} catch(IOException ex) {
			RPUtils.crashReport("Error while reloading Nether portal types", ex);
		}
	}
}
