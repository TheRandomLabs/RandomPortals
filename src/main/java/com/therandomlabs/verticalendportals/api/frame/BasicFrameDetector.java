package com.therandomlabs.verticalendportals.api.frame;

import java.util.Collection;
import java.util.Collections;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import net.minecraft.block.Block;
import net.minecraft.block.state.BlockWorldState;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;

public class BasicFrameDetector extends FrameDetector {
	private final Function<FrameType, FrameSize> defaultSize;
	private final Supplier<Collection<Block>> blocks;
	private final RequiredCorner requiredCorner;
	private final Predicate<Frame> framePredicate;
	private final BiPredicate<World, BlockWorldState> innerPredicate;

	public BasicFrameDetector(Block block, RequiredCorner requiredCorner,
			Predicate<Frame> framePredicate, BiPredicate<World, BlockWorldState> innerPredicate) {
		this(() -> Collections.singleton(block), requiredCorner, framePredicate, innerPredicate);
	}

	public BasicFrameDetector(Supplier<Collection<Block>> blocks, RequiredCorner requiredCorner,
			Predicate<Frame> framePredicate, BiPredicate<World, BlockWorldState> innerPredicate) {
		this(null, blocks, requiredCorner, framePredicate, innerPredicate);
	}

	public BasicFrameDetector(Function<FrameType, FrameSize> defaultSize, Block block,
			RequiredCorner requiredCorner, Predicate<Frame> framePredicate,
			BiPredicate<World, BlockWorldState> innerPredicate) {
		this(
				defaultSize, () -> Collections.singleton(block), requiredCorner, framePredicate,
				innerPredicate
		);
	}

	public BasicFrameDetector(Function<FrameType, FrameSize> defaultSize,
			Supplier<Collection<Block>> blocks, RequiredCorner requiredCorner,
			Predicate<Frame> framePredicate, BiPredicate<World, BlockWorldState> innerPredicate) {
		this.defaultSize = defaultSize;
		this.blocks = blocks;
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
	protected boolean test(World world, FrameType type, BlockWorldState state, FrameSide side,
			int position) {
		final Block block = state.getBlockState().getBlock();

		if(position == CORNER) {
			if(requiredCorner == RequiredCorner.ANY) {
				return true;
			}

			if(requiredCorner == RequiredCorner.ANY_NON_AIR) {
				return state.getBlockState().getBlock() != Blocks.AIR;
			}
		}

		return this.blocks.get().contains(block);
	}

	@Override
	protected boolean test(Frame frame) {
		return framePredicate.test(frame);
	}

	@Override
	protected boolean testInner(World world, BlockWorldState state) {
		return innerPredicate.test(world, state);
	}

	public Collection<Block> getBlocks() {
		return blocks.get();
	}
}
