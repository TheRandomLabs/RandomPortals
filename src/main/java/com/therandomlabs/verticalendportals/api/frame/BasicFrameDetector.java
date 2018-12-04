package com.therandomlabs.verticalendportals.api.frame;

import java.util.function.Function;
import java.util.function.Predicate;
import com.therandomlabs.verticalendportals.api.util.StatePredicate;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class BasicFrameDetector extends FrameDetector {
	private final Function<FrameType, FrameSize> defaultSize;
	private final StatePredicate blockMatcher;
	private final StatePredicate requiredCorner;
	private final Predicate<Frame> framePredicate;
	private final StatePredicate innerPredicate;

	public BasicFrameDetector(Block block, StatePredicate requiredCorner,
			Predicate<Frame> framePredicate, StatePredicate innerPredicate) {
		this(StatePredicate.of(block), requiredCorner, framePredicate, innerPredicate);
	}

	public BasicFrameDetector(StatePredicate blockMatcher, StatePredicate requiredCorner,
			Predicate<Frame> framePredicate, StatePredicate innerPredicate) {
		this(null, blockMatcher, requiredCorner, framePredicate, innerPredicate);
	}

	public BasicFrameDetector(Function<FrameType, FrameSize> defaultSize, Block block,
			StatePredicate requiredCorner, Predicate<Frame> framePredicate,
			StatePredicate innerPredicate) {
		this(
				defaultSize, StatePredicate.of(block), requiredCorner, framePredicate,
				innerPredicate
		);
	}

	public BasicFrameDetector(Function<FrameType, FrameSize> defaultSize,
			StatePredicate blockMatcher, StatePredicate requiredCorner,
			Predicate<Frame> framePredicate, StatePredicate innerPredicate) {
		this.defaultSize = defaultSize;
		this.blockMatcher = blockMatcher;
		this.requiredCorner = requiredCorner;
		this.framePredicate = framePredicate;
		this.innerPredicate = innerPredicate;
	}

	@Override
	public FrameType getDefaultType() {
		return FrameType.LATERAL_OR_VERTICAL;
	}

	@Override
	public Function<FrameType, FrameSize> getDefaultSize() {
		return defaultSize;
	}

	@SuppressWarnings("Duplicates")
	@Override
	protected boolean test(World world, FrameType type, BlockPos pos, IBlockState state,
			FrameSide side, int position) {
		if(position == CORNER && requiredCorner != RequiredCorner.SAME) {
			return requiredCorner.test(world, pos, state);
		}

		return blockMatcher.test(world, pos, state);
	}

	@Override
	protected boolean test(Frame frame) {
		return framePredicate.test(frame);
	}

	@Override
	protected boolean testInner(World world, BlockPos pos, IBlockState state) {
		return innerPredicate.test(world, pos, state);
	}
}
