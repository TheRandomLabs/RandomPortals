package com.therandomlabs.verticalendportals.config;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.therandomlabs.verticalendportals.api.frame.Frame;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;

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

	public boolean test(Frame frame) {
		boolean shouldTestAmounts = false;

		for(FrameBlock frameBlock : frameBlocks) {
			if(frameBlock.minimumAmount != 0) {
				shouldTestAmounts = true;
				break;
			}
		}

		if(!shouldTestAmounts) {
			return true;
		}

		final Map<Block, Integer> detectedBlocks = new HashMap<>();

		for(IBlockState state : frame.getFrameBlocks()) {
			detectedBlocks.merge(state.getBlock(), 1, (a, b) -> a + b);
		}

		for(FrameBlock frameBlock : frameBlocks) {
			if(frameBlock.minimumAmount == 0) {
				continue;
			}

			final Integer detectedAmount = detectedBlocks.get(frameBlock.registryName);

			if(detectedAmount == null || detectedAmount < frameBlock.minimumAmount) {
				return false;
			}
		}

		return true;
	}
}
