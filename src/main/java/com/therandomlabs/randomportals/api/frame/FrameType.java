package com.therandomlabs.randomportals.api.frame;

import net.minecraft.util.EnumFacing;

public enum FrameType {
	LATERAL(
			EnumFacing.Axis.Y,
			EnumFacing.EAST,
			EnumFacing.SOUTH,
			EnumFacing.WEST,
			EnumFacing.NORTH,
			false
	),
	VERTICAL(null, null, null, null, null, true),
	VERTICAL_X(
			EnumFacing.Axis.X,
			EnumFacing.EAST,
			EnumFacing.DOWN,
			EnumFacing.WEST,
			EnumFacing.UP,
			true
	),
	VERTICAL_Z(
			EnumFacing.Axis.Z,
			EnumFacing.NORTH,
			EnumFacing.DOWN,
			EnumFacing.SOUTH,
			EnumFacing.UP,
			true
	),
	LATERAL_OR_VERTICAL(null, null, null, null, null, false);

	final EnumFacing[] rightDownLeftUp;

	private final EnumFacing.Axis axis;
	private final boolean vertical;

	FrameType(EnumFacing.Axis axis, EnumFacing right, EnumFacing down, EnumFacing left,
			EnumFacing up, boolean vertical) {
		this.axis = axis;
		rightDownLeftUp = new EnumFacing[] {
				right, down, left, up
		};
		this.vertical = vertical;
	}

	public EnumFacing.Axis getAxis() {
		return axis;
	}

	public boolean isVertical() {
		return vertical;
	}

	public boolean test(FrameType type) {
		if(this == type || this == LATERAL_OR_VERTICAL) {
			return true;
		}

		return type == VERTICAL && (type == VERTICAL_X || type == VERTICAL_Z);
	}

	public <T> T get(T lateral, T verticalX, T verticalZ) {
		if(this == LATERAL) {
			return lateral;
		}

		if(this == VERTICAL_X) {
			return verticalX;
		}

		return verticalZ;
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
