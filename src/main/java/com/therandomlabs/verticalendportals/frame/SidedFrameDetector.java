package com.therandomlabs.verticalendportals.frame;

import net.minecraft.block.state.BlockWorldState;

public abstract class SidedFrameDetector extends FrameDetector {
	protected SidedFrameDetector(Type type) {
		super(type);
	}

	@Override
	protected final boolean test(Type type, BlockWorldState state, FrameSide side, int position) {
		if(position == CORNER) {
			switch(side) {
			case TOP:
				return testTopLeftCorner(type, state);
			case RIGHT:
				return testTopRightCorner(type, state);
			case BOTTOM:
				return testBottomRightCorner(type, state);
			default:
				return testBottomLeftCorner(type, state);
			}
		}

		switch(side) {
		case TOP:
			return testTop(type, state, position);
		case RIGHT:
			return testRight(type, state, position);
		case BOTTOM:
			return testBottom(type, state, position);
		default:
			return testLeft(type, state, position);
		}
	}

	protected boolean testTopRightCorner(Type type, BlockWorldState state) {
		return true;
	}

	protected boolean testTopLeftCorner(Type type, BlockWorldState state) {
		return true;
	}

	protected boolean testBottomRightCorner(Type type, BlockWorldState state) {
		return true;
	}

	protected boolean testBottomLeftCorner(Type type, BlockWorldState state) {
		return true;
	}

	protected abstract boolean testTop(Type type, BlockWorldState state, int position);

	protected abstract boolean testRight(Type type, BlockWorldState state, int position);

	protected abstract boolean testBottom(Type type, BlockWorldState state, int position);

	protected abstract boolean testLeft(Type type, BlockWorldState state, int position);
}
