package com.therandomlabs.verticalendportals.block;

import java.util.List;
import net.minecraft.block.BlockEndPortalFrame;
import net.minecraft.block.SoundType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BlockUpsideDownEndPortalFrame extends BlockEndPortalFrame {
	private static final AxisAlignedBB AABB_BLOCK = new AxisAlignedBB(
			0.0, 0.1875, 0.0, 1.0, 1.0, 1.0
	);

	private static final AxisAlignedBB AABB_EYE = new AxisAlignedBB(
			0.3125, 0, 0.3125, 0.6875, 0.1875, 0.6875
	);

	public BlockUpsideDownEndPortalFrame() {
		setSoundType(SoundType.GLASS);
		setLightLevel(0.125F);
		setHardness(-1.0F);
		setResistance(6000000.0F);
		setCreativeTab(CreativeTabs.DECORATIONS);
		setTranslationKey("endPortalFrameUpsideDown");
		setRegistryName("upside_down_end_portal_frame");
	}

	@Override
	public boolean canEntityDestroy(IBlockState state, IBlockAccess world, BlockPos pos,
			Entity entity) {
		return false;
	}

	@SuppressWarnings("deprecation")
	@Override
	public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess world, BlockPos pos) {
		return AABB_BLOCK;
	}

	@SuppressWarnings("deprecation")
	@Override
	public void addCollisionBoxToList(IBlockState state, World world, BlockPos pos,
			AxisAlignedBB entityBox, List<AxisAlignedBB> collidingBoxes, Entity entity,
			boolean isActualState) {
		addCollisionBoxToList(pos, entityBox, collidingBoxes, AABB_BLOCK);

		if(state.getValue(EYE)) {
			addCollisionBoxToList(pos, entityBox, collidingBoxes, AABB_EYE);
		}
	}
}
