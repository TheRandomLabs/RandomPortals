package com.therandomlabs.randomportals.api.netherportal;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import com.therandomlabs.randomportals.api.config.NetherPortalType;
import com.therandomlabs.randomportals.api.config.NetherPortalTypes;
import com.therandomlabs.randomportals.api.frame.Frame;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;

public class TeleportData {
	private final NetherPortal portal;
	private final BlockPos portalPos;
	private IBlockState portalState;

	public TeleportData(NetherPortal portal, BlockPos portalPos, IBlockState portalState) {
		this.portal = portal;
		this.portalPos = portalPos;
		this.portalState = portalState;
	}

	@Nullable
	public NetherPortal getPortal() {
		return portal;
	}

	//If frame is not null, frame.getWorld() is not null
	@Nullable
	public Frame getFrame() {
		return portal == null ? null : portal.getFrame();
	}

	@Nullable
	public NetherPortalType getPortalType() {
		return portal == null ? NetherPortalTypes.getDefault() : portal.getType();
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
