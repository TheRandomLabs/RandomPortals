package com.therandomlabs.verticalendportals.api.frame;

import java.util.function.Function;
import java.util.function.Predicate;
import net.minecraft.block.state.BlockWorldState;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import static net.minecraft.block.BlockHorizontal.FACING;

public class BasicVerticalFrameDetector extends FrameDetector {
	private final FrameType defaultType;
	private final Function<FrameType, FrameSize> defaultSize;
	private final Predicate<BlockWorldState> predicate;
	private final EnumFacing facing;
	private final RequiredCorner requiredCorner;
	private final Predicate<Frame> framePredicate;

	public BasicVerticalFrameDetector(Function<FrameType, FrameSize> defaultSize,
			Predicate<BlockWorldState> predicate, RequiredCorner requiredCorner,
			Predicate<Frame> framePredicate) {
		this(defaultSize, predicate, null, requiredCorner, framePredicate);
	}

	public BasicVerticalFrameDetector(Function<FrameType, FrameSize> defaultSize,
			Predicate<BlockWorldState> predicate, EnumFacing facing, RequiredCorner requiredCorner,
			Predicate<Frame> framePredicate) {
		if(facing == null) {
			defaultType = FrameType.VERTICAL;
		} else {
			defaultType = facing.getAxis() == EnumFacing.Axis.X ?
					FrameType.VERTICAL_Z : FrameType.VERTICAL_X;
		}

		this.defaultSize = defaultSize;
		this.predicate = predicate;
		this.facing = facing;
		this.requiredCorner = requiredCorner;
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

	@SuppressWarnings("Duplicates")
	@Override
	protected boolean test(World world, FrameType type, BlockWorldState state, FrameSide side,
			int position) {
		if(position == CORNER) {
			if(requiredCorner == RequiredCorner.ANY) {
				return true;
			}

			if(requiredCorner == RequiredCorner.ANY_NON_AIR) {
				return state.getBlockState().getBlock() != Blocks.AIR;
			}
		}

		if(facing == null) {
			return predicate.test(state);
		}

		return predicate.test(state) && state.getBlockState().getValue(FACING) == facing;
	}

	@Override
	protected boolean test(Frame frame) {
		return framePredicate.test(frame);
	}
}
