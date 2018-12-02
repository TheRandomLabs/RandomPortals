package com.therandomlabs.verticalendportals.config;

import com.therandomlabs.verticalendportals.util.VEPUtils;

public final class FrameBlock {
	public String registryName;
	public int minimumAmount;

	public FrameBlock() {}

	public FrameBlock(String registryName, int minimumAmount) {
		this.registryName = registryName;
		this.minimumAmount = minimumAmount;
	}

	public boolean isValid() {
		return VEPUtils.getBlock(registryName, null) != null;
	}

	public void ensureCorrect() {
		if(minimumAmount < 0) {
			minimumAmount = 0;
		}
	}
}
