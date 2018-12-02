package com.therandomlabs.verticalendportals.handler;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;
import com.therandomlabs.verticalendportals.VerticalEndPortals;
import com.therandomlabs.verticalendportals.api.event.NetherPortalEvent;
import com.therandomlabs.verticalendportals.config.NetherPortalType;
import com.therandomlabs.verticalendportals.config.NetherPortalTypes;
import com.therandomlabs.verticalendportals.world.storage.NetherPortalSavedData;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

@Mod.EventBusSubscriber
public final class NetherPortalTeleportHandler {
	private static final Map<WeakReference<Entity>, TeleportData> entities =
			new HashMap<>();

	public static class TeleportData {
		private NetherPortalSavedData.Portal portal;
		private BlockPos pos;

		private TeleportData(NetherPortalSavedData.Portal portal, BlockPos pos) {
			this.portal = portal;
			this.pos = pos;
		}

		public NetherPortalSavedData.Portal getPortal() {
			return portal;
		}

		public NetherPortalType getPortalType() {
			return portal == null ? NetherPortalTypes.getDefault() : portal.getType();
		}

		public BlockPos getPos() {
			return pos;
		}
	}

	public static void setPortal(Entity entity, NetherPortalSavedData.Portal portal, BlockPos pos) {
		if(!entity.getEntityWorld().getMinecraftServer().getAllowNether()) {
			return;
		}

		VerticalEndPortals.LOGGER.error(entity.timeUntilPortal);

		if(entity.timeUntilPortal > 0) {
			entity.timeUntilPortal = entity.getPortalCooldown();
			return;
		}

		boolean found = false;

		for(Map.Entry<WeakReference<Entity>, TeleportData> entry : entities.entrySet()) {
			final Entity referencedEntity = entry.getKey().get();

			if(referencedEntity == entity) {
				final TeleportData data = entry.getValue();

				data.portal = portal;
				data.pos = pos;

				found = true;
				break;
			}
		}

		entity.lastPortalPos = pos;

		if(!found) {
			entities.put(new WeakReference<>(entity), new TeleportData(portal, pos));
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
		if(event.world.isRemote) {
			return;
		}

		for(Map.Entry<WeakReference<Entity>, TeleportData> entry : entities.entrySet()) {
			final Entity entity = entry.getKey().get();

			if(entity != null) {
				handle(entity, entry.getValue(), event.world.provider.getDimension());
			}
		}

		entities.clear();
	}

	private static void handle(Entity entity, TeleportData data, int dimension) {
		if(entity.isRiding()) {
			return;
		}

		final int maxInPortalTime = entity.getMaxInPortalTime();

		if(entity.portalCounter++ < maxInPortalTime) {
			return;
		}

		entity.portalCounter = maxInPortalTime;
		entity.timeUntilPortal = entity.getPortalCooldown();

		final NetherPortalEvent.Teleport event = new NetherPortalEvent.Teleport(
				data.portal.getFrame(), entity, data.pos
		);

		if(MinecraftForge.EVENT_BUS.post(event)) {
			return;
		}

		final NetherPortalType type = data.getPortalType();
		entity.changeDimension(dimension == type.dimensionID ? 0 : type.dimensionID);
	}
}
