package com.therandomlabs.verticalendportals.tileentity;

import net.minecraft.tileentity.TileEntityEndPortal;
import net.minecraft.util.EnumFacing;

public class TileEntityUpsideDownEndPortal extends TileEntityEndPortal {
	@Override
	public boolean shouldRenderFace(EnumFacing facing) {
		return facing == EnumFacing.UP || facing == EnumFacing.DOWN;
	}
}
