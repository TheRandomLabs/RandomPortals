package com.therandomlabs.randomportals.api.netherportal;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.BiFunction;
import com.therandomlabs.randomportals.RPOConfig;
import com.therandomlabs.randomportals.api.config.NetherPortalType;
import com.therandomlabs.randomportals.api.config.NetherPortalTypes;
import com.therandomlabs.randomportals.api.event.NetherPortalEvent;
import com.therandomlabs.randomportals.api.frame.Frame;
import com.therandomlabs.randomportals.api.frame.FrameType;
import com.therandomlabs.randomportals.api.util.FrameStatePredicate;
import com.therandomlabs.randomportals.block.BlockNetherPortal;
import com.therandomlabs.randomportals.block.RPOBlocks;
import com.therandomlabs.randomportals.frame.NetherPortalFrames;
import com.therandomlabs.randomportals.handler.NetherPortalActivationHandler;
import com.therandomlabs.randomportals.world.storage.RPOSavedData;
import net.minecraft.block.Block;
import net.minecraft.block.BlockPortal;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;

public class NetherPortalActivator {
	private static final class PortalContainer {
		NetherPortal portal;
	}

	private static final Random random = new Random();

	private NetherPortalType forcePortalType;
	private NetherPortalType[] portalTypes;
	private boolean userCreated = true;
	private boolean activatedByFire;
	private boolean activationDelayed;

	public NetherPortalType getForcedPortalType() {
		return forcePortalType;
	}

	public NetherPortalActivator forcePortalType(NetherPortalType type) {
		forcePortalType = type;
		return this;
	}

	public NetherPortalType[] getPortalTypes() {
		return portalTypes == null ? null : portalTypes.clone();
	}

	public NetherPortalActivator setPortalTypes(NetherPortalType... portalTypes) {
		if(portalTypes.length == 0) {
			portalTypes = null;
		}

		for(NetherPortalType type : portalTypes) {
			if(type == null) {
				throw new NullPointerException("portalTypes");
			}
		}

		if(portalTypes != null && forcePortalType != null) {
			throw new IllegalStateException(
					"setPortalTypes cannot be called when forcePortalType is not null"
			);
		}

		this.portalTypes = portalTypes;
		return this;
	}

	public boolean isUserCreated() {
		return userCreated;
	}

	public NetherPortalActivator setUserCreated(boolean flag) {
		userCreated = flag;
		return this;
	}

	public boolean isActivatedByFire() {
		return activatedByFire;
	}

	public NetherPortalActivator setActivatedByFire(boolean flag) {
		activatedByFire = flag;
		return this;
	}

	public boolean isActivationDelayed() {
		return activationDelayed;
	}

	public NetherPortalActivator setActivationDelayed(boolean flag) {
		activationDelayed = flag;
		return this;
	}

	public NetherPortal activate(World world, BlockPos pos, ItemStack activator) {
		return activate(world, pos, activator, (axis, color) -> {
			final IBlockState state;

			switch(axis) {
			case X:
				state = ((BlockNetherPortal) Blocks.PORTAL).getByColor(color).getDefaultState();
				break;
			case Y:
				state = RPOBlocks.purple_lateral_nether_portal.getByColor(color).getDefaultState();
				break;
			default:
				state = ((BlockNetherPortal) Blocks.PORTAL).getByColor(color).getDefaultState().
						withProperty(BlockPortal.AXIS, EnumFacing.Axis.Z);
			}

			return state.withProperty(BlockNetherPortal.USER_PLACED, false);
		});
	}

	public NetherPortal activate(World world, BlockPos pos, ItemStack activator,
			IBlockState lateralPortal, IBlockState verticalXPortal, IBlockState verticalZPortal) {
		return activate(world, pos, activator, (axis, color) ->
				FrameType.get(axis, lateralPortal, verticalXPortal, verticalZPortal));
	}

