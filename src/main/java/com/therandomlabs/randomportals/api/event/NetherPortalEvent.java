package com.therandomlabs.randomportals.api.event;

import java.util.Collection;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import com.google.common.collect.ImmutableList;
import com.therandomlabs.randomportals.api.frame.Frame;
import com.therandomlabs.randomportals.api.netherportal.NetherPortal;
import com.therandomlabs.randomportals.api.netherportal.TeleportData;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.EnumDyeColor;
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

	public static class Add extends NetherPortalEvent {
		private final NetherPortal portal;
		private final boolean userCreated;

		public Add(World world, NetherPortal portal, boolean userCreated) {
			super(world, portal.getFrame());
			this.portal = portal;
			this.userCreated = userCreated;
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

		public boolean isUserCreated() {
			return userCreated;
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

		public static class Post extends Teleport {
			private final Frame receivingFrame;

			public Post(Entity entity, TeleportData data, Frame receivingFrame) {
				super(entity, data);
				this.receivingFrame = receivingFrame;
			}

			@Nonnull
			public Frame getReceivingFrame() {
				return receivingFrame;
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

	public static class Dye extends NetherPortalEvent {
		@Cancelable
		public static class Pre extends Dye {
			private final EntityItem dyeEntity;

			public Pre(World world, NetherPortal portal, Collection<BlockPos> dyedPortalPositions,
					EnumDyeColor oldColor, EnumDyeColor newColor, EntityItem dyeEntity) {
				super(world, portal, dyedPortalPositions, oldColor, newColor);
				this.dyeEntity = dyeEntity;
			}

			@Nonnull
			public EntityItem getDyeEntity() {
				return dyeEntity;
			}
		}

		public static class Post extends Dye {
			public Post(World world, NetherPortal portal, Collection<BlockPos> dyedPortalPositions,
					EnumDyeColor oldColor, EnumDyeColor newColor) {
				super(world, portal, dyedPortalPositions, oldColor, newColor);
			}
		}

		private final NetherPortal portal;
		private final ImmutableList<BlockPos> dyedPortalPositions;
		private final EnumDyeColor oldColor;
		private final EnumDyeColor newColor;

		protected Dye(World world, NetherPortal portal, Collection<BlockPos> dyedPortalPositions,
				EnumDyeColor oldColor, EnumDyeColor newColor) {
			super(world, portal == null ? null : portal.getFrame());
			this.portal = portal;
			this.dyedPortalPositions = ImmutableList.copyOf(dyedPortalPositions);
			this.oldColor = oldColor;
			this.newColor = newColor;
		}

		@Nullable
		public NetherPortal getPortal() {
			return portal;
		}

		@Nonnull
		public ImmutableList<BlockPos> getDyedPortalPositions() {
			return dyedPortalPositions;
		}

		@Nonnull
		public EnumDyeColor getOldColor() {
			return oldColor;
		}

		@Nonnull
		public EnumDyeColor getNewColor() {
			return newColor;
		}
	}

	public static class Remove extends NetherPortalEvent {
		private final NetherPortal portal;

		public Remove(NetherPortal portal) {
			super(portal.getWorld(), portal.getFrame());
			this.portal = portal;
		}

		@Nonnull
		@Override
		public Frame getFrame() {
			return super.getFrame();
		}

		@Nonnull
		public NetherPortal getPortal() {
			return portal;
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

	@Nullable
	public Frame getFrame() {
		return frame;
	}
}
