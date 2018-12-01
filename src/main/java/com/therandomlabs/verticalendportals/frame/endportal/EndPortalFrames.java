package com.therandomlabs.verticalendportals.frame.endportal;

import java.util.EnumMap;
import java.util.function.Function;
import com.google.common.collect.ImmutableMap;
import com.therandomlabs.verticalendportals.VEPConfig;
import com.therandomlabs.verticalendportals.api.frame.BasicVerticalFrameDetector;
import com.therandomlabs.verticalendportals.api.frame.FrameDetector;
import com.therandomlabs.verticalendportals.api.frame.FrameSize;
import com.therandomlabs.verticalendportals.api.frame.FrameSizeFunction;
import com.therandomlabs.verticalendportals.api.frame.FrameType;
import com.therandomlabs.verticalendportals.api.frame.RequiredCorner;
import com.therandomlabs.verticalendportals.block.VEPBlocks;
import net.minecraft.block.state.BlockWorldState;
import net.minecraft.block.state.pattern.BlockStateMatcher;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import static net.minecraft.block.BlockEndPortalFrame.EYE;

public final class EndPortalFrames {
	public static final FrameSizeFunction NORMAL_SIZE = FrameSizeFunction.fromJSONs(
			"end_portal/normal", () -> VEPConfig.endPortals.useAllVariantsJson
	);

	public static final Function<FrameType, FrameSize> LATERAL_WITH_VERTICAL_FRAMES_SIZE =
			type -> FrameSize.get("end_portal", "lateral_with_vertical_frames");

	public static final Function<FrameType, FrameSize> UPSIDE_DOWN_SIZE =
			type -> FrameSize.get("end_portal", "upside_down");

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
}
