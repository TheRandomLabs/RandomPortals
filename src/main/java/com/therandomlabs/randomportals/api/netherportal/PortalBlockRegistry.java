package com.therandomlabs.randomportals.api.netherportal;

import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import com.google.common.collect.ImmutableSet;
import com.therandomlabs.randomportals.block.BlockNetherPortal;
import net.minecraft.block.Block;
import net.minecraft.block.BlockPortal;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public final class PortalBlockRegistry {
	private static final Set<Block> portalBlocks = new HashSet<>();

	public static ImmutableSet<Block> getBlocks() {
		return ImmutableSet.copyOf(portalBlocks);
	}

	public static void register(Block block) {
		Objects.requireNonNull(block, "block");
		portalBlocks.add(block);
	}

	public static boolean isPortal(Block block) {
		return portalBlocks.contains(block);
	}

	public static boolean isPortal(IBlockState state) {
		return isPortal(state.getBlock());
	}

	public static boolean isPortal(World world, BlockPos pos) {
		return isPortal(world.getBlockState(pos));
	}

	public static IBlockState getState(Block block, EnumFacing.Axis axis, boolean userPlaced) {
		if(block instanceof BlockNetherPortal) {
			return block.getDefaultState().
					withProperty(BlockPortal.AXIS, axis).
					withProperty(BlockNetherPortal.USER_PLACED, userPlaced);
		}

		IBlockState state = block.getDefaultState();
		final Collection<IProperty<?>> properties = state.getPropertyKeys();

		if(properties.contains(BlockPortal.AXIS)) {
			state = state.withProperty(BlockPortal.AXIS, axis);
		}

		if(properties.contains(BlockNetherPortal.USER_PLACED)) {
			state = state.withProperty(BlockNetherPortal.USER_PLACED, userPlaced);
		}

		return state;
	}
}
