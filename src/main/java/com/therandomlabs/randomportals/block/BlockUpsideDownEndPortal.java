package com.therandomlabs.randomportals.block;

import com.therandomlabs.randomportals.tileentity.TileEntityUpsideDownEndPortal;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BlockUpsideDownEndPortal extends BlockLateralEndPortal{
	private static final AxisAlignedBB AABB_BLOCK = new AxisAlignedBB(
			0.0, 0.25, 0.0, 1.0, 1.0, 1.0
	);

	public BlockUpsideDownEndPortal() {
		super("upside_down_end_portal");
		setTranslationKey("endPortalUpsideDown");
	}

	@Override
	public TileEntity createNewTileEntity(World world, int meta) {
		return new TileEntityUpsideDownEndPortal();
	}

	@SuppressWarnings("deprecation")
	@Override
	public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess world, BlockPos pos) {
		return AABB_BLOCK;
	}

	@SuppressWarnings("deprecation")
	@Override
	public boolean shouldSideBeRendered(IBlockState state, IBlockAccess world, BlockPos pos,
			EnumFacing side) {
		if(side != EnumFacing.UP && side != EnumFacing.DOWN) {
			return false;
		}

		return !world.getBlockState(pos.offset(side)).doesSideBlockRendering(
				world, pos.offset(side), side.getOpposite()
		);
	}

	@Override
	public ItemStack getItem(World world, BlockPos pos, IBlockState state) {
		return new ItemStack(this);
	}

	@Override
	public boolean canEntityDestroy(IBlockState state, IBlockAccess world, BlockPos pos,
			Entity entity) {
		return false;
	}
}
