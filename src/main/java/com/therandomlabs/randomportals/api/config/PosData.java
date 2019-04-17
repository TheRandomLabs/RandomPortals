package com.therandomlabs.randomportals.api.config;

import com.therandomlabs.randomportals.RandomPortals;
import net.minecraft.util.math.BlockPos;

public final class PosData {
	public int x;
	public int y;
	public int z;

	public void ensureCorrect() {
		if(!RandomPortals.CUBIC_CHUNKS_INSTALLED) {
			if(y < 0) {
				y = 0;
			}

			if(y > 256) {
				y = 256;
			}
		}
	}

	public BlockPos toBlockPos() {
		return new BlockPos(x, y, z);
	}
}
