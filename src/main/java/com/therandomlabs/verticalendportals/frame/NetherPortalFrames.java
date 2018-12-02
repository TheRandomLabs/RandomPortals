package com.therandomlabs.verticalendportals.frame;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.therandomlabs.verticalendportals.VEPConfig;
import com.therandomlabs.verticalendportals.api.event.NetherPortalEvent;
import com.therandomlabs.verticalendportals.api.frame.BasicFrameDetector;
import com.therandomlabs.verticalendportals.api.frame.Frame;
import com.therandomlabs.verticalendportals.api.frame.FrameDetector;
import com.therandomlabs.verticalendportals.api.frame.FrameSizeFunction;
import com.therandomlabs.verticalendportals.api.frame.FrameType;
import com.therandomlabs.verticalendportals.api.frame.RequiredCorner;
import com.therandomlabs.verticalendportals.block.BlockNetherPortal;
import com.therandomlabs.verticalendportals.block.VEPBlocks;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockWorldState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.block.state.pattern.BlockStateMatcher;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;

public final class NetherPortalFrames {
	public static final FrameSizeFunction SIZE = FrameSizeFunction.fromJSONs(
			"nether_portal", () -> VEPConfig.netherPortals.useAllVariantsJson
	);

	public static final FrameDetector FRAMES = new BasicFrameDetector(
			SIZE,
			() -> VEPConfig.netherPortalFrameBlocks.keySet(),
			RequiredCorner.ANY_NON_AIR,
			frame -> true,
			(world, state) -> true
	);

	public static final FrameDetector EMPTY_FRAMES = new BasicFrameDetector(
			SIZE,
			() -> VEPConfig.netherPortalFrameBlocks.keySet(),
			RequiredCorner.ANY_NON_AIR,
			frame -> frame.testInnerBlocks(NetherPortalFrames::isEmpty),
			NetherPortalFrames::isEmpty
	);

	public static final FrameDetector ACTIVATED_FRAMES = new BasicFrameDetector(
			SIZE,
			() -> VEPConfig.netherPortalFrameBlocks.keySet(),
			RequiredCorner.ANY_NON_AIR,
			NetherPortalFrames::isActivated,
			(world, state) -> state.getBlockState().getBlock() instanceof BlockNetherPortal
	);

	private NetherPortalFrames() {}

	public static boolean isEmpty(World world, BlockWorldState state) {
		final IBlockState blockState = state.getBlockState();
		final Material material = blockState.getMaterial();

		if(material == Material.AIR || material == Material.FIRE || material == Material.PORTAL) {
			return true;
		}

		return blockState.getBlock().isReplaceable(world, state.getPos());
	}

	public static boolean isActivated(Frame frame) {
		final BlockStateMatcher portal;
		final FrameType type = frame.getType();

		if(type == FrameType.LATERAL) {
			portal = BlockNetherPortal.Matcher.LATERAL;
		} else if(type == FrameType.VERTICAL_X) {
			portal = BlockNetherPortal.Matcher.VERTICAL_X;
		} else {
			portal = BlockNetherPortal.Matcher.VERTICAL_Z;
		}

		for(IBlockState state : frame.getInnerBlocks()) {
			if(!portal.apply(state)) {
				return false;
			}
		}

		return true;
	}

	public static boolean trySpawn(World world, BlockPos pos) {
		Frame frame = null;
		BlockPos framePos = null;

		for(EnumFacing facing : EnumFacing.values()) {
			final BlockPos offset = pos.offset(facing);
			final IBlockState state = world.getBlockState(offset);

			if(VEPConfig.netherPortalFrameBlocks.containsKey(state.getBlock())) {
				frame = NetherPortalFrames.EMPTY_FRAMES.detectWithCondition(
						world, offset,
						potentialFrame -> testFrame(potentialFrame, offset, facing.getOpposite())
				);

				if(frame != null) {
					framePos = offset;
					break;
				}
			}
		}

		if(frame == null) {
			return false;
		}

		if(MinecraftForge.EVENT_BUS.post(new NetherPortalEvent.Activate(frame, framePos))) {
			return false;
		}

		final EnumFacing.Axis axis = frame.getType().getAxis();
		IBlockState state;

		if(axis == EnumFacing.Axis.Y) {
			state = VEPBlocks.lateral_nether_portal.getDefaultState();
		} else {
			state = VEPBlocks.vertical_nether_portal.getDefaultState().
					withProperty(BlockNetherPortal.AXIS, axis);
		}

		state = state.withProperty(BlockNetherPortal.USER_PLACED, false);

		final List<BlockPos> innerBlockPositions = frame.getInnerBlockPositions();

		for(BlockPos innerPos : innerBlockPositions) {
			world.setBlockState(innerPos, state, 2);
		}

		return true;
	}

	private static boolean testFrame(Frame frame, BlockPos framePos, EnumFacing inwards) {
		if(!frame.isFacingInwards(framePos, inwards)) {
			return false;
		}

		boolean shouldTestAmounts = false;

		for(Map.Entry<Block, Integer> blocks : VEPConfig.netherPortalFrameBlocks.entrySet()) {
			if(blocks.getValue() != 0) {
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

		for(Map.Entry<Block, Integer> blocks : VEPConfig.netherPortalFrameBlocks.entrySet()) {
			final int requiredAmount = blocks.getValue();

			if(requiredAmount == 0) {
				continue;
			}

			final Integer detectedAmount = detectedBlocks.get(blocks.getKey());

			if(detectedAmount == null || detectedAmount < requiredAmount) {
				return false;
			}
		}

		return true;
	}
}