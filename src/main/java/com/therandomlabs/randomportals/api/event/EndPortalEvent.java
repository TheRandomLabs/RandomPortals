package com.therandomlabs.randomportals.api.event;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import com.therandomlabs.randomportals.api.frame.Frame;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.eventhandler.Cancelable;
import net.minecraftforge.fml.common.eventhandler.Event;

public class EndPortalEvent extends Event {
	@Cancelable
	public static class Activate extends EndPortalEvent {
		private final BlockPos activatedFramePos;

		public Activate(World world, Frame frame, BlockPos activatedFramePos) {
			super(world, frame);
			this.activatedFramePos = activatedFramePos;
		}

		@Nonnull
		@Override
		public Frame getFrame() {
			return frame;
		}

		public BlockPos getActivatedFramePos() {
			return activatedFramePos;
		}
	}

	public static class Add extends EndPortalEvent {
		public Add(World world, Frame frame) {
			super(world, frame);
		}

		@Nonnull
		@Override
		public Frame getFrame() {
			return frame;
		}
	}

	public static class Teleport extends EndPortalEvent {
		@Cancelable
		public static class Pre extends Teleport {
			public Pre(Frame frame, Entity entity, BlockPos portalPos) {
				super(frame, entity, portalPos);
			}
		}

		public static class Post extends Teleport {
			public Post(Frame frame, Entity entity, BlockPos portalPos) {
				super(frame, entity, portalPos);
			}
		}

		private final Entity entity;
		private final BlockPos portalPos;

		protected Teleport(Frame frame, Entity entity, BlockPos portalPos) {
			super(entity.getEntityWorld(), frame);
			this.entity = entity;
			this.portalPos = portalPos;
		}

		public Entity getEntity() {
			return entity;
		}

		public BlockPos getPortalPos() {
			return portalPos;
		}
	}

	public static class Remove extends EndPortalEvent {
		public Remove(Frame frame) {
			super(frame.getWorld(), frame);
		}

		@Nonnull
		@Override
		public Frame getFrame() {
			return super.getFrame();
		}
	}

	protected final World world;
	protected final Frame frame;

	public EndPortalEvent(World world, Frame frame) {
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
