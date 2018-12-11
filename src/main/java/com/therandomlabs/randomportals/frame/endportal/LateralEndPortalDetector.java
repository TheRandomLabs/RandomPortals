package com.therandomlabs.randomportals.frame.endportal;

import java.util.function.Function;
import com.therandomlabs.randomportals.api.config.FrameSize;
import com.therandomlabs.randomportals.api.frame.Frame;
import com.therandomlabs.randomportals.api.frame.FrameType;
import com.therandomlabs.randomportals.api.frame.detector.SidedFrameDetector;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
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
	protected boolean testTop(FrameType type, BlockPos pos, IBlockState state, int position) {
		return test(state, EnumFacing.SOUTH);
	}

	@Override
	protected boolean testRight(FrameType type, BlockPos pos, IBlockState state, int position) {
		return test(state, EnumFacing.WEST);
	}

	@Override
	protected boolean testBottom(FrameType type, BlockPos pos, IBlockState state, int position) {
		return test(state, EnumFacing.NORTH);
	}

	@Override
	protected boolean testLeft(FrameType type, BlockPos pos, IBlockState state, int position) {
		return test(state, EnumFacing.EAST);
	}

	public Block getBlock() {
		return block;
	}

	private boolean test(IBlockState state, EnumFacing facing) {
		return state.getBlock() == block && state.getValue(EYE) && state.getValue(FACING) == facing;
	}
}
