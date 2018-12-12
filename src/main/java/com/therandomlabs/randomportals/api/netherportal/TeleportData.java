package com.therandomlabs.randomportals.api.netherportal;

import com.therandomlabs.randomportals.api.config.NetherPortalType;
import com.therandomlabs.randomportals.api.config.NetherPortalTypes;
import com.therandomlabs.randomportals.api.frame.Frame;
import net.minecraft.util.math.BlockPos;

public class TeleportData {
	private NetherPortal portal;
	private BlockPos portalPos;

	public TeleportData(NetherPortal portal, BlockPos portalPos) {
		this.portal = portal;
		this.portalPos = portalPos;
	}

	public NetherPortal getPortal() {
		return portal;
	}

	public Frame getFrame() {
		return portal == null ? null : portal.getFrame();
	}

	public NetherPortalType getPortalType() {
		return portal == null ? NetherPortalTypes.getDefault() : portal.getType();
	}

	public BlockPos getPortalPos() {
		return portalPos;
	}
}
