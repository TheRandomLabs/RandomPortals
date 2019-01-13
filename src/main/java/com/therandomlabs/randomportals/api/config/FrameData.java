package com.therandomlabs.randomportals.api.config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.google.common.collect.Lists;
import com.therandomlabs.randomportals.api.frame.Frame;
import com.therandomlabs.randomportals.api.frame.FrameType;
import com.therandomlabs.randomportals.api.frame.RequiredCorner;
import com.therandomlabs.randomportals.util.RegistryNameAndMeta;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public final class FrameData {
	public FrameType type = FrameType.LATERAL_OR_VERTICAL;
	public FrameSizeData size = new FrameSizeData();

	public List<FrameBlock> blocks = Lists.newArrayList(new FrameBlock(Blocks.OBSIDIAN));

	public RequiredCorner requiredCorner = RequiredCorner.ANY_NON_AIR;
	public boolean cornerBlocksContributeToMinimumAmount = true;

	public boolean doGeneratedFramesDrop = true;

	public void ensureCorrect() {
		if(type == FrameType.SAME) {
			type = FrameType.LATERAL_OR_VERTICAL;
		}

		size.ensureCorrect();

		final List<RegistryNameAndMeta> checkedBlocks = new ArrayList<>();

		for(int i = 0; i < blocks.size(); i++) {
			final FrameBlock block = blocks.get(i);
			final RegistryNameAndMeta registryNameAndMeta = new RegistryNameAndMeta(
					block.registryName, block.meta
			);

			if(!block.isValid() || checkedBlocks.contains(registryNameAndMeta)) {
				blocks.remove(i--);
				continue;
			}

			checkedBlocks.add(registryNameAndMeta);
			block.ensureCorrect();
		}
	}

	public boolean test(Frame frame) {
		final FrameType frameType = frame.getType();

		if(!type.test(frameType)) {
			return false;
		}

		final World world = frame.getWorld();

		if(!size.test(frameType, frame.getWidth(), frame.getHeight())) {
			return false;
		}

		final Map<FrameBlock, Integer> detectedBlocks = new HashMap<>();

		for(BlockPos pos : frame.getFrameBlockPositions()) {
			final IBlockState state = world.getBlockState(pos);
			final boolean corner = frame.isCorner(pos);
			boolean found = false;

			if(corner) {
				found = requiredCorner.test(world, pos, state);

				if(found) {
					if(!cornerBlocksContributeToMinimumAmount) {
						continue;
					}
				} else {
					if(requiredCorner != RequiredCorner.SAME) {
						return false;
					}
				}
			}

			for(FrameBlock block : blocks) {
				if(block.test(state)) {
					if(!corner || cornerBlocksContributeToMinimumAmount) {
						detectedBlocks.merge(block, 1, (a, b) -> a + b);
					}

					found = true;
					break;
				}
			}

			if(!found) {
				return false;
			}
		}

		for(FrameBlock block : blocks) {
			if(block.minimumAmount == 0) {
				continue;
			}

			final Integer detectedAmount = detectedBlocks.get(block);

			if(detectedAmount == null || detectedAmount < block.minimumAmount) {
				return false;
			}
		}

		return true;
	}
}
