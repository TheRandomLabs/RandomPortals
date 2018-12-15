package com.therandomlabs.randomportals.api.frame;

import com.therandomlabs.randomportals.api.util.FrameStatePredicate;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public enum RequiredCorner {
	ANY((world, pos, state, type) -> true),
	ANY_NON_AIR((world, pos, state, type) -> state.getBlock() != Blocks.AIR),
	SAME(null);

	private final FrameStatePredicate predicate;

	RequiredCorner(FrameStatePredicate predicate) {
		this.predicate = predicate;
	}

	public FrameStatePredicate getPredicate() {
		return predicate;
	}

	public boolean test(World world, BlockPos pos, IBlockState state) {
		return predicate != null && predicate.test(world, pos, state);
	}
}
