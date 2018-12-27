package com.therandomlabs.randomportals.api.netherportal;

import javax.annotation.Nullable;
import com.therandomlabs.randomportals.api.config.PortalType;
import com.therandomlabs.randomportals.api.config.PortalTypes;
import com.therandomlabs.randomportals.api.frame.Frame;
import com.therandomlabs.randomportals.world.storage.RPOSavedData;

public final class NetherPortal {
	private final Frame frame;
	private Frame receivingFrame;
	private String type;

	public NetherPortal(Frame frame, Frame receivingFrame, PortalType type) {
		this.frame = frame;
		this.receivingFrame = receivingFrame;
		this.type = type.getName();
	}

	@Override
	public String toString() {
		return "NetherPortal[frame=" + frame + ",receivingFrame=" + receivingFrame +
				",type=" + type + "]";
	}

	@Nullable
	public Frame getFrame() {
		return frame;
	}

	@Nullable
	public Frame getReceivingFrame() {
		return receivingFrame;
	}

	public void setReceivingFrame(Frame frame) {
		receivingFrame = frame;

		if(this.frame != null) {
			RPOSavedData.get(this.frame.getWorld()).markDirty();
		}
	}

	public PortalType getType() {
		final PortalType type = PortalTypes.get(this.type);
		this.type = type.getName();
		return type;
	}
}
