package com.therandomlabs.randomportals.api.netherportal;

import javax.annotation.Nullable;
import com.therandomlabs.randomportals.api.config.NetherPortalType;
import com.therandomlabs.randomportals.api.config.NetherPortalTypes;
import com.therandomlabs.randomportals.api.frame.Frame;
import com.therandomlabs.randomportals.world.storage.RPOSavedData;
import net.minecraft.world.World;

public final class NetherPortal {
	private final Frame frame;
	private Frame receivingFrame;
	private String type;

	public NetherPortal(Frame frame, Frame receivingFrame, NetherPortalType type) {
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
	public Frame getFrame(World world) {
		return getFrame(frame, world);
	}

	@Nullable
	public Frame getReceivingFrame() {
		return receivingFrame;
	}

	public void setReceivingFrame(Frame frame) {
		receivingFrame = frame;

		if(this.frame != null) {
			final World world = this.frame.getWorld();

			if(world != null) {
				RPOSavedData.get(world).markDirty();
			}
		}
	}

	@Nullable
	public Frame getReceivingFrame(World world) {
		return getFrame(receivingFrame, world);
	}

	public NetherPortalType getType() {
		final NetherPortalType type = NetherPortalTypes.get(this.type);
		this.type = type.getName();
		return type;
	}

	private Frame getFrame(Frame frame, World world) {
		if(frame == null) {
			return null;
		}

		if(world != null) {
			frame.setWorld(world);
		}

		return frame;
	}
}
