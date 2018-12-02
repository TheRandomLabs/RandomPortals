package com.therandomlabs.verticalendportals;

import com.therandomlabs.verticalendportals.config.VEPConfig;
import com.therandomlabs.verticalendportals.handler.EndPortalActivationHandler;
import com.therandomlabs.verticalendportals.util.VEPTeleporter;
import net.minecraftforge.common.MinecraftForge;

public class CommonProxy {
	public void preInit() {
		VEPConfig.reload();
		VEPTeleporter.register();
	}

	public void init() {
		if(VEPConfig.endPortals.enabled) {
			MinecraftForge.EVENT_BUS.register(new EndPortalActivationHandler());
		}
	}
}
