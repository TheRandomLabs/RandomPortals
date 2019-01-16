package com.therandomlabs.randomportals.api.netherportal;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import com.therandomlabs.randomportals.api.config.PortalType;
import com.therandomlabs.randomportals.api.config.PortalTypes;
import com.therandomlabs.randomportals.api.frame.Frame;
import com.therandomlabs.randomportals.block.BlockNetherPortal;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.Tuple;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class TeleportData {
	private final World world;
	private final BlockPos portalPos;
	private IBlockState portalState;
	private NetherPortal portal;

	private boolean portalChecked;

	public TeleportData(World world, BlockPos portalPos, IBlockState portalState,
			NetherPortal portal) {
		this.world = world;
		this.portalPos = portalPos;
		this.portalState = portalState;
		this.portal = portal;
	}

	@Nonnull
	public World getSendingPortalWorld() {
		return world;
	}

	@Nullable
	public Frame getFrame() {
		return portal == null ? null : portal.getFrame();
	}

	@Nullable
	public PortalType getPortalType() {
		return portal == null ? PortalTypes.getDefault(world) : portal.getType();
	}

	@Nonnull
	public BlockPos getPortalPos() {
		return portalPos;
	}

	@Nonnull
	public IBlockState getPortalState() {
		return portalState;
	}

	@Nullable
	public NetherPortal getPortal() {
		if(portal == null && !portalChecked) {
			final Tuple<Boolean, NetherPortal> tuple =
					BlockNetherPortal.findFrame(world, portalPos);

			if(tuple != null) {
				portal = tuple.getSecond();
			}

			portalChecked = true;
		}

		return portal;
	}
}
