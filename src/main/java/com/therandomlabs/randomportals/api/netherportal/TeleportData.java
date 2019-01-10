package com.therandomlabs.randomportals.api.netherportal;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import com.therandomlabs.randomportals.api.config.PortalType;
import com.therandomlabs.randomportals.api.config.PortalTypes;
import com.therandomlabs.randomportals.api.frame.Frame;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class TeleportData {
	private final World world;
	private final NetherPortal portal;
	private final BlockPos portalPos;
	private IBlockState portalState;

	public TeleportData(World world, NetherPortal portal, BlockPos portalPos,
			IBlockState portalState) {
		this.world = world;
		this.portal = portal;
		this.portalPos = portalPos;
		this.portalState = portalState;
	}

	@Nonnull
	public World getSendingPortalWorld() {
		return world;
	}

	@Nullable
	public NetherPortal getPortal() {
		return portal;
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
}
