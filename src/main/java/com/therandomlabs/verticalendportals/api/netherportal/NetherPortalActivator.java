package com.therandomlabs.verticalendportals.api.netherportal;

import java.util.List;
import com.therandomlabs.verticalendportals.api.config.NetherPortalType;
import com.therandomlabs.verticalendportals.api.config.NetherPortalTypes;
import com.therandomlabs.verticalendportals.api.event.NetherPortalEvent;
import com.therandomlabs.verticalendportals.api.frame.Frame;
import com.therandomlabs.verticalendportals.api.util.StatePredicate;
import com.therandomlabs.verticalendportals.block.BlockNetherPortal;
import com.therandomlabs.verticalendportals.block.VEPBlocks;
import com.therandomlabs.verticalendportals.frame.NetherPortalFrames;
import com.therandomlabs.verticalendportals.world.storage.NetherPortalSavedData;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;

public class NetherPortalActivator {
	static final class PortalContainer {
		NetherPortal portal;
	}

	private NetherPortalType forcePortalType;
	private NetherPortalType[] portalTypes;
	private boolean userCreated = true;
	private boolean activatedByFire;

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

	public NetherPortal activate(World world, BlockPos pos) {
		final StatePredicate validBlocks;

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

		for(EnumFacing facing : EnumFacing.values()) {
			final BlockPos offset = pos.offset(facing);
			final IBlockState state = world.getBlockState(offset);

			if(!validBlocks.test(world, pos, state)) {
				continue;
			}

			NetherPortalFrames.EMPTY_FRAMES.detectWithCondition(world, offset, potentialFrame -> {
				final NetherPortal result = testFrame(potentialFrame, offset, facing.getOpposite());

				if(result == null) {
					return false;
				}

				portal.portal = result;
				return true;
			});

			if(portal.portal != null) {
				framePos = offset;
			}

			break;
		}

		if(portal.portal == null) {
			return null;
		}

		final NetherPortalEvent.Activate event = new NetherPortalEvent.Activate(
				portal.portal, framePos, userCreated, activatedByFire
		);

		if(MinecraftForge.EVENT_BUS.post(event)) {
			return event.getPortal();
		}

		final NetherPortal result = event.getPortal();
		onActivate(world, result);
		return result;
	}

	protected void onActivate(World world, NetherPortal portal) {
		NetherPortalSavedData.get(world).addPortal(portal, userCreated);

		final Frame frame = portal.getFrame();
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
	}

	protected NetherPortal testFrame(Frame frame, BlockPos framePos, EnumFacing inwards) {
		if(!frame.isFacingInwards(framePos, inwards)) {
			return null;
		}

		if(forcePortalType != null) {
			return new NetherPortal(frame, forcePortalType);
		}

		final NetherPortalType[] types;

		if(portalTypes == null) {
			types = NetherPortalTypes.getTypes().values().toArray(new NetherPortalType[0]);
		} else {
			types = portalTypes;
		}

		for(NetherPortalType type : types) {
			if((!activatedByFire || type.canBeActivatedByFire) && type.test(frame)) {
				return new NetherPortal(frame, type);
			}
		}

		return null;
	}
}
