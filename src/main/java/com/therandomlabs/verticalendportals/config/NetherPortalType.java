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
	public int minWidth = 3;
	public int maxWidth = 9000;
	public int minHeight = 3;
	public int maxHeight = 9000;

	public NetherPortalType() {}

	public NetherPortalType(List<FrameBlock> frameBlocks, int dimensionID) {
		this.frameBlocks = frameBlocks;
		this.dimensionID = dimensionID;
	}

	@Override
	public String toString() {
		return "NetherPortalType[name=" + getName() + ",frameBlocks=" + frameBlocks +
				"],dimensionID=" + dimensionID + "]";
	}

	@SuppressWarnings("Duplicates")
	public void ensureCorrect() {
		for(int i = 0; i < frameBlocks.size(); i++) {
			final FrameBlock frameBlock = frameBlocks.get(i);

			if(!frameBlock.isValid()) {
				frameBlocks.remove(i--);
				continue;
			}

			frameBlock.ensureCorrect();
		}

		if(minWidth < 3) {
			minWidth = 3;
		}

		if(minHeight < 3) {
			minHeight = 3;
		}

		if(maxWidth < minWidth) {
			maxWidth = minWidth;
		}

		if(maxHeight < minHeight) {
			maxHeight = minHeight;
		}
	}

	public String getName() {
		for(Map.Entry<String, NetherPortalType> entry : NetherPortalTypes.getTypes().entrySet()) {
			if(entry.getValue() == this) {
				return entry.getKey();
			}
		}

		return "unknown_type";
	}

	public boolean test(Frame frame) {
		final int width = frame.getWidth();
		final int height = frame.getHeight();

		if(width < minWidth || width > maxWidth || height < minHeight || height > maxHeight) {
			return false;
		}

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
