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
	public boolean forcePortal; //In the End

	public NetherPortalType() {}

	public NetherPortalType(List<FrameBlock> frameBlocks, int dimensionID) {
		this.frameBlocks = frameBlocks;
		this.dimensionID = dimensionID;
	}

	@Override
	public String toString() {
		return "NetherPortalType[frameBlocks=" + frameBlocks + "],dimensionID=" + dimensionID +
				"]";
	}

	public void ensureCorrect() {
		for(int i = 0; i < frameBlocks.size(); i++) {
			final FrameBlock frameBlock = frameBlocks.get(i);

			if(!frameBlock.isValid()) {
				frameBlocks.remove(i--);
				continue;
			}

			frameBlock.ensureCorrect();
		}
	}

	public boolean test(Frame frame) {
		final Map<Block, Integer> detectedBlocks = new HashMap<>();

		for(IBlockState state : frame.getFrameBlocks()) {
			final Block block = state.getBlock();
			boolean found = false;

			for(FrameBlock frameBlock : frameBlocks) {
				if(block == frameBlock.getBlock()) {
					found = true;
					break;
				}
			}

			if(!found) {
				return false;
			}

			detectedBlocks.merge(block, 1, (a, b) -> a + b);
		}

		for(FrameBlock frameBlock : frameBlocks) {
			if(frameBlock.minimumAmount == 0) {
				continue;
			}

			final Integer detectedAmount = detectedBlocks.get(frameBlock.getBlock());

			if(detectedAmount == null || detectedAmount < frameBlock.minimumAmount) {
				return false;
			}
		}

		return true;
	}
}
