package com.therandomlabs.randomportals.block;

import net.minecraft.block.BlockEndGateway;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class BlockRPOEndGateway extends BlockEndGateway {
	public BlockRPOEndGateway() {
		super(Material.PORTAL);
		setHardness(-1.0F);
		setResistance(6000000.0F);
		setCreativeTab(CreativeTabs.DECORATIONS);
		setTranslationKey("endGateway");
		setRegistryName("minecraft:end_gateway");
	}

	@Override
	public ItemStack getItem(World world, BlockPos pos, IBlockState state) {
		return new ItemStack(this);
	}
}
