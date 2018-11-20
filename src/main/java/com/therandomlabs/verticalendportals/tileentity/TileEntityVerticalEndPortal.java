package com.therandomlabs.verticalendportals.tileentity;

import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntityEndPortal;
import net.minecraft.util.EnumFacing;
import static net.minecraft.block.BlockHorizontal.FACING;

public class TileEntityVerticalEndPortal extends TileEntityEndPortal {
	private EnumFacing facing;

	@Override
	public boolean shouldRenderFace(EnumFacing facing) {
		if(this.facing == null) {
			//https://github.com/TechReborn/TechReborn/issues/1515
			final IBlockState state = world.getBlockState(pos);

			if(!state.getProperties().containsKey(FACING)) {
				return false;
			}

			this.facing = state.getValue(FACING);
		}

		return this.facing == facing || this.facing.getOpposite() == facing;
	}
}
