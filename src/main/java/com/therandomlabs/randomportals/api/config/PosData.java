package com.therandomlabs.randomportals.api.config;

import com.therandomlabs.randomportals.RandomPortals;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public final class PosData {
	public int x;
	public int y;
	public int z;
	public boolean useTopSolidY;

	public void ensureCorrect() {
		if (!RandomPortals.CUBIC_CHUNKS_INSTALLED) {
			if (y < 0) {
				y = 0;
			}

			if (y > 256) {
				y = 256;
			}
		}
	}

	public BlockPos toBlockPos(World world) {
		if (useTopSolidY) {
			return world.getTopSolidOrLiquidBlock(new BlockPos(x, 0, z));
		}

		return new BlockPos(x, y, z);
	}
}
