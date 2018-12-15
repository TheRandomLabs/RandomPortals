package com.therandomlabs.randomportals.api.util;

import java.util.function.Predicate;
import javax.annotation.Nullable;
import com.therandomlabs.randomportals.api.frame.FrameType;
import net.minecraft.block.Block;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.oredict.OreDictionary;

@FunctionalInterface
public interface FrameStatePredicate {
	boolean test(World world, BlockPos pos, IBlockState state, @Nullable FrameType type);

	default boolean test(World world, BlockPos pos, IBlockState state) {
		return test(world, pos, state, null);
	}

	default <T extends Comparable<T>> FrameStatePredicate with(IProperty<T> property, T value) {
		return (world, pos, state, type) -> test(world, pos, state, type) &&
				state.getValue(property).equals(value);
	}

	default <T extends Comparable<T>> FrameStatePredicate where(IProperty<T> property,
			Predicate<? super T> is) {
		return (world, pos, state, type) ->
				test(world, pos, state, type) && is.test(state.getValue(property));
	}

	static FrameStatePredicate of(Predicate<IBlockState> predicate) {
		return (world, pos, state, type) -> predicate.test(state);
	}

	static FrameStatePredicate of(Block block) {
		return (world, pos, state, type) -> state.getBlock() == block;
	}

	static FrameStatePredicate of(Block block, int meta) {
		return (world, pos, state, type) -> state.getBlock() == block &&
				(meta == OreDictionary.WILDCARD_VALUE || meta == block.getMetaFromState(state));
	}

	static FrameStatePredicate ofBlock(Predicate<Block> predicate) {
		return (world, pos, state, type) -> predicate.test(state.getBlock());
	}
}
