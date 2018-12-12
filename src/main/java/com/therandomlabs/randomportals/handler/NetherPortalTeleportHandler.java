package com.therandomlabs.randomportals.handler;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.therandomlabs.randomportals.api.config.NetherPortalType;
import com.therandomlabs.randomportals.api.event.NetherPortalEvent;
import com.therandomlabs.randomportals.api.netherportal.NetherPortal;
import com.therandomlabs.randomportals.api.netherportal.TeleportData;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public final class NetherPortalTeleportHandler {
	private static final Map<WeakReference<Entity>, TeleportData> preTeleportData = new HashMap<>();
	private static final Map<WeakReference<Entity>, TeleportData> teleportData = new HashMap<>();

	public static void setPortal(Entity entity, NetherPortal portal, BlockPos pos) {
		final World world = entity.getEntityWorld();

		if(!world.getMinecraftServer().getAllowNether()) {
			return;
		}

		if(entity.timeUntilPortal > 0) {
			entity.timeUntilPortal = entity.getPortalCooldown();
			return;
		}

		WeakReference<Entity> reference = null;
		boolean found = false;

		for(Map.Entry<WeakReference<Entity>, TeleportData> entry : preTeleportData.entrySet()) {
			reference = entry.getKey();

			if(reference.get() == entity) {
				found = true;
				break;
			}
		}

		if(!found) {
			reference = new WeakReference<>(entity);
		}

		preTeleportData.put(reference, new TeleportData(portal, pos));

		//In case another mod needs lastPortalPos for whatever reason
		entity.lastPortalPos = pos;
	}

	public static TeleportData getTeleportData(Entity entity) {
		for(Map.Entry<WeakReference<Entity>, TeleportData> entry : preTeleportData.entrySet()) {
			if(entry.getKey().get() == entity) {
				return entry.getValue();
			}
		}

		final List<WeakReference<Entity>> toRemove = new ArrayList<>();
		TeleportData data = null;

		for(Map.Entry<WeakReference<Entity>, TeleportData> entry : teleportData.entrySet()) {
			final WeakReference<Entity> entityReference = entry.getKey();
			final Entity referencedEntity = entityReference.get();

			if(referencedEntity == null) {
				toRemove.add(entityReference);
			} else if(referencedEntity == entity) {
				data = entry.getValue();
			}
		}

		teleportData.keySet().removeAll(toRemove);
		return data;
	}

	public static void clearTeleportData(Entity entity) {
		final List<WeakReference<Entity>> toRemove = new ArrayList<>();

		for(Map.Entry<WeakReference<Entity>, TeleportData> entry : teleportData.entrySet()) {
			final WeakReference<Entity> entityReference = entry.getKey();
			final Entity referencedEntity = entityReference.get();

			if(referencedEntity == null || referencedEntity == entity) {
				toRemove.add(entityReference);
			}
		}

		teleportData.keySet().removeAll(toRemove);
	}

	@SubscribeEvent
	public static void onServerTick(TickEvent.ServerTickEvent event) {
		if(event.phase != TickEvent.Phase.END) {
			return;
		}

		for(Map.Entry<WeakReference<Entity>, TeleportData> entry : preTeleportData.entrySet()) {
			final WeakReference<Entity> reference = entry.getKey();
			final Entity entity = entry.getKey().get();

			if(entity != null) {
				handle(
						reference, entity, entry.getValue(),
						entity.getEntityWorld().provider.getDimension()
				);
			}
		}

		preTeleportData.clear();
	}

	private static void handle(WeakReference<Entity> reference, Entity entity, TeleportData data,
			int dimension) {
		if(entity.isRiding()) {
			return;
		}

		final int maxInPortalTime = entity.getMaxInPortalTime();

		if(entity.portalCounter++ < maxInPortalTime) {
			//Entity decrements this by 4 every tick because inPortal is false
			entity.portalCounter += 4;
			return;
		}

		entity.portalCounter += 4;

		entity.portalCounter = maxInPortalTime;
		entity.timeUntilPortal = entity.getPortalCooldown();

		if(MinecraftForge.EVENT_BUS.post(new NetherPortalEvent.Teleport.Pre(entity, data))) {
			return;
		}

		teleportData.put(reference, data);
		final NetherPortalType type = data.getPortalType();
		entity.changeDimension(dimension == type.dimensionID ? 0 : type.dimensionID);
	}
}
