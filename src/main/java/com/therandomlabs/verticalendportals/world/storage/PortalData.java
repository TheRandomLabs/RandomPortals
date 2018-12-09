package com.therandomlabs.verticalendportals.world.storage;

import com.therandomlabs.verticalendportals.api.config.NetherPortalType;
import com.therandomlabs.verticalendportals.api.frame.Frame;

public final class PortalData {
	private final NetherPortalType type;
	private final Frame frame;
	private final boolean userCreated;

	public PortalData(NetherPortalType type, Frame frame, boolean userCreated) {
		this.type = type;
		this.frame = frame;
		this.userCreated = userCreated;
	}

	@Override
	public String toString() {
		return "PortalData[type=" + type.getName() + ",frame=" + frame + ",userCreated=" +
				userCreated + "]";
	}

	public NetherPortalType getType() {
		return type;
	}

	public Frame getFrame() {
		return frame;
	}

	public boolean isUserCreated() {
		return userCreated;
	}
}
