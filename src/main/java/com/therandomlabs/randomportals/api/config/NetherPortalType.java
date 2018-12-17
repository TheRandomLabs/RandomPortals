package com.therandomlabs.randomportals.api.config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import com.therandomlabs.randomportals.api.frame.Frame;
import com.therandomlabs.randomportals.api.frame.FrameType;
import com.therandomlabs.randomportals.api.frame.RequiredCorner;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public final class NetherPortalType {
	public enum ConsumeBehavior {
		CONSUME,
		DAMAGE,
		DO_NOTHING
	}

	public List<FrameBlock> frameBlocks;
	public RequiredCorner requiredCorner = RequiredCorner.ANY_NON_AIR;
	public boolean cornerBlocksContributeToMinimumAmount = true;

	public FrameType type = FrameType.LATERAL_OR_VERTICAL;
	public boolean doGeneratedFramesDrop = true;

	public List<FrameActivator> activators = new ArrayList<>();
	public ConsumeBehavior activatorConsumeBehavior = ConsumeBehavior.CONSUME;
	public boolean canBeActivatedByFire;

	public EnumDyeColor[] colors = {
			EnumDyeColor.PURPLE
	};
	public boolean forceColor;

	public boolean whitelist;
	public List<Integer> dimensions = new ArrayList<>();

	public int dimensionID;
	public boolean spawnPortal = true;
	public boolean teleportToPortal = true;
	public FrameSizeData size = new FrameSizeData();
	public TeleportationDelay teleportationDelay = new TeleportationDelay();

	transient String name;

	public NetherPortalType() {}

	public NetherPortalType(List<FrameBlock> frameBlocks, int dimensionID) {
		this(null, frameBlocks, dimensionID);
	}

	public NetherPortalType(String name, List<FrameBlock> frameBlocks, int dimensionID) {
		this.name = name;
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
		final List<String> registryNames = new ArrayList<>();

		for(int i = 0; i < frameBlocks.size(); i++) {
			final FrameBlock frameBlock = frameBlocks.get(i);

			if(!frameBlock.isValid() || registryNames.contains(frameBlock.registryName)) {
				frameBlocks.remove(i--);
				continue;
			}

			registryNames.add(frameBlock.registryName);
			frameBlock.ensureCorrect();
		}

		registryNames.clear();

		for(int i = 0; i < activators.size(); i++) {
			final FrameActivator activator = activators.get(i);

			if(!activator.isValid() || registryNames.contains(activator.registryName)) {
				activators.remove(i--);
				continue;
			}

			registryNames.add(activator.registryName);
		}

		final Set<EnumDyeColor> colorSet = new HashSet<>();

		for(EnumDyeColor color : colors) {
			if(color != null) {
				colorSet.add(color);
			}
		}

		if(colorSet.isEmpty()) {
			colors = new EnumDyeColor[] {
					EnumDyeColor.PURPLE
			};
		} else {
			colors = colorSet.toArray(new EnumDyeColor[0]);
		}

		size.ensureCorrect();
		teleportationDelay.ensureCorrect();
	}

	public String getName() {
		return name == null ? "unknown_type" : name;
	}

	public boolean testActivator(ItemStack stack) {
		for(FrameActivator activator : activators) {
			if(activator.test(stack)) {
				return true;
			}
		}

		return false;
	}

	public boolean test(Frame frame) {
		final FrameType frameType = frame.getType();

		if(!type.test(frameType)) {
			return false;
		}

		final World world = frame.getWorld();
		final int dimension = world.provider.getDimension();

		if(whitelist) {
			if(!dimensions.contains(dimension)) {
				return false;
			}
		} else {
			if(dimensions.contains(dimension)) {
				return false;
			}
		}

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

			for(FrameBlock block : frameBlocks) {
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

		for(FrameBlock block : frameBlocks) {
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
