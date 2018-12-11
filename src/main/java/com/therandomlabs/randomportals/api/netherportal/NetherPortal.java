package com.therandomlabs.randomportals.api.netherportal;

import com.therandomlabs.randomportals.api.config.NetherPortalType;
import com.therandomlabs.randomportals.api.config.NetherPortalTypes;
import com.therandomlabs.randomportals.api.frame.Frame;

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
