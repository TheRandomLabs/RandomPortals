package com.therandomlabs.randomportals.api.util;

import java.util.function.Predicate;
import net.minecraft.block.Block;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.oredict.OreDictionary;

@FunctionalInterface
public interface StatePredicate {
	boolean test(World world, BlockPos pos, IBlockState state);

	default <T extends Comparable<T>> StatePredicate with(IProperty<T> property, T value) {
		return (world, pos, state) -> test(world, pos, state) &&
				state.getValue(property).equals(value);
	}

	default <T extends Comparable<T>> StatePredicate where(IProperty<T> property,
			Predicate<? super T> is) {
		return (world, pos, state) -> test(world, pos, state) && is.test(state.getValue(property));
	}

	static StatePredicate of(Predicate<IBlockState> predicate) {
		return (world, pos, state) -> predicate.test(state);
	}

	static StatePredicate of(Block block) {
		return (world, pos, state) -> state.getBlock() == block;
	}

	static StatePredicate of(Block block, int meta) {
		return (world, pos, state) -> state.getBlock() == block &&
				(meta == OreDictionary.WILDCARD_VALUE || meta == block.getMetaFromState(state));
	}
}
