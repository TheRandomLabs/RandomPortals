package com.therandomlabs.verticalendportals.api.event;

import com.therandomlabs.verticalendportals.api.frame.Frame;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.eventhandler.Event;

public class EndPortalEvent extends Event {
	public static class Activate extends EndPortalEvent {
		private final BlockPos activatedFrameBlock;

		public Activate(Frame frame, BlockPos activatedFrameBlock) {
			super(frame);
			this.activatedFrameBlock = activatedFrameBlock;
		}

		public BlockPos getActivatedFrameBlock() {
			return activatedFrameBlock;
		}
	}

	public static class Teleport extends EndPortalEvent {
		private final Entity entity;
		private final BlockPos portalPos;

		public Teleport(Frame frame, Entity entity, BlockPos portalPos) {
			super(frame);
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

	private final Frame frame;

	public EndPortalEvent(Frame frame) {
		this.frame = frame;
	}

	@Override
	public boolean isCancelable() {
		return true;
	}

	public Frame getFrame() {
		return frame;
	}
}
