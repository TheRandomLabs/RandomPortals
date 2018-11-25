package com.therandomlabs.verticalendportals;

import com.therandomlabs.verticalendportals.handler.EndPortalPlaceHandler;
import net.minecraftforge.common.MinecraftForge;

public class CommonProxy {
	public void preInit() {
		VEPConfig.reload();
	}

	public void init() {
		if(VEPConfig.endPortals.enabled) {
			MinecraftForge.EVENT_BUS.register(new EndPortalPlaceHandler());
		}
	}
}
