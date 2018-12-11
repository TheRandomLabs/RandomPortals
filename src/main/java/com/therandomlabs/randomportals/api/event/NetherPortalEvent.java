package com.therandomlabs.randomportals.api.event;

import com.therandomlabs.randomportals.api.frame.Frame;
import com.therandomlabs.randomportals.api.netherportal.NetherPortal;
import net.minecraft.entity.Entity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.eventhandler.Cancelable;
import net.minecraftforge.fml.common.eventhandler.Event;

public class NetherPortalEvent extends Event {
	@Cancelable
	public static class Activate extends NetherPortalEvent {
		private NetherPortal portal;
		private final BlockPos activatedFrameBlock;
		private final boolean userCreated;
		private final boolean activatedByFire;

		public Activate(NetherPortal portal, BlockPos activatedFrameBlock, boolean userCreated,
				boolean activatedByFire) {
			super(portal.getFrame());
			this.portal = portal;
			this.activatedFrameBlock = activatedFrameBlock;
			this.userCreated = userCreated;
			this.activatedByFire = activatedByFire;
		}

		public NetherPortal getPortal() {
			return portal;
		}

		public void setPortal(NetherPortal portal) {
			this.portal = portal;
		}

		public BlockPos getActivatedFrameBlock() {
			return activatedFrameBlock;
		}

		public boolean isUserCreated() {
			return userCreated;
		}

		public boolean isActivatedByFire() {
			return activatedByFire;
		}
	}

	@Cancelable
	public static class Teleport extends NetherPortalEvent {
		private final NetherPortal portal;
		private final Entity entity;
		private final BlockPos portalPos;
		private final EnumFacing originalEntityFacing;

		public Teleport(NetherPortal portal, Entity entity, BlockPos portalPos,
				EnumFacing originalEntityFacing) {
			super(portal == null ? null : portal.getFrame());
			this.portal = portal;
			this.entity = entity;
			this.portalPos = portalPos;
			this.originalEntityFacing = originalEntityFacing;
		}

		public NetherPortal getPortal() {
			return portal;
		}

		public Entity getEntity() {
			return entity;
		}

		public BlockPos getPortalPos() {
			return portalPos;
		}

		public EnumFacing getOriginalEntityFacing() {
			return originalEntityFacing;
		}
	}

	private final Frame frame;

	public NetherPortalEvent(Frame frame) {
		this.frame = frame;
	}

	public Frame getFrame() {
		return frame;
	}
}
