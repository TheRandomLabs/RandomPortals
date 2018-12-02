package com.therandomlabs.verticalendportals.handler;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.therandomlabs.verticalendportals.config.NetherPortalType;
import com.therandomlabs.verticalendportals.config.NetherPortalTypes;
import net.minecraft.entity.Entity;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

@Mod.EventBusSubscriber
public final class NetherPortalTeleportHandler {
	private static final Map<WeakReference<Entity>, NetherPortalType> entities =
			new HashMap<>();

	public static void setPortalType(Entity entity, NetherPortalType type) {
		final List<WeakReference<Entity>> toRemove = new ArrayList<>();

		for(WeakReference<Entity> entityReference : entities.keySet()) {
			final Entity referencedEntity = entityReference.get();

			if(referencedEntity == null || referencedEntity == entity) {
				toRemove.add(entityReference);
			}
		}

		entities.keySet().removeAll(toRemove);
		entities.put(new WeakReference<>(entity), type);
	}

	public static NetherPortalType getNetherPortalType(Entity entity) {
		for(Map.Entry<WeakReference<Entity>, NetherPortalType> entry : entities.entrySet()) {
			if(entry.getKey().get() == entity) {
				return entry.getValue();
			}
		}

		return NetherPortalTypes.getDefault();
	}

	@SubscribeEvent
	public static void onWorldTick(TickEvent.WorldTickEvent event) {
		if(event.world.isRemote) {
			return;
		}

		for(Entity entity : event.world.getEntities(Entity.class, entity -> entity.inPortal)) {
			
		}
	}
}
