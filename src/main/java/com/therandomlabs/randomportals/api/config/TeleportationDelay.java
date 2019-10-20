package com.therandomlabs.randomportals.api.config;

import com.therandomlabs.randomportals.api.frame.FrameType;
import net.minecraft.entity.Entity;
import net.minecraft.util.EnumFacing;

public final class TeleportationDelay {
	public int lateral;
	public int verticalX;
	public int verticalZ;

	public void ensureCorrect() {
		if (lateral < 0) {
			lateral = 0;
		}

		if (verticalX < 0) {
			verticalX = 0;
		}

		if (verticalZ < 0) {
			verticalZ = 0;
		}
	}

	public int get(EnumFacing.Axis axis) {
		return FrameType.get(axis, lateral, verticalX, verticalZ);
	}

	public int getMaxInPortalTime(EnumFacing.Axis axis, Entity entity) {
		final int time = get(axis);
		return time == 0 ? entity.getMaxInPortalTime() : time;
	}
}
