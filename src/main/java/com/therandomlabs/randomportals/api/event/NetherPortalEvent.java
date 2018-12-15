package com.therandomlabs.randomportals.api.event;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import com.therandomlabs.randomportals.api.frame.Frame;
import com.therandomlabs.randomportals.api.netherportal.NetherPortal;
import com.therandomlabs.randomportals.api.netherportal.TeleportData;
import net.minecraft.entity.Entity;
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

		@Override
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

	public static class Teleport extends NetherPortalEvent {
		@Cancelable
		public static class Pre extends Teleport {
			public Pre(Entity entity, TeleportData data) {
				super(entity, data);
			}
		}

		/**
		 * Posted before RPOTeleporter starts searching for a destination.
		 */
		@Cancelable
		public static class SearchingForDestination extends Teleport {
			public SearchingForDestination(Entity entity, TeleportData data) {
				super(entity, data);
			}
		}

		/**
		 * Posted after RPOTeleporter has found a destination, but before the entity is moved
		 * to that destination.
		 */
		@Cancelable
		public static class DestinationFound extends Teleport {
			private final Frame receivingFrame;
			private final double x;
			private final double y;
			private final double z;
			private final float yaw;
			private final float pitch;

			public DestinationFound(Entity entity, TeleportData data, Frame receivingFrame,
					double x, double y, double z, float yaw, float pitch) {
				super(entity, data);
				this.receivingFrame = receivingFrame;
				this.x = x;
				this.y = y;
				this.z = z;
				this.yaw = yaw;
				this.pitch = pitch;
			}

			@Nonnull
			public Frame getReceivingFrame() {
				return receivingFrame;
			}

			public double getX() {
				return x;
			}

			public double getY() {
				return y;
			}

			public double getZ() {
				return z;
			}

			public float getYaw() {
				return yaw;
			}

			public float getPitch() {
				return pitch;
			}
		}

		protected final Entity entity;
		protected final TeleportData data;

		protected Teleport(Entity entity, TeleportData data) {
			super(entity.getEntityWorld(), data.getFrame());
			this.entity = entity;
			this.data = data;
		}

		/**
		 * Returns the sending portal frame.
		 *
		 * @return the sending portal frame.
		 */
		@Override
		@Nullable
		public Frame getFrame() {
			return frame;
		}

		public Entity getEntity() {
			return entity;
		}

		@Nonnull
		public TeleportData getData() {
			return data;
		}
	}

	protected final World world;
	protected final Frame frame;

	public NetherPortalEvent(World world, Frame frame) {
		this.world = world;
		this.frame = frame;
	}

	@Nullable
	public Frame getFrame() {
		return frame;
	}

	@Nonnull
	public World getWorld() {
		return world;
	}
}
