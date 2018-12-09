package com.therandomlabs.verticalendportals.frame;

import java.util.List;
import java.util.function.Function;
import com.therandomlabs.verticalendportals.VEPConfig;
import com.therandomlabs.verticalendportals.api.config.FrameSize;
import com.therandomlabs.verticalendportals.api.config.NetherPortalType;
import com.therandomlabs.verticalendportals.api.config.NetherPortalTypes;
import com.therandomlabs.verticalendportals.api.event.NetherPortalEvent;
import com.therandomlabs.verticalendportals.api.frame.Frame;
import com.therandomlabs.verticalendportals.api.frame.FrameDetector;
import com.therandomlabs.verticalendportals.api.frame.FrameType;
import com.therandomlabs.verticalendportals.api.frame.RequiredCorner;
import com.therandomlabs.verticalendportals.api.frame.detector.BasicFrameDetector;
import com.therandomlabs.verticalendportals.block.BlockNetherPortal;
import com.therandomlabs.verticalendportals.block.VEPBlocks;
import com.therandomlabs.verticalendportals.world.storage.NetherPortalSavedData;
import com.therandomlabs.verticalendportals.world.storage.PortalData;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.block.state.pattern.BlockStateMatcher;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;

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

	//pos must be an inner portal position adjacent to a frame position
	public static boolean trySpawn(World world, BlockPos pos, NetherPortalType forcePortalType,
			boolean userCreated) {
		Frame frame = null;
		BlockPos framePos = null;

		for(EnumFacing facing : EnumFacing.values()) {
			final BlockPos offset = pos.offset(facing);
			final IBlockState state = world.getBlockState(offset);

			if(!NetherPortalTypes.getValidBlocks().test(world, pos, state)) {
				continue;
			}

			frame = NetherPortalFrames.EMPTY_FRAMES.detectWithCondition(
					world, offset,
					potentialFrame -> testFrame(
							potentialFrame, offset, facing.getOpposite(), forcePortalType,
							userCreated
					)
			);

			if(frame != null) {
				framePos = offset;
			}

			break;
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

	private static boolean testFrame(Frame frame, BlockPos framePos, EnumFacing inwards,
			NetherPortalType forcePortalType, boolean userCreated) {
		if(!frame.isFacingInwards(framePos, inwards)) {
			return false;
		}

		if(forcePortalType != null) {
			final NetherPortalSavedData savedData = NetherPortalSavedData.get(frame.getWorld());
			savedData.addPortal(new PortalData(forcePortalType, frame, userCreated));
			return true;
		}

		for(NetherPortalType type : NetherPortalTypes.getTypes().values()) {
			if(type.test(frame)) {
				final NetherPortalSavedData savedData = NetherPortalSavedData.get(frame.getWorld());
				savedData.addPortal(new PortalData(type, frame, userCreated));
				return true;
			}
		}

		return false;
	}
}
