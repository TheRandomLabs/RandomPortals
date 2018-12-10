package com.therandomlabs.verticalendportals.api.netherportal;

import com.therandomlabs.verticalendportals.api.config.NetherPortalType;
import com.therandomlabs.verticalendportals.api.config.NetherPortalTypes;
import com.therandomlabs.verticalendportals.api.frame.Frame;

public final class NetherPortal {
	private final Frame frame;
	private String type;

	public NetherPortal(Frame frame, NetherPortalType type) {
		this.frame = frame;
		this.type = type.getName();
	}

	@Override
	public String toString() {
		return "NetherPortal[frame=" + frame + ",type=" + type + "]";
	}

	public Frame getFrame() {
		return frame;
	}

	public NetherPortalType getType() {
		final NetherPortalType type = NetherPortalTypes.get(this.type);
		this.type = type.getName();
		return type;
	}
}
