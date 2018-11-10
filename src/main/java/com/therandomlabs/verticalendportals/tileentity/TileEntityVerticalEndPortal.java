package com.therandomlabs.verticalendportals.tileentity;

import com.therandomlabs.verticalendportals.block.BlockVerticalEndPortal;
import net.minecraft.tileentity.TileEntityEndPortal;
import net.minecraft.util.EnumFacing;

public class TileEntityVerticalEndPortal extends TileEntityEndPortal {
	private EnumFacing facing;

	@Override
	public boolean shouldRenderFace(EnumFacing facing) {
		if(this.facing == null) {
			this.facing = world.getBlockState(pos).getValue(BlockVerticalEndPortal.FACING);
		}

		return this.facing == facing || this.facing.getOpposite() == facing;
	}
}
