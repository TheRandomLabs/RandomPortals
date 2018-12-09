package com.therandomlabs.verticalendportals.block;

import com.therandomlabs.verticalendportals.api.event.EndPortalEvent;
import net.minecraft.block.BlockEndPortal;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.DimensionType;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;

public class BlockLateralEndPortal extends BlockEndPortal {
	public BlockLateralEndPortal() {
		super(Material.PORTAL);
		setHardness(-1.0F);
		setResistance(6000000.0F);
		setCreativeTab(CreativeTabs.DECORATIONS);
		setTranslationKey("endPortalLateral");
		setRegistryName("minecraft:end_portal");
	}

	@SuppressWarnings("Duplicates")
	@Override
	public void onEntityCollision(World world, BlockPos pos, IBlockState state, Entity entity) {
		if(world.isRemote || entity.isRiding() || entity.isBeingRidden() || !entity.isNonBoss()) {
			return;
		}

		final AxisAlignedBB aabb = entity.getEntityBoundingBox();

		if(!aabb.intersects(state.getBoundingBox(world, pos).offset(pos))) {
			return;
		}

		if(MinecraftForge.EVENT_BUS.post(new EndPortalEvent.Teleport(null, entity, pos))) {
			return;
		}

		entity.changeDimension(DimensionType.THE_END.getId());
	}

	@Override
	public ItemStack getItem(World world, BlockPos pos, IBlockState state) {
		return new ItemStack(this);
	}
}
