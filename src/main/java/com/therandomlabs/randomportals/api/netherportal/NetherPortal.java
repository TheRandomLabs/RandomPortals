package com.therandomlabs.randomportals.api.netherportal;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import com.therandomlabs.randomportals.api.config.PortalType;
import com.therandomlabs.randomportals.api.config.PortalTypes;
import com.therandomlabs.randomportals.api.frame.Frame;
import com.therandomlabs.randomportals.world.storage.RPOSavedData;
import net.minecraft.world.World;

public final class NetherPortal {
	private final Frame frame;
	private final World world;
	private Frame receivingFrame;
	private String typeID;
	private FunctionType functionType;

	public NetherPortal(Frame frame, Frame receivingFrame, PortalType type) {
		this(frame, receivingFrame, type, null);
	}

	public NetherPortal(Frame frame, Frame receivingFrame, PortalType type,
			FunctionType functionType) {
		this.frame = frame;
		world = frame.getWorld();
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

	@Nonnull
	public Frame getFrame() {
		return frame;
	}

	@Nonnull
	public World getWorld() {
		return world;
	}

	@Nullable
	public Frame getReceivingFrame() {
		return receivingFrame;
	}

	public void setReceivingFrame(Frame frame) {
		receivingFrame = frame;
		RPOSavedData.get(world).markDirty();
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
		RPOSavedData.get(world).markDirty();
	}
}
