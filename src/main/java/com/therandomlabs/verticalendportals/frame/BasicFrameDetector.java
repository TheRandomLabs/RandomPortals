package com.therandomlabs.verticalendportals.frame;

import java.util.function.Predicate;
import net.minecraft.block.Block;
import net.minecraft.block.state.BlockWorldState;
import static net.minecraft.init.Blocks.AIR;

public class BasicFrameDetector extends FrameDetector {
	private final Block block;
	private final RequiredCorner requiredCorner;
	private final Predicate<Frame> framePredicate;

	public BasicFrameDetector(Block block, RequiredCorner requiredCorner,
			Predicate<Frame> framePredicate) {
		super(FrameType.LATERAL_OR_VERTICAL);
		this.block = block;
		this.requiredCorner = requiredCorner;
		this.framePredicate = framePredicate;
	}

	@SuppressWarnings("Duplicates")
	@Override
	protected boolean test(FrameType type, BlockWorldState state, FrameSide side, int position) {
		final Block block = state.getBlockState().getBlock();

		if(position == CORNER) {
			if(requiredCorner == RequiredCorner.ANY) {
				return true;
			}

			if(requiredCorner == RequiredCorner.ANY_NON_AIR) {
				return state.getBlockState().getBlock() != AIR;
			}
		}

		return block == this.block;
	}

	@Override
	protected boolean test(Frame frame) {
		return framePredicate.test(frame);
	}
}
