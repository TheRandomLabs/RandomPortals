package com.therandomlabs.verticalendportals.frame.endportal;

import java.util.function.Function;
import com.therandomlabs.verticalendportals.api.frame.Frame;
import com.therandomlabs.verticalendportals.api.frame.FrameSize;
import com.therandomlabs.verticalendportals.api.frame.FrameType;
import com.therandomlabs.verticalendportals.api.frame.SidedFrameDetector;
import net.minecraft.block.Block;
import net.minecraft.block.state.BlockWorldState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import static net.minecraft.block.BlockEndPortalFrame.EYE;
import static net.minecraft.block.BlockHorizontal.FACING;

public final class LateralEndPortalDetector extends SidedFrameDetector {
	private final Block block;
	private final Function<FrameType, FrameSize> defaultSize;

	public LateralEndPortalDetector(Block block, Function<FrameType, FrameSize> defaultSize) {
		super(FrameType.LATERAL);
		this.block = block;
		this.defaultSize = defaultSize;
	}

	@Override
	public Function<FrameType, FrameSize> getDefaultSize() {
		return defaultSize;
	}

	@Override
	protected boolean test(Frame frame) {
		return true;
	}

	@Override
	protected boolean testTop(FrameType type, BlockWorldState state, int position) {
		return test(state, EnumFacing.SOUTH);
	}

	@Override
	protected boolean testRight(FrameType type, BlockWorldState state, int position) {
		return test(state, EnumFacing.WEST);
	}

	@Override
	protected boolean testBottom(FrameType type, BlockWorldState state, int position) {
		return test(state, EnumFacing.NORTH);
	}

	@Override
	protected boolean testLeft(FrameType type, BlockWorldState state, int position) {
		return test(state, EnumFacing.EAST);
	}

	public Block getBlock() {
		return block;
	}

	private boolean test(BlockWorldState state, EnumFacing facing) {
		final IBlockState blockState = state.getBlockState();
		return blockState.getBlock() == block && blockState.getValue(EYE) &&
				blockState.getValue(FACING) == facing;
	}
}
