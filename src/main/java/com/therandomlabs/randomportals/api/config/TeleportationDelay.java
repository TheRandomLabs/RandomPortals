package com.therandomlabs.randomportals.api.config;

import net.minecraft.util.EnumFacing;

public final class TeleportationDelay {
	public int lateral;
	public int verticalX;
	public int verticalZ;

	public void ensureCorrect() {
		if(lateral < 0) {
			lateral = 0;
		}

		if(verticalX < 0) {
			verticalX = 0;
		}

		if(verticalZ < 0) {
			verticalZ = 0;
		}
	}

	public int get(EnumFacing.Axis axis) {
		switch(axis) {
		case X:
			return verticalX;
		case Y:
			return lateral;
		default:
			return verticalZ;
		}
	}
}
