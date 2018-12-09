package com.therandomlabs.verticalendportals.world.storage;

import com.therandomlabs.verticalendportals.api.config.NetherPortalType;
import com.therandomlabs.verticalendportals.api.config.NetherPortalTypes;
import com.therandomlabs.verticalendportals.api.frame.Frame;

public final class PortalData {
	private String type;
	private final Frame frame;

	public PortalData(NetherPortalType type, Frame frame) {
		this.type = type.getName();
		this.frame = frame;
	}

	@Override
	public String toString() {
		return "PortalData[type=" + type + ",frame=" + frame + ",userCreated=]";
	}

	public NetherPortalType getType() {
		final NetherPortalType type = NetherPortalTypes.get(this.type);
		this.type = type.getName();
		return type;
	}

	public Frame getFrame() {
		return frame;
	}
}
