package com.therandomlabs.verticalendportals.frame;

import java.util.function.Predicate;
import net.minecraft.block.state.BlockWorldState;
import net.minecraft.util.EnumFacing;
import static net.minecraft.block.BlockHorizontal.FACING;
import static net.minecraft.init.Blocks.AIR;

public class BasicVerticalFrameDetector extends FrameDetector {
	private final Predicate<BlockWorldState> predicate;
	private final EnumFacing facing;
	private final RequiredCorner requiredCorner;
	private final Predicate<Frame> framePredicate;

	public BasicVerticalFrameDetector(Predicate<BlockWorldState> predicate,
			RequiredCorner requiredCorner, Predicate<Frame> framePredicate) {
		super(FrameType.VERTICAL);
		this.predicate = predicate;
		facing = null;
		this.requiredCorner = requiredCorner;
		this.framePredicate = framePredicate;
	}

	public BasicVerticalFrameDetector(Predicate<BlockWorldState> predicate, EnumFacing facing,
			RequiredCorner requiredCorner, Predicate<Frame> framePredicate) {
		super(facing.getAxis() == EnumFacing.Axis.X ? FrameType.VERTICAL_Z : FrameType.VERTICAL_X);
		this.predicate = predicate;
		this.facing = facing;
		this.requiredCorner = requiredCorner;
		this.framePredicate = framePredicate;
	}

	@SuppressWarnings("Duplicates")
	@Override
	protected boolean test(FrameType type, BlockWorldState state, FrameSide side, int position) {
		if(position == CORNER) {
			if(requiredCorner == RequiredCorner.ANY) {
				return true;
			}

			if(requiredCorner == RequiredCorner.ANY_NON_AIR) {
				return state.getBlockState().getBlock() != AIR;
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
