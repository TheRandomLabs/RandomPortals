package com.therandomlabs.verticalendportals.frame;

import com.therandomlabs.verticalendportals.VEPConfig;
import com.therandomlabs.verticalendportals.api.frame.BasicFrameDetector;
import com.therandomlabs.verticalendportals.api.frame.Frame;
import com.therandomlabs.verticalendportals.api.frame.FrameDetector;
import com.therandomlabs.verticalendportals.api.frame.FrameSizeFunction;
import com.therandomlabs.verticalendportals.api.frame.FrameType;
import com.therandomlabs.verticalendportals.api.frame.RequiredCorner;
import com.therandomlabs.verticalendportals.block.BlockNetherPortal;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockWorldState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.block.state.pattern.BlockStateMatcher;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;

public final class NetherPortalFrames {
	public static final FrameSizeFunction SIZE = FrameSizeFunction.fromJSONs(
			"nether_portal", () -> VEPConfig.netherPortals.useAllVariantsJson
	);

	public static final FrameDetector EMPTY_FRAMES = new BasicFrameDetector(
			SIZE,
			Blocks.OBSIDIAN,
			RequiredCorner.ANY_NON_AIR,
			frame -> frame.testInnerBlocks(NetherPortalFrames::isEmpty)
	);

	public static final FrameDetector ACTIVATED_FRAMES = new BasicFrameDetector(
			SIZE,
			Blocks.OBSIDIAN,
			RequiredCorner.ANY_NON_AIR,
			NetherPortalFrames::isActivated
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
}
