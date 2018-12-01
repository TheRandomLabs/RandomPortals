package com.therandomlabs.verticalendportals.api.frame;

import net.minecraft.util.EnumFacing;

public enum FrameType {
	LATERAL(EnumFacing.Axis.Y, false),
	VERTICAL(null, true),
	VERTICAL_X(EnumFacing.Axis.X, true),
	VERTICAL_Z(EnumFacing.Axis.Z, true),
	LATERAL_OR_VERTICAL(null, false);

	private final EnumFacing.Axis axis;
	private final boolean vertical;

	FrameType(EnumFacing.Axis axis, boolean vertical) {
		this.axis = axis;
		this.vertical = vertical;
	}

	public EnumFacing.Axis getAxis() {
		return axis;
	}

	public boolean isVertical() {
		return vertical;
	}

	public static FrameType fromAxis(EnumFacing.Axis axis) {
		for(FrameType type : values()) {
			if(type.axis == axis) {
				return type;
			}
		}

		return null;
	}
}
