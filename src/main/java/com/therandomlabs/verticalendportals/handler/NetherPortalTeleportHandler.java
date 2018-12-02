package com.therandomlabs.verticalendportals.handler;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;
import com.therandomlabs.verticalendportals.config.NetherPortalType;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

@Mod.EventBusSubscriber
public final class NetherPortalTeleportHandler {
	private static final Map<WeakReference<Entity>, TeleportData> entities =
			new HashMap<>();

	public static class TeleportData {
		private NetherPortalType type;

		private TeleportData(NetherPortalType type) {
			this.type = type;
		}

		public NetherPortalType getType() {
			return type;
		}
	}

	public static void setPortal(Entity entity, BlockPos pos, NetherPortalType type) {
		if(entity.timeUntilPortal > 0) {
			entity.timeUntilPortal = entity.getPortalCooldown();
			return;
		}

		boolean found = false;

		for(Map.Entry<WeakReference<Entity>, TeleportData> entityReference : entities.entrySet()) {
			final Entity referencedEntity = entityReference.getKey().get();

			if(referencedEntity == entity) {
				entityReference.getValue().type = type;
				found = true;
				break;
			}
		}

		entity.lastPortalPos = pos;

		if(!found) {
			entities.put(new WeakReference<>(entity), new TeleportData(type));
		}
	}

	public static TeleportData getTeleportData(Entity entity) {
		for(Map.Entry<WeakReference<Entity>, TeleportData> entry : entities.entrySet()) {
			if(entry.getKey().get() == entity) {
				return entry.getValue();
			}
		}

		return null;
	}

	@SubscribeEvent
	public static void onWorldTick(TickEvent.WorldTickEvent event) {
		if(event.world.isRemote || !event.world.getMinecraftServer().getAllowNether()) {
			return;
		}

		for(Map.Entry<WeakReference<Entity>, TeleportData> entry : entities.entrySet()) {
			final Entity entity = entry.getKey().get();

			if(entity != null) {
				handle(entity, entry.getValue());
			}
		}

		entities.clear();
	}

	private static void handle(Entity entity, TeleportData data) {
		if(entity.isRiding()) {
			return;
		}

		final int maxInPortalTime = entity.getMaxInPortalTime();

		if(entity.portalCounter++ < maxInPortalTime) {
			return;
		}

		entity.portalCounter = maxInPortalTime;
		entity.timeUntilPortal = entity.getPortalCooldown();

		final int dimension = entity.getEntityWorld().provider.getDimension();
		entity.changeDimension(dimension == data.type.dimensionID ? 0 : data.type.dimensionID);
	}
}
