package com.therandomlabs.verticalendportals.world.storage;

import com.therandomlabs.verticalendportals.api.config.NetherPortalType;
import com.therandomlabs.verticalendportals.api.config.NetherPortalTypes;
import com.therandomlabs.verticalendportals.api.frame.Frame;

public final class PortalData {
	private final Frame frame;
	private String type;

	public PortalData(Frame frame, NetherPortalType type) {
		this.frame = frame;
		this.type = type.getName();
	}

	@Override
	public String toString() {
		return "PortalData[frame=" + frame + ",type=" + type + "]";
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
