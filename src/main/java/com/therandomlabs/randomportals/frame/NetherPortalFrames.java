package com.therandomlabs.randomportals.frame;

import java.util.function.Function;
import com.therandomlabs.randomportals.api.config.PortalTypes;
import com.therandomlabs.randomportals.api.frame.Frame;
import com.therandomlabs.randomportals.api.frame.FrameDetector;
import com.therandomlabs.randomportals.api.frame.FrameType;
import com.therandomlabs.randomportals.api.frame.RequiredCorner;
import com.therandomlabs.randomportals.api.frame.detector.BasicFrameDetector;
import com.therandomlabs.randomportals.api.util.FrameStatePredicate;
import com.therandomlabs.randomportals.block.BlockNetherPortal;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public final class NetherPortalFrames {
	public static final FrameDetector FRAMES = new BasicFrameDetector(
			PortalTypes::getSize,
			PortalTypes::getValidBlocks,
			RequiredCorner.ANY,
			frame -> true,
			(world, pos, state, type) -> true
	);

	public static final FrameDetector EMPTY_FRAMES = new BasicFrameDetector(
			PortalTypes::getSize,
			PortalTypes::getValidBlocks,
			RequiredCorner.ANY,
			frame -> frame.testInnerBlocks(NetherPortalFrames::isEmpty),
			NetherPortalFrames::isEmpty
	);

	public static final FrameDetector ACTIVATED_FRAMES = new BasicFrameDetector(
			PortalTypes::getSize,
			PortalTypes::getValidBlocks,
			RequiredCorner.ANY_NON_AIR,
			NetherPortalFrames::isActivated,
			(world, pos, state, type) -> state.getBlock() instanceof BlockNetherPortal
	);

	private NetherPortalFrames() {}

	public static boolean isEmpty(World world, BlockPos pos, IBlockState state, FrameType type) {
		final Material material = state.getMaterial();

		if(material == Material.AIR || material == Material.FIRE) {
			return true;
		}

		final Block block = state.getBlock();

		if(block instanceof BlockNetherPortal) {
			final BlockNetherPortal portalBlock = (BlockNetherPortal) block;

			if(portalBlock.getEffectiveAxis(state) == type.getAxis()) {
				return true;
			}
		}

		return block.isReplaceable(world, pos);
	}

	public static boolean isActivated(Frame frame) {
		return isActivated(frame, BlockNetherPortal.Matcher::ofType);
	}

	public static boolean isActivated(Frame frame, FrameStatePredicate lateralPortal,
			FrameStatePredicate verticalXPortal, FrameStatePredicate verticalZPortal) {
		return isActivated(
				frame, type -> type.get(lateralPortal, verticalXPortal, verticalZPortal)
		);
	}

	public static boolean isActivated(Frame frame,
			Function<FrameType, FrameStatePredicate> portalFunction) {
		final FrameType type = frame.getType();
		final FrameStatePredicate portal = portalFunction.apply(type);
		final World world = frame.getWorld();

		for(BlockPos pos : frame.getInnerBlockPositions()) {
			if(!portal.test(world, pos, world.getBlockState(pos), type)) {
				return false;
			}
		}

		return true;
	}
}
