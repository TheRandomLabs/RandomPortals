package com.therandomlabs.verticalendportals.block;

import net.minecraft.block.BlockEndPortal;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class BlockLateralEndPortal extends BlockEndPortal {
	public BlockLateralEndPortal() {
		super(Material.PORTAL);
		setHardness(-1.0F);
		setResistance(6000000.0F);
		setCreativeTab(CreativeTabs.DECORATIONS);
		setTranslationKey("endPortalLateral");
		setRegistryName("minecraft:end_portal");
	}

	@Override
	public ItemStack getItem(World world, BlockPos pos, IBlockState state) {
		return new ItemStack(this);
	}
}
