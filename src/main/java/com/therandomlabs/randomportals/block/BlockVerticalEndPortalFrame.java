package com.therandomlabs.randomportals.block;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import net.minecraft.block.BlockEndPortalFrame;
import net.minecraft.block.SoundType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BlockVerticalEndPortalFrame extends BlockEndPortalFrame {
	private static final Map<EnumFacing, AxisAlignedBB> AABB_BLOCK =
			new EnumMap<>(EnumFacing.class);
	private static final Map<EnumFacing, AxisAlignedBB> AABB_EYE = new EnumMap<>(EnumFacing.class);

	static {
		AABB_BLOCK.put(EnumFacing.NORTH, new AxisAlignedBB(
				0.0, 0.0, 0.1875, 1.0, 1.0, 1.0
		));

		AABB_BLOCK.put(EnumFacing.SOUTH, new AxisAlignedBB(
				0.0, 0.0, 0.0, 1.0, 1.0, 0.8125
		));

		AABB_BLOCK.put(EnumFacing.WEST, new AxisAlignedBB(
				0.1875, 0.0, 0.0, 1.0, 1.0, 1.0
		));

		AABB_BLOCK.put(EnumFacing.EAST, new AxisAlignedBB(
				0.0, 0.0, 0.0, 0.8125, 1.0, 1.0
		));

		AABB_EYE.put(EnumFacing.NORTH, new AxisAlignedBB(
				0.3125, 0.3125, 0.0, 0.6875, 0.6875, 0.1875
		));

		AABB_EYE.put(EnumFacing.SOUTH, new AxisAlignedBB(
				0.3125, 0.3125, 0.1875, 0.6875, 0.6875, 1.0
		));

		AABB_EYE.put(EnumFacing.WEST, new AxisAlignedBB(
				0.0, 0.3125, 0.3125, 0.1875, 0.6875, 0.6875
		));

		AABB_EYE.put(EnumFacing.EAST, new AxisAlignedBB(
				0.8125, 0.3125, 0.3125, 1.0, 0.6875, 0.6875
		));
	}

	public BlockVerticalEndPortalFrame() {
		setSoundType(SoundType.GLASS);
		setLightLevel(0.125F);
		setHardness(-1.0F);
		setResistance(6000000.0F);
		setCreativeTab(CreativeTabs.DECORATIONS);
		setTranslationKey("endPortalFrameVertical");
		setRegistryName("vertical_end_portal_frame");
	}

	@Override
	public boolean canEntityDestroy(
			IBlockState state, IBlockAccess world, BlockPos pos,
			Entity entity
	) {
		return false;
	}

	@SuppressWarnings("deprecation")
	@Override
	public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess world, BlockPos pos) {
		return AABB_BLOCK.get(state.getValue(FACING));
	}

	@SuppressWarnings("deprecation")
	@Override
	public void addCollisionBoxToList(
			IBlockState state, World world, BlockPos pos,
			AxisAlignedBB entityBox, List<AxisAlignedBB> collidingBoxes, Entity entity,
			boolean isActualState
	) {
		final EnumFacing facing = state.getValue(FACING);

		addCollisionBoxToList(pos, entityBox, collidingBoxes, AABB_BLOCK.get(facing));

		if (state.getValue(EYE)) {
			addCollisionBoxToList(pos, entityBox, collidingBoxes, AABB_EYE.get(facing));
		}
	}
}
