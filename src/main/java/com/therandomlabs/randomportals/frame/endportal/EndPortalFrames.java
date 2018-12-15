package com.therandomlabs.randomportals.frame.endportal;

import java.util.EnumMap;
import java.util.List;
import java.util.function.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.therandomlabs.randomportals.RPOConfig;
import com.therandomlabs.randomportals.api.config.FrameSize;
import com.therandomlabs.randomportals.api.config.FrameSizes;
import com.therandomlabs.randomportals.api.event.EndPortalEvent;
import com.therandomlabs.randomportals.api.frame.Frame;
import com.therandomlabs.randomportals.api.frame.FrameDetector;
import com.therandomlabs.randomportals.api.frame.FrameType;
import com.therandomlabs.randomportals.api.frame.RequiredCorner;
import com.therandomlabs.randomportals.api.frame.detector.BasicVerticalFrameDetector;
import com.therandomlabs.randomportals.api.util.FrameStatePredicate;
import com.therandomlabs.randomportals.block.RPOBlocks;
import com.therandomlabs.randomportals.world.storage.RPOSavedData;
import net.minecraft.block.Block;
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
	public static final Function<FrameType, FrameSize> NORMAL = FrameSize.fromJSONs(
			"end_portal/normal", () -> RPOConfig.endPortals.useAllVariantsJson
	);

	public static final Function<FrameType, FrameSize> LATERAL_WITH_VERTICAL_FRAMES_SIZE =
			type -> FrameSizes.get("end_portal", "lateral_with_vertical_frames");

	public static final Function<FrameType, FrameSize> UPSIDE_DOWN_SIZE =
			type -> FrameSizes.get("end_portal", "upside_down");

	public static final Function<FrameType, FrameSize> VERTICAL_INWARDS_FACING_SIZE =
			FrameSize.fromJSONs(
					"end_portal/inwards_facing", () -> RPOConfig.endPortals.useAllVariantsJson
			);

	public static final ImmutableList<Function<FrameType, FrameSize>> SIZES = ImmutableList.of(
			EndPortalFrames.NORMAL,
			EndPortalFrames.LATERAL_WITH_VERTICAL_FRAMES_SIZE,
			EndPortalFrames.UPSIDE_DOWN_SIZE,
			EndPortalFrames.VERTICAL_INWARDS_FACING_SIZE
	);

	public static final FrameDetector LATERAL =
			new LateralEndPortalDetector(Blocks.END_PORTAL_FRAME, NORMAL);

	public static final FrameDetector LATERAL_WITH_VERTICAL_FRAMES = new LateralEndPortalDetector(
			RPOBlocks.vertical_end_portal_frame, LATERAL_WITH_VERTICAL_FRAMES_SIZE
	);

	public static final FrameDetector UPSIDE_DOWN =
			new LateralEndPortalDetector(RPOBlocks.upside_down_end_portal_frame, UPSIDE_DOWN_SIZE);

	public static final ImmutableMap<EnumFacing, FrameDetector> VERTICAL;

	public static final FrameDetector VERTICAL_INWARDS_FACING =
			new VerticalInwardsEndPortalFrameDetector();

	static {
		final EnumMap<EnumFacing, FrameDetector> vertical = new EnumMap<>(EnumFacing.class);

		vertical.put(EnumFacing.NORTH, new BasicVerticalFrameDetector(
				NORMAL,
				FrameStatePredicate.of(
						BlockStateMatcher.forBlock(RPOBlocks.vertical_end_portal_frame).
								where(EYE, eye -> eye)
				),
				RequiredCorner.ANY,
				EnumFacing.NORTH,
				frame -> true
		));

		vertical.put(EnumFacing.EAST, new BasicVerticalFrameDetector(
				NORMAL,
				FrameStatePredicate.of(
						BlockStateMatcher.forBlock(RPOBlocks.vertical_end_portal_frame).
								where(EYE, eye -> eye)
				),
				RequiredCorner.ANY,
				EnumFacing.EAST,
				frame -> true
		));

		vertical.put(EnumFacing.SOUTH, new BasicVerticalFrameDetector(
				NORMAL,
				FrameStatePredicate.of(
						BlockStateMatcher.forBlock(RPOBlocks.vertical_end_portal_frame).
								where(EYE, eye -> eye)
				),
				RequiredCorner.ANY,
				EnumFacing.SOUTH,

				frame -> true
		));

		vertical.put(EnumFacing.WEST, new BasicVerticalFrameDetector(
				NORMAL,
				FrameStatePredicate.of(
						BlockStateMatcher.forBlock(RPOBlocks.vertical_end_portal_frame).
								where(EYE, eye -> eye)
				),
				RequiredCorner.ANY,
				EnumFacing.WEST,
				frame -> true
		));

		VERTICAL = ImmutableMap.copyOf(vertical);
	}

	private EndPortalFrames() {}

	public static Frame activate(World world, BlockPos framePos) {
		final IBlockState state = world.getBlockState(framePos);
		final Block block = state.getBlock();

		world.playSound(
				null, framePos, SoundEvents.BLOCK_END_PORTAL_FRAME_FILL, SoundCategory.BLOCKS,
				1.0F, 1.0F
		);

		Frame frame;

		if(block == Blocks.END_PORTAL_FRAME) {
			frame = LATERAL.detect(world, framePos);
		} else if(block == RPOBlocks.vertical_end_portal_frame) {
			frame = LATERAL_WITH_VERTICAL_FRAMES.detect(world, framePos);
		} else {
			frame = UPSIDE_DOWN.detect(world, framePos);
		}

		if(frame != null && !frame.isCorner(framePos)) {
			final EndPortalEvent.Activate event =
					new EndPortalEvent.Activate(world, frame, framePos);

			if(MinecraftForge.EVENT_BUS.post(event)) {
				return null;
			}

			final IBlockState portalState;

			if(block == RPOBlocks.upside_down_end_portal_frame) {
				portalState = RPOBlocks.upside_down_end_portal.getDefaultState();
			} else {
				portalState = Blocks.END_PORTAL.getDefaultState();
			}

			for(BlockPos innerPos : frame.getInnerBlockPositions()) {
				world.setBlockState(innerPos, portalState, 2);
			}

			world.playBroadcastSound(1038, frame.getTopLeft().add(1, 0, 1), 0);

			RPOSavedData.get(world).addEndPortal(frame);
			return frame;
		}

		final EnumFacing facing = state.getValue(FACING);
		EnumFacing portalFacing = null;

		if(block == RPOBlocks.vertical_end_portal_frame) {
			frame = VERTICAL.get(facing).detect(world, framePos);
			portalFacing = facing;
		}

		if(frame == null) {
			frame = VERTICAL_INWARDS_FACING.detect(world, framePos);

			if(frame != null) {
				portalFacing = frame.getType() == FrameType.VERTICAL_X ?
						EnumFacing.NORTH : EnumFacing.EAST;
			}
		}

		if(frame == null || frame.isCorner(framePos)) {
			return null;
		}

		final EndPortalEvent.Activate event = new EndPortalEvent.Activate(world, frame, framePos);

		if(MinecraftForge.EVENT_BUS.post(event)) {
			return null;
		}

		final List<BlockPos> innerBlockPositions = frame.getInnerBlockPositions();
		final IBlockState portalState =
				RPOBlocks.vertical_end_portal.getDefaultState().withProperty(FACING, portalFacing);

		for(BlockPos innerPos : innerBlockPositions) {
			world.setBlockState(innerPos, portalState, 2);
		}

		world.playBroadcastSound(1038, innerBlockPositions.get(0), 0);

		RPOSavedData.get(world).addEndPortal(frame);
		return frame;
	}

	public static void registerSizes() {
		FrameSizes.register("end_portal/normal");
		FrameSizes.register("end_portal", "lateral_with_vertical_frames", "upside_down");
		FrameSizes.registerVertical("end_portal/inwards_facing");
	}
}
