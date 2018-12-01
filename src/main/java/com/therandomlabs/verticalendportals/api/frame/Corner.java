package com.therandomlabs.verticalendportals.api.frame;

import net.minecraft.util.math.BlockPos;

final class Corner {
	BlockPos pos;
	int sideLength;

	Corner(BlockPos pos, int sideLength) {
		this.pos = pos;
		this.sideLength = sideLength;
	}
}
