package com.therandomlabs.verticalendportals.frame.endportal;

import com.therandomlabs.verticalendportals.block.VEPBlocks;
import com.therandomlabs.verticalendportals.frame.Frame;
import com.therandomlabs.verticalendportals.frame.FrameType;
import com.therandomlabs.verticalendportals.frame.SidedFrameDetector;
import net.minecraft.block.Block;
import net.minecraft.block.state.BlockWorldState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import static net.minecraft.block.BlockEndPortalFrame.EYE;
import static net.minecraft.block.BlockHorizontal.FACING;

public final class VerticalInwardsFacingEndPortalFrameDetector extends SidedFrameDetector {
	public static final VerticalInwardsFacingEndPortalFrameDetector INSTANCE =
			new VerticalInwardsFacingEndPortalFrameDetector();

	public VerticalInwardsFacingEndPortalFrameDetector() {
		super(FrameType.VERTICAL);
	}

	@Override
	protected boolean testTop(FrameType type, BlockWorldState state, int position) {
		return test(
				type, state, VEPBlocks.upside_down_end_portal_frame, null, null
		);
	}

	@Override
	protected boolean testRight(FrameType type, BlockWorldState state, int position) {
		return test(
				type, state, VEPBlocks.vertical_end_portal_frame, EnumFacing.WEST, EnumFacing.SOUTH
		);
	}

	@Override
	protected boolean testBottom(FrameType type, BlockWorldState state, int position) {
		return test(
				type, state, Blocks.END_PORTAL_FRAME, null, null
		);
	}

	@Override
	protected boolean testLeft(FrameType type, BlockWorldState state, int position) {
		return test(
				type, state, VEPBlocks.vertical_end_portal_frame, EnumFacing.EAST, EnumFacing.SOUTH
		);
	}

	@Override
	protected boolean test(Frame frame) {
		return true;
	}

	private boolean test(FrameType type, BlockWorldState state, Block block, EnumFacing facingX,
			EnumFacing facingY) {
		final IBlockState blockState = state.getBlockState();
		final boolean result = blockState.getBlock() == block && blockState.getValue(EYE);

		if(facingX == null) {
			return result;
		}

		final EnumFacing facing = type == FrameType.VERTICAL_X ? facingX : facingY;
		return result && blockState.getValue(FACING) == facing;
	}
}
