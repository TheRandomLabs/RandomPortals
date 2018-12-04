package com.therandomlabs.verticalendportals.frame.endportal;

import java.util.EnumMap;
import java.util.List;
import java.util.function.Function;
import com.google.common.collect.ImmutableMap;
import com.therandomlabs.verticalendportals.api.event.EndPortalEvent;
import com.therandomlabs.verticalendportals.api.frame.BasicVerticalFrameDetector;
import com.therandomlabs.verticalendportals.api.frame.Frame;
import com.therandomlabs.verticalendportals.api.frame.FrameDetector;
import com.therandomlabs.verticalendportals.api.frame.FrameSize;
import com.therandomlabs.verticalendportals.api.frame.FrameSizeFunction;
import com.therandomlabs.verticalendportals.api.frame.FrameType;
import com.therandomlabs.verticalendportals.api.frame.RequiredCorner;
import com.therandomlabs.verticalendportals.block.VEPBlocks;
import com.therandomlabs.verticalendportals.config.FrameSizes;
import com.therandomlabs.verticalendportals.config.VEPConfig;
import net.minecraft.block.Block;
import net.minecraft.block.state.BlockWorldState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.block.state.pattern.BlockStateMatcher;
import net.minecraft.init.Blocks;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import static net.minecraft.block.BlockEndPortalFrame.EYE;
import static net.minecraft.block.BlockHorizontal.FACING;

public final class EndPortalFrames {
	public static final FrameSizeFunction NORMAL_SIZE = FrameSizeFunction.fromJSONs(
			"end_portal/normal", () -> VEPConfig.endPortals.useAllVariantsJson
	);

	public static final Function<FrameType, FrameSize> LATERAL_WITH_VERTICAL_FRAMES_SIZE =
			type -> FrameSizes.get("end_portal", "lateral_with_vertical_frames");

	public static final Function<FrameType, FrameSize> UPSIDE_DOWN_SIZE =
			type -> FrameSizes.get("end_portal", "upside_down");

	public static final FrameSizeFunction VERTICAL_INWARDS_FACING_SIZE =
			FrameSizeFunction.fromJSONsVertical(
					"end_portal/inwards_facing", () -> VEPConfig.endPortals.useAllVariantsJson
			);

	public static final FrameDetector LATERAL =
			new LateralEndPortalDetector(Blocks.END_PORTAL_FRAME, NORMAL_SIZE);

	public static final FrameDetector LATERAL_WITH_VERTICAL_FRAMES = new LateralEndPortalDetector(
			VEPBlocks.vertical_end_portal_frame, LATERAL_WITH_VERTICAL_FRAMES_SIZE
	);

	public static final FrameDetector UPSIDE_DOWN =
			new LateralEndPortalDetector(VEPBlocks.upside_down_end_portal_frame, UPSIDE_DOWN_SIZE);

	public static final ImmutableMap<EnumFacing, FrameDetector> VERTICAL;

	public static final FrameDetector VERTICAL_INWARDS_FACING =
			new VerticalInwardsEndPortalFrameDetector();

	public static final FrameSizeFunction SIZE = FrameSizeFunction.fromJSONs(
			"end_portal", () -> VEPConfig.endPortals.useAllVariantsJson
	);

	static {
		final EnumMap<EnumFacing, FrameDetector> vertical = new EnumMap<>(EnumFacing.class);

		vertical.put(EnumFacing.NORTH, new BasicVerticalFrameDetector(
				NORMAL_SIZE,
				BlockWorldState.hasState(
						BlockStateMatcher.forBlock(VEPBlocks.vertical_end_portal_frame).
								where(EYE, eye -> eye)
				),
				EnumFacing.NORTH,
				RequiredCorner.ANY,
				frame -> true
		));

		vertical.put(EnumFacing.EAST, new BasicVerticalFrameDetector(
				NORMAL_SIZE,
				BlockWorldState.hasState(
						BlockStateMatcher.forBlock(VEPBlocks.vertical_end_portal_frame).
								where(EYE, eye -> eye)
				),
				EnumFacing.EAST,
				RequiredCorner.ANY,
				frame -> true
		));

		vertical.put(EnumFacing.SOUTH, new BasicVerticalFrameDetector(
				NORMAL_SIZE,
				BlockWorldState.hasState(
						BlockStateMatcher.forBlock(VEPBlocks.vertical_end_portal_frame).
								where(EYE, eye -> eye)
				),
				EnumFacing.SOUTH,
				RequiredCorner.ANY,
				frame -> true
		));

		vertical.put(EnumFacing.WEST, new BasicVerticalFrameDetector(
				NORMAL_SIZE,
				BlockWorldState.hasState(
						BlockStateMatcher.forBlock(VEPBlocks.vertical_end_portal_frame).
								where(EYE, eye -> eye)
				),
				EnumFacing.WEST,
				RequiredCorner.ANY,
				frame -> true
		));

		VERTICAL = ImmutableMap.copyOf(vertical);
	}

	private EndPortalFrames() {}

	//pos must be an activated portal frame block position
	public static boolean trySpawn(World world, BlockPos pos) {
		final IBlockState state = world.getBlockState(pos);
		final Block block = state.getBlock();

		world.playSound(
				null, pos, SoundEvents.BLOCK_END_PORTAL_FRAME_FILL, SoundCategory.BLOCKS, 1.0F, 1.0F
		);

		Frame frame = null;

		if(block == Blocks.END_PORTAL_FRAME) {
			frame = LATERAL.detect(world, pos);
		} else if(block == VEPBlocks.vertical_end_portal_frame) {
			frame = LATERAL_WITH_VERTICAL_FRAMES.detect(world, pos);
		} else if(block == VEPBlocks.upside_down_end_portal_frame) {
			frame = UPSIDE_DOWN.detect(world, pos);
		}

		if(frame != null && !frame.isCorner(pos)) {
			if(MinecraftForge.EVENT_BUS.post(new EndPortalEvent.Activate(frame, pos))) {
				return false;
			}

			final IBlockState portalState;

			if(block == VEPBlocks.upside_down_end_portal_frame) {
				portalState = VEPBlocks.upside_down_end_portal.getDefaultState();
			} else {
				portalState = VEPBlocks.lateral_end_portal.getDefaultState();
			}

			for(BlockPos innerPos : frame.getInnerBlockPositions()) {
				world.setBlockState(innerPos, portalState, 2);
			}

			world.playBroadcastSound(1038, frame.getTopLeft().add(1, 0, 1), 0);

			return true;
		}

		final EnumFacing facing = state.getValue(FACING);
		EnumFacing portalFacing = null;

		if(block == VEPBlocks.vertical_end_portal_frame) {
			frame = VERTICAL.get(facing).detect(world, pos);
			portalFacing = facing;
		}

		if(frame == null) {
			frame = VERTICAL_INWARDS_FACING.detect(world, pos);

			if(frame != null) {
				portalFacing = frame.getType() == FrameType.VERTICAL_X ?
						EnumFacing.NORTH : EnumFacing.EAST;
			}
		}

		if(frame == null || frame.isCorner(pos) ||
				MinecraftForge.EVENT_BUS.post(new EndPortalEvent.Activate(frame, pos))) {
			return false;
		}

		final List<BlockPos> innerBlockPositions = frame.getInnerBlockPositions();

		for(BlockPos innerPos : innerBlockPositions) {
			world.setBlockState(
					innerPos, VEPBlocks.vertical_end_portal.getDefaultState().withProperty(
							FACING, portalFacing
					), 2
			);
		}

		world.playBroadcastSound(1038, innerBlockPositions.get(0), 0);

		return true;
	}
}
