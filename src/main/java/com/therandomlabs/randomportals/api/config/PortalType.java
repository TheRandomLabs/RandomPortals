package com.therandomlabs.randomportals.api.config;

import com.therandomlabs.randomportals.api.frame.Frame;
import net.minecraft.item.ItemStack;

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

	@SuppressWarnings("Duplicates")
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
}
