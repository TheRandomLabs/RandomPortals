package com.therandomlabs.verticalendportals.config;

import com.therandomlabs.verticalendportals.util.VEPUtils;
import net.minecraft.block.Block;

public final class FrameBlock {
	public String registryName;
	public int minimumAmount;

	private transient Block block;

	public FrameBlock() {}

	public FrameBlock(Block block, int minimumAmount) {
		registryName = block.getRegistryName().toString();
		this.minimumAmount = minimumAmount;
	}

	@Override
	public String toString() {
		return "FrameBlock[registryName=" + registryName + ",minimumAmount=" + minimumAmount + "]";
	}

	public Block getBlock() {
		if(block == null) {
			block = VEPUtils.getBlock(registryName);
		}

		return block;
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
