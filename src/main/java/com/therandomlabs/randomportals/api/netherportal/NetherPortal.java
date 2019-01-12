package com.therandomlabs.randomportals.api.netherportal;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import com.therandomlabs.randomportals.api.config.PortalType;
import com.therandomlabs.randomportals.api.config.PortalTypes;
import com.therandomlabs.randomportals.api.frame.Frame;
import com.therandomlabs.randomportals.world.storage.RPOSavedData;

public final class NetherPortal {
	private final Frame frame;
	private Frame receivingFrame;
	private String typeID;
	private FunctionType functionType;

	public NetherPortal(Frame frame, Frame receivingFrame, PortalType type) {
		this(frame, receivingFrame, type, null);
	}

	public NetherPortal(Frame frame, Frame receivingFrame, PortalType type,
			FunctionType functionType) {
		this.frame = frame;
		this.receivingFrame = receivingFrame;
		typeID = type.toString();

		if(functionType == null) {
			this.functionType = type.decorative ? FunctionType.DECORATIVE : FunctionType.NORMAL;
		} else {
			this.functionType = functionType;
		}
	}

	@Override
	public String toString() {
		return "NetherPortal[frame=" + frame + ",receivingFrame=" + receivingFrame +
				",typeID=" + typeID + ",functionType=" + functionType + "]";
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

	@Nonnull
	public PortalType getType() {
		final PortalType type = PortalTypes.getSpecific(typeID);
		typeID = type.toString();
		return type;
	}

	@Nonnull
	public FunctionType getFunctionType() {
		return functionType;
	}

	public void setFunctionType(FunctionType type) {
		functionType = type;

		if(frame != null) {
			RPOSavedData.get(frame.getWorld()).markDirty();
		}
	}
}
