package com.therandomlabs.randomportals.handler;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;
import com.therandomlabs.randomportals.api.config.PortalType;
import com.therandomlabs.randomportals.api.config.PortalTypes;
import com.therandomlabs.randomportals.api.event.NetherPortalEvent;
import com.therandomlabs.randomportals.api.netherportal.FunctionType;
import com.therandomlabs.randomportals.api.netherportal.NetherPortal;
import com.therandomlabs.randomportals.api.netherportal.TeleportData;
import com.therandomlabs.randomportals.block.BlockNetherPortal;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.DimensionType;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public final class NetherPortalTeleportHandler {
	private static final Map<WeakReference<Entity>, TeleportData> preTeleportData = new HashMap<>();
	private static final Map<WeakReference<Entity>, TeleportData> teleportData = new HashMap<>();

	public static void setPortal(Entity entity, @Nullable NetherPortal portal, BlockPos pos) {
		final World world = entity.getEntityWorld();

		if(portal != null && portal.getFunctionType() != FunctionType.NORMAL) {
			return;
		}

		if(!world.getMinecraftServer().getAllowNether()) {
			final PortalType type =
					portal == null ? PortalTypes.getDefault(world) : portal.getType();

			if(type.destination.dimensionID == DimensionType.NETHER.getId()) {
				return;
			}
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

		preTeleportData.put(
				reference, new TeleportData(world, pos, world.getBlockState(pos), portal)
		);

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
			int dimensionID) {
		if(entity.isRiding()) {
			return;
		}

		final PortalType type = data.getPortalType();
		final IBlockState state = data.getPortalState();
		final Block block = state.getBlock();

		final int maxInPortalTime;

		if(block instanceof BlockNetherPortal) {
			maxInPortalTime = type.teleportationDelay.get(
					((BlockNetherPortal) block).getEffectiveAxis(state)
			);
		} else {
			maxInPortalTime = entity.getMaxInPortalTime();
		}

		if(entity.portalCounter++ < maxInPortalTime) {
			//Entity decrements this by 4 every tick because inPortal is false
			entity.portalCounter += 4;
			return;
		}

		entity.portalCounter = maxInPortalTime;
		entity.timeUntilPortal = entity.getPortalCooldown();

		if(MinecraftForge.EVENT_BUS.post(new NetherPortalEvent.Teleport.Pre(entity, data))) {
			return;
		}

		teleportData.put(reference, data);
		entity.changeDimension(type.getDestinationDimensionID(dimensionID));
	}
}
