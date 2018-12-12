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
		public Frame getFrame() {
			return frame;
		}

		public BlockPos getActivatedFramePos() {
			return activatedFramePos;
		}
	}

	@Cancelable
	public static class Teleport extends EndPortalEvent {
		private final Entity entity;
		private final BlockPos portalPos;

		public Teleport(Frame frame, Entity entity, BlockPos portalPos) {
			super(entity.getEntityWorld(), frame);
			this.entity = entity;
			this.portalPos = portalPos;
		}

		@Nullable
		public Frame getFrame() {
			return frame;
		}

		public Entity getEntity() {
			return entity;
		}

		public BlockPos getPortalPos() {
			return portalPos;
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
}
