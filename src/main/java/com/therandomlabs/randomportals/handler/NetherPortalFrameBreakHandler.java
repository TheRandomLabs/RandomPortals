package com.therandomlabs.randomportals.handler;

import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;
import com.therandomlabs.randomportals.api.config.NetherPortalType;
import com.therandomlabs.randomportals.world.storage.NetherPortalSavedData;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public final class NetherPortalFrameBreakHandler {
	private static final Map<Map.Entry<World, BlockPos>, NetherPortalType> positions =
			new HashMap<>();

	@SubscribeEvent
	public static void onBlockPlace(BlockEvent.PlaceEvent event) {
		final World world = event.getWorld();
		final BlockPos pos = event.getPos();

		if(!event.getBlockSnapshot().getReplacedBlock().getBlock().isReplaceable(world, pos) &&
				NetherPortalSavedData.get(world).getGeneratedPortalType(pos) != null) {
			event.setCanceled(true);
		}
	}

	@SubscribeEvent
	public static void onBlockBreak(BlockEvent.BreakEvent event) {
		final World world = event.getWorld();
		final BlockPos pos = event.getPos();

		positions.put(
				new AbstractMap.SimpleEntry<>(world, pos),
				NetherPortalSavedData.get(world).getGeneratedPortalType(pos)
		);
	}

	@SubscribeEvent
	public static void onBlockDrops(BlockEvent.HarvestDropsEvent event) {
		final NetherPortalType type = positions.get(new AbstractMap.SimpleEntry<>(
				event.getWorld(), event.getPos()
		));

		if(type != null && !type.doGeneratedFramesDrop) {
			event.getDrops().clear();
		}
	}

	@SubscribeEvent
	public static void onWorldTick(TickEvent.WorldTickEvent event) {
		if(event.phase != TickEvent.Phase.END) {
			return;
		}

		final NetherPortalSavedData savedData = NetherPortalSavedData.get(event.world);

		for(Map.Entry<Map.Entry<World, BlockPos>, NetherPortalType> entry :
				positions.entrySet()) {
			final NetherPortalType type = entry.getValue();

			if(type == null) {
				continue;
			}

			final Map.Entry<World, BlockPos> key = entry.getKey();

			if(key.getKey() == event.world) {
				savedData.removeGeneratedPortalFramePos(type.getName(), key.getValue());
			}
		}
	}

	@SubscribeEvent
	public static void onServerTick(TickEvent.ServerTickEvent event) {
		if(event.phase == TickEvent.Phase.END) {
			positions.clear();
		}
	}
}
