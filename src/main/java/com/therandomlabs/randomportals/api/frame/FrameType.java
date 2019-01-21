package com.therandomlabs.randomportals.api.frame;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
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
	LATERAL_OR_VERTICAL(null, null, null, null, null, false),
	LATERAL_OR_VERTICAL_X(null, null, null, null, null, false),
	LATERAL_OR_VERTICAL_Z(null, null, null, null, null, false),
	SAME(null, null, null, null, null, false);

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

	public EnumFacing getWidthDirection() {
		return rightDownLeftUp[0];
	}

	public EnumFacing getHeightDirection() {
		return rightDownLeftUp[1];
	}

	public EnumFacing.Axis getAxis() {
		return axis;
	}

	public List<FrameType> getTypes() {
		switch(this) {
		case VERTICAL:
			return Arrays.asList(VERTICAL_X, VERTICAL_Z);
		case LATERAL_OR_VERTICAL:
			return Arrays.asList(LATERAL, VERTICAL_X, VERTICAL_Z);
		case LATERAL_OR_VERTICAL_X:
			return Arrays.asList(LATERAL, VERTICAL_X);
		case LATERAL_OR_VERTICAL_Z:
			return Arrays.asList(LATERAL, VERTICAL_Z);
		default:
			return Collections.singletonList(this);
		}
	}

	public boolean isVertical() {
		return vertical;
	}

	public boolean test(FrameType type) {
		if(this == type || this == LATERAL_OR_VERTICAL) {
			return true;
		}

		if(this == VERTICAL) {
			return type == VERTICAL_X || type == VERTICAL_Z;
		}

		if(this == LATERAL_OR_VERTICAL_X) {
			return type == LATERAL || type == VERTICAL_X;
		}

		if(this == LATERAL_OR_VERTICAL_Z) {
			return type == LATERAL || type == VERTICAL_Z;
		}

		return false;
	}

	public <T> T get(T lateral, T verticalX, T verticalZ) {
		return get(axis, lateral, verticalX, verticalZ);
	}

	public static <T> T get(EnumFacing.Axis axis, T lateral, T verticalX, T verticalZ) {
		switch(axis) {
		case X:
			return verticalX;
		case Y:
			return lateral;
		default:
			return verticalZ;
		}
	}

	public static FrameType fromAxis(EnumFacing.Axis axis) {
		return get(axis, LATERAL, VERTICAL_X, VERTICAL_Z);
	}
}
