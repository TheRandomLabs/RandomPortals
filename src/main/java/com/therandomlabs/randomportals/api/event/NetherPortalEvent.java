package com.therandomlabs.randomportals.api.event;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import com.therandomlabs.randomportals.api.frame.Frame;
import com.therandomlabs.randomportals.api.netherportal.NetherPortal;
import net.minecraft.entity.Entity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.eventhandler.Cancelable;
import net.minecraftforge.fml.common.eventhandler.Event;

public class NetherPortalEvent extends Event {
	@Cancelable
	public static class Activate extends NetherPortalEvent {
		private final NetherPortal portal;
		private final BlockPos activatedFramePos;
		private final boolean userCreated;
		private final boolean activatedByFire;

		public Activate(World world, NetherPortal portal, BlockPos activatedFramePos,
				boolean userCreated, boolean activatedByFire) {
			super(world, portal.getFrame());
			this.portal = portal;
			this.activatedFramePos = activatedFramePos;
			this.userCreated = userCreated;
			this.activatedByFire = activatedByFire;
		}

		@Nonnull
		public Frame getFrame() {
			return frame;
		}

		@Nonnull
		public NetherPortal getPortal() {
			return portal;
		}

		public BlockPos getActivatedFramePos() {
			return activatedFramePos;
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
			super(entity.getEntityWorld(), portal == null ? null : portal.getFrame());
			this.portal = portal;
			this.entity = entity;
			this.portalPos = portalPos;
			this.originalEntityFacing = originalEntityFacing;
		}

		@Nullable
		public Frame getFrame() {
			return frame;
		}

		@Nullable
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

	protected final World world;
	protected final Frame frame;

	public NetherPortalEvent(World world, Frame frame) {
		this.world = world;
		this.frame = frame;
	}

	@Nonnull
	public World getWorld() {
		return world;
	}
}
