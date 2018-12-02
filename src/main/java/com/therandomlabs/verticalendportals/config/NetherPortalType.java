package com.therandomlabs.verticalendportals.config;

import java.util.List;

public final class NetherPortalType {
	public List<FrameBlock> frameBlocks;
	public int dimensionID;

	public NetherPortalType() {}

	public NetherPortalType(List<FrameBlock> frameBlocks, int dimensionID) {
		this.frameBlocks = frameBlocks;
		this.dimensionID = dimensionID;
	}

	public void ensureCorrect() {
		for(int i = 0; i < frameBlocks.size(); i++) {
			final FrameBlock frameBlock = frameBlocks.get(i);

			if(!frameBlock.isValid()) {
				frameBlocks.remove(i--);
			}

			frameBlock.ensureCorrect();
		}
	}
}
