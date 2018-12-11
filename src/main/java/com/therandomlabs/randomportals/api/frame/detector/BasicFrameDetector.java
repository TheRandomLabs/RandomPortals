package com.therandomlabs.randomportals.api.frame.detector;

import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import com.therandomlabs.randomportals.api.config.FrameSize;
import com.therandomlabs.randomportals.api.frame.Frame;
import com.therandomlabs.randomportals.api.frame.FrameDetector;
import com.therandomlabs.randomportals.api.frame.FrameSide;
import com.therandomlabs.randomportals.api.frame.FrameType;
import com.therandomlabs.randomportals.api.frame.RequiredCorner;
import com.therandomlabs.randomportals.api.util.StatePredicate;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class BasicFrameDetector extends FrameDetector {
	private final Function<FrameType, FrameSize> defaultSize;
	private final Supplier<StatePredicate> blockMatcher;
	private final RequiredCorner requiredCorner;
	private final Predicate<Frame> framePredicate;
	private final StatePredicate innerPredicate;

	public BasicFrameDetector(Block block, RequiredCorner requiredCorner,
			Predicate<Frame> framePredicate, StatePredicate innerPredicate) {
		this(() -> StatePredicate.of(block), requiredCorner, framePredicate, innerPredicate);
	}

	public BasicFrameDetector(Supplier<StatePredicate> blockMatcher, RequiredCorner requiredCorner,
			Predicate<Frame> framePredicate, StatePredicate innerPredicate) {
		this(null, blockMatcher, requiredCorner, framePredicate, innerPredicate);
	}

	public BasicFrameDetector(Function<FrameType, FrameSize> defaultSize, Block block,
			RequiredCorner requiredCorner, Predicate<Frame> framePredicate,
			StatePredicate innerPredicate) {
		this(
				defaultSize, () -> StatePredicate.of(block), requiredCorner, framePredicate,
				innerPredicate
		);
	}

	public BasicFrameDetector(Function<FrameType, FrameSize> defaultSize,
			Supplier<StatePredicate> blockMatcher, RequiredCorner requiredCorner,
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

	@Override
	protected boolean test(World world, FrameType type, BlockPos pos, IBlockState state,
			FrameSide side, int position) {
		if(position == CORNER && requiredCorner != RequiredCorner.SAME) {
			return requiredCorner.test(world, pos, state);
		}

		return blockMatcher.get().test(world, pos, state);
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
