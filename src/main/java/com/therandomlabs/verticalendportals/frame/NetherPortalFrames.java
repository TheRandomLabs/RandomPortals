package com.therandomlabs.verticalendportals.frame;

import java.util.function.Function;
import com.therandomlabs.verticalendportals.VEPConfig;
import com.therandomlabs.verticalendportals.api.config.FrameSize;
import com.therandomlabs.verticalendportals.api.config.NetherPortalTypes;
import com.therandomlabs.verticalendportals.api.frame.Frame;
import com.therandomlabs.verticalendportals.api.frame.FrameDetector;
import com.therandomlabs.verticalendportals.api.frame.FrameType;
import com.therandomlabs.verticalendportals.api.frame.RequiredCorner;
import com.therandomlabs.verticalendportals.api.frame.detector.BasicFrameDetector;
import com.therandomlabs.verticalendportals.api.util.StatePredicate;
import com.therandomlabs.verticalendportals.block.BlockNetherPortal;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public final class NetherPortalFrames {
	public static final Function<FrameType, FrameSize> SIZE = FrameSize.fromJSONs(
			"nether_portal", () -> VEPConfig.netherPortals.useAllVariantsJson
	);

	public static final FrameDetector FRAMES = new BasicFrameDetector(
			SIZE,
			NetherPortalTypes::getValidBlocks,
			RequiredCorner.ANY_NON_AIR,
			frame -> true,
			(world, pos, state) -> true
	);

	public static final FrameDetector EMPTY_FRAMES = new BasicFrameDetector(
			SIZE,
			NetherPortalTypes::getValidBlocks,
			RequiredCorner.ANY_NON_AIR,
			frame -> frame.testInnerBlocks(NetherPortalFrames::isEmpty),
			NetherPortalFrames::isEmpty
	);

	public static final FrameDetector ACTIVATED_FRAMES = new BasicFrameDetector(
			SIZE,
			NetherPortalTypes::getValidBlocks,
			RequiredCorner.ANY_NON_AIR,
			NetherPortalFrames::isActivated,
			(world, pos, state) -> state.getBlock() instanceof BlockNetherPortal
	);

	private NetherPortalFrames() {}

	public static boolean isEmpty(World world, BlockPos pos, IBlockState state) {
		final Material material = state.getMaterial();

		if(material == Material.AIR || material == Material.FIRE || material == Material.PORTAL) {
			return true;
		}

		return state.getBlock().isReplaceable(world, pos);
	}

	public static boolean isActivated(Frame frame) {
		return isActivated(frame, BlockNetherPortal.Matcher::ofType);
	}

	@SuppressWarnings("Duplicates")
	public static boolean isActivated(Frame frame, StatePredicate lateralPortal,
			StatePredicate verticalXPortal, StatePredicate verticalZPortal) {
		return isActivated(frame, type -> {
			if(type == FrameType.LATERAL) {
				return lateralPortal;
			}

			if(type == FrameType.VERTICAL_X) {
				return verticalXPortal;
			}

			return verticalZPortal;
		});
	}

	public static boolean isActivated(Frame frame,
			Function<FrameType, StatePredicate> portalFunction) {
		final StatePredicate portal = portalFunction.apply(frame.getType());
		final World world = frame.getWorld();

		for(BlockPos pos : frame.getInnerBlockPositions()) {
			if(!portal.test(world, pos, world.getBlockState(pos))) {
				return false;
			}
		}

		return true;
	}
}
