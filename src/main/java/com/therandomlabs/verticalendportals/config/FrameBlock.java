package com.therandomlabs.verticalendportals.config;

import net.minecraft.block.Block;

public final class FrameBlock {
	public Block registryName;
	public int minimumAmount;

	public FrameBlock() {}

	public FrameBlock(Block block, int minimumAmount) {
		registryName = block;
		this.minimumAmount = minimumAmount;
	}

	public boolean isValid() {
		return registryName != null;
	}

	public void ensureCorrect() {
		if(minimumAmount < 0) {
			minimumAmount = 0;
		}
	}
}