	public NetherPortal activate(World world, BlockPos pos, ItemStack activator,
			BiFunction<EnumFacing.Axis, EnumDyeColor, IBlockState> portalBlocks) {
		if(NetherPortalActivationHandler.isDelayedActivationQueued(world, pos)) {
			return null;
		}

		final FrameStatePredicate validBlocks;

		if(forcePortalType == null) {
			if(portalTypes == null) {
				validBlocks = NetherPortalTypes.getValidBlocks();
			} else {
				validBlocks = NetherPortalTypes.getValidBlocks(portalTypes);
			}
		} else {
			validBlocks = NetherPortalTypes.getValidBlocks(forcePortalType);
		}

		final PortalContainer portal = new PortalContainer();
		BlockPos framePos = null;
		FrameType type = FrameType.LATERAL_OR_VERTICAL;

		for(EnumFacing facing : EnumFacing.values()) {
			final BlockPos offset = pos.offset(facing);
			final IBlockState state = world.getBlockState(offset);

			if(!validBlocks.test(world, pos, state)) {
				continue;
			}

			NetherPortalFrames.EMPTY_FRAMES.detectWithCondition(world, offset, type, frame -> {
				final NetherPortal result =
						testFrame(frame, offset, facing.getOpposite(), activator);

				if(result == null) {
					return false;
				}

				portal.portal = result;
				return true;
			});

			if(portal.portal != null) {
				framePos = offset;
				break;
			}

			//Optimization black magic
			if(facing == EnumFacing.DOWN || facing == EnumFacing.UP) {
				type = FrameType.LATERAL;
			} else {
				break;
			}
		}

		if(portal.portal == null) {
			return null;
		}

		final NetherPortalEvent.Activate event = new NetherPortalEvent.Activate(
				world, portal.portal, framePos, userCreated, activatedByFire
		);

		if(MinecraftForge.EVENT_BUS.post(event)) {
			return null;
		}

		final NetherPortal result = event.getPortal();
		onActivate(world, result, portalBlocks);
		return result;
	}

	protected void onActivate(World world, NetherPortal portal,
			BiFunction<EnumFacing.Axis, EnumDyeColor, IBlockState> portalBlocks) {
		RPOSavedData.get(world).addNetherPortal(portal, userCreated);

		final Frame frame = portal.getFrame();
		final EnumFacing.Axis axis = frame.getType().getAxis();
		final EnumDyeColor[] colors = portal.getType().colors;
		final IBlockState state = portalBlocks.apply(axis, colors[random.nextInt(colors.length)]);
		final Block block = state.getBlock();
		final Class<?> blockClass = block.getClass();
		final BlockNetherPortal portalBlock =
				block instanceof BlockNetherPortal ? (BlockNetherPortal) block : null;

		final List<BlockPos> portalPositions = new ArrayList<>();

		for(BlockPos innerPos : frame.getInnerBlockPositions()) {
			//Allow players to create colorful patterns
			if(portalBlock != null &&
					!RPOConfig.netherPortals.replaceUserPlacedPortalsOnActivation) {
				final IBlockState innerState = world.getBlockState(innerPos);

				if(innerState.getBlock().getClass() == blockClass &&
						innerState.getValue(BlockNetherPortal.USER_PLACED) &&
						portalBlock.getEffectiveAxis(innerState) == axis) {
					continue;
				}
			}

			portalPositions.add(innerPos);
		}

		if(activationDelayed) {
			NetherPortalActivationHandler.queueDelayedActivation(world, portalPositions, state);
		} else {
			for(BlockPos portalPos : portalPositions) {
				world.setBlockState(portalPos, state, 2);
			}
		}
	}

	protected NetherPortal testFrame(Frame frame, BlockPos framePos, EnumFacing inwards,
			ItemStack activator) {
		if(!frame.isFacingInwards(framePos, inwards)) {
			return null;
		}

		if(forcePortalType != null) {
			return new NetherPortal(frame, null, forcePortalType);
		}

		final NetherPortalType[] types;

		if(portalTypes == null) {
			types = NetherPortalTypes.getTypes().values().toArray(new NetherPortalType[0]);
		} else {
			types = portalTypes;
		}

		for(NetherPortalType type : types) {
			if((!activatedByFire || type.activation.canBeActivatedByFire) && type.test(frame) &&
					(forcePortalType != null || activator == null ||
							type.testActivator(activator))) {
				return new NetherPortal(frame, null, type);
			}
		}

		return null;
	}
}
