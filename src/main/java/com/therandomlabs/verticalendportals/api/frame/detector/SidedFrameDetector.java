package com.therandomlabs.verticalendportals.api.frame.detector;

import com.therandomlabs.verticalendportals.api.frame.FrameDetector;
import com.therandomlabs.verticalendportals.api.frame.FrameSide;
import com.therandomlabs.verticalendportals.api.frame.FrameType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public abstract class SidedFrameDetector extends FrameDetector {
	private final FrameType defaultType;

	protected SidedFrameDetector(FrameType defaultType) {
		this.defaultType = defaultType;
	}

	@Override
	public FrameType getDefaultType() {
		return defaultType;
	}

	@Override
	protected final boolean test(World world, FrameType type, BlockPos pos, IBlockState state,
			FrameSide side, int position) {
		if(position == CORNER) {
			switch(side) {
			case TOP:
				return testTopLeftCorner(type, pos, state);
			case RIGHT:
				return testTopRightCorner(type, pos, state);
			case BOTTOM:
				return testBottomRightCorner(type, pos, state);
			default:
				return testBottomLeftCorner(type, pos, state);
			}
		}

		switch(side) {
		case TOP:
			return testTop(type, pos, state, position);
		case RIGHT:
			return testRight(type, pos, state, position);
		case BOTTOM:
			return testBottom(type, pos, state, position);
		default:
			return testLeft(type, pos, state, position);
		}
	}

	protected boolean testTopRightCorner(FrameType type, BlockPos pos, IBlockState state) {
		return true;
	}

	protected boolean testTopLeftCorner(FrameType type, BlockPos pos, IBlockState state) {
		return true;
	}

	protected boolean testBottomRightCorner(FrameType type, BlockPos pos, IBlockState state) {
		return true;
	}

	protected boolean testBottomLeftCorner(FrameType type, BlockPos pos, IBlockState state) {
		return true;
	}

	protected abstract boolean testTop(FrameType type, BlockPos pos, IBlockState state,
			int position);

	protected abstract boolean testRight(FrameType type, BlockPos pos, IBlockState state,
			int position);

	protected abstract boolean testBottom(FrameType type, BlockPos pos, IBlockState state,
			int position);

	protected abstract boolean testLeft(FrameType type, BlockPos pos, IBlockState state,
			int position);
}
