package com.therandomlabs.verticalendportals.block;

import com.therandomlabs.verticalendportals.VerticalEndPortals;
import com.therandomlabs.verticalendportals.tileentity.TileEntityBetterEndPortal;
import net.minecraft.block.BlockEndPortal;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BlockBetterEndPortal extends BlockEndPortal {
	public BlockBetterEndPortal() {
		super(Material.PORTAL);
		setLightLevel(1.0F);
		setHardness(-1.0F);
		setResistance(6000000.0F);
		setCreativeTab(CreativeTabs.DECORATIONS);
		setTranslationKey("endPortal");
		setRegistryName("minecraft:end_portal");
	}

	@SuppressWarnings("deprecation")
	@Override
	public boolean shouldSideBeRendered(IBlockState state, IBlockAccess world, BlockPos pos,
			EnumFacing side) {
		return (side == EnumFacing.DOWN || side == EnumFacing.UP) &&
				VerticalEndPortals.shouldSideBeRendered(state, world, pos, side);
	}

	@Override
	public TileEntity createNewTileEntity(World worldIn, int meta) {
		return new TileEntityBetterEndPortal();
	}
}
