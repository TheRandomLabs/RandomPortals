package com.therandomlabs.randomportals.api.config;

import com.therandomlabs.randomportals.api.frame.Frame;
import net.minecraft.item.ItemStack;
import net.minecraft.world.DimensionType;

public final class PortalType {
	public FrameData frame = new FrameData();
	public ActivationData activation = new ActivationData();
	public ColorData color = new ColorData();
	public DestinationData destination = new DestinationData();
	public TeleportationDelay teleportationDelay = new TeleportationDelay();

	public boolean decorative;

	public transient PortalTypeGroup group;
	public transient int dimensionID;

	public PortalType() {}

	@Override
	public String toString() {
		return group + ":" + dimensionID;
	}

	public void ensureCorrect() {
		frame.ensureCorrect();
		activation.ensureCorrect();
		color.ensureCorrect();
		destination.ensureCorrect();
		teleportationDelay.ensureCorrect();
	}

	public boolean testActivator(ItemStack stack) {
		return activation.test(stack);
	}

	public boolean test(Frame frame) {
		return this.frame.test(frame);
	}

	public int getDestinationDimensionID(int sendingDimensionID) {
		final int id = sendingDimensionID == destination.dimensionID ?
				group.defaultDimensionID : destination.dimensionID;

		try {
			DimensionType.getById(id);
			return id;
		} catch(IllegalArgumentException ignored) {}

		return sendingDimensionID == -1 ? 0 : -1;
	}
}
