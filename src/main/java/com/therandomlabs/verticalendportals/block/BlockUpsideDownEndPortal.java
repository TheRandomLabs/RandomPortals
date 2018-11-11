package com.therandomlabs.verticalendportals.block;

import com.therandomlabs.verticalendportals.tileentity.TileEntityUpsideDownEndPortal;
import net.minecraft.block.BlockEndPortal;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BlockUpsideDownEndPortal extends BlockEndPortal {
	private static final AxisAlignedBB AABB_BLOCK = new AxisAlignedBB(
			0.0, 0.25, 0.0, 1.0, 1.0, 1.0
	);

	public BlockUpsideDownEndPortal() {
		super(Material.PORTAL);
		setLightLevel(1.0F);
		setHardness(-1.0F);
		setResistance(6000000.0F);
		setCreativeTab(CreativeTabs.DECORATIONS);
		setTranslationKey("endPortalUpsideDown");
		setRegistryName("upside_down_end_portal");
	}

	@Override
	public TileEntity createNewTileEntity(World world, int meta) {
		return new TileEntityUpsideDownEndPortal();
	}

	@Override
	public boolean canEntityDestroy(IBlockState state, IBlockAccess world, BlockPos pos,
			Entity entity) {
		return false;
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

	@SuppressWarnings("deprecation")
	@Override
	public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess world, BlockPos pos) {
		return AABB_BLOCK;
	}
}
