package com.therandomlabs.randomportals.api.config;

import com.therandomlabs.randomportals.api.frame.FrameType;
import net.minecraft.util.EnumFacing;

public final class FrameSizeData {
	public FrameSize lateral = new FrameSize();
	public FrameSize verticalX = new FrameSize();
	public FrameSize verticalZ = new FrameSize();

	public void ensureCorrect() {
		lateral.ensureCorrect();
		verticalX.ensureCorrect();
		verticalZ.ensureCorrect();
	}

	public FrameSize get(EnumFacing.Axis axis) {
		return FrameType.get(axis, lateral, verticalX, verticalZ);
	}

	public FrameSize get(FrameType type) {
		return type.get(lateral, verticalX, verticalZ);
	}

	public boolean test(FrameType type, int width, int height) {
		return get(type).test(width, height);
	}
}
