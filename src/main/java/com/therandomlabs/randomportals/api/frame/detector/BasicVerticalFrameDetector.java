package com.therandomlabs.randomportals.api.frame.detector;

import java.util.function.Function;
import java.util.function.Predicate;
import com.therandomlabs.randomportals.api.config.FrameSize;
import com.therandomlabs.randomportals.api.frame.Frame;
import com.therandomlabs.randomportals.api.frame.FrameDetector;
import com.therandomlabs.randomportals.api.frame.FrameSide;
import com.therandomlabs.randomportals.api.frame.FrameType;
import com.therandomlabs.randomportals.api.frame.RequiredCorner;
import com.therandomlabs.randomportals.api.util.FrameStatePredicate;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import static net.minecraft.block.BlockHorizontal.FACING;

public class BasicVerticalFrameDetector extends FrameDetector {
	private final FrameType defaultType;
	private final Function<FrameType, FrameSize> defaultSize;
	private final FrameStatePredicate blockMatcher;
	private final RequiredCorner requiredCorner;
	private final EnumFacing facing;
	private final Predicate<Frame> framePredicate;

	public BasicVerticalFrameDetector(
			Function<FrameType, FrameSize> defaultSize,
			FrameStatePredicate blockMatcher, RequiredCorner requiredCorner,
			Predicate<Frame> framePredicate
	) {
		this(defaultSize, blockMatcher, requiredCorner, null, framePredicate);
	}

	public BasicVerticalFrameDetector(
			Function<FrameType, FrameSize> defaultSize,
			FrameStatePredicate blockMatcher, RequiredCorner requiredCorner,
			EnumFacing facing, Predicate<Frame> framePredicate
	) {
		if (facing == null) {
			defaultType = FrameType.VERTICAL;
		} else {
			defaultType = facing.getAxis() == EnumFacing.Axis.X ?
					FrameType.VERTICAL_Z : FrameType.VERTICAL_X;
		}

		this.defaultSize = defaultSize;
		this.blockMatcher = blockMatcher;
		this.requiredCorner = requiredCorner;
		this.facing = facing;
		this.framePredicate = framePredicate;
	}

	@Override
	public FrameType getDefaultType() {
		return defaultType;
	}

	@Override
	public Function<FrameType, FrameSize> getDefaultSize() {
		return defaultSize;
	}

	@Override
	protected boolean test(
			World world, FrameType type, BlockPos pos, IBlockState state,
			FrameSide side, int position
	) {
		if (position == CORNER && requiredCorner != RequiredCorner.SAME) {
			return requiredCorner.test(world, pos, state);
		}

		if (facing == null) {
			return blockMatcher.test(world, pos, state);
		}

		return blockMatcher.test(world, pos, state) && state.getValue(FACING) == facing;
	}

	@Override
	protected boolean test(Frame frame) {
		return framePredicate.test(frame);
	}
}
