package com.therandomlabs.randomportals.handler;

import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;
import com.therandomlabs.randomportals.api.config.PortalType;
import com.therandomlabs.randomportals.world.storage.RPOSavedData;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public final class NetherPortalFrameBreakHandler {
	private static final Map<Map.Entry<World, BlockPos>, PortalType> positions =
			new HashMap<>();

	@SubscribeEvent
	public static void onBlockPlace(BlockEvent.PlaceEvent event) {
		final World world = event.getWorld();
		final BlockPos pos = event.getPos();

		//Exchangers compatibility
		//If the replaced block is not replaceable (i.e. not air, snow or similar blocks),
		//then it can be assumed that this event was posted by Exchangers or a similar mod
		if(!event.getBlockSnapshot().getReplacedBlock().getMaterial().isReplaceable() &&
				RPOSavedData.get(world).getGeneratedNetherPortalType(pos) != null) {
			event.setCanceled(true);
		}
	}

	@SubscribeEvent
	public static void onBlockBreak(BlockEvent.BreakEvent event) {
		final World world = event.getWorld();
		final BlockPos pos = event.getPos();

		positions.put(
				new AbstractMap.SimpleEntry<>(world, pos),
				RPOSavedData.get(world).getGeneratedNetherPortalType(pos)
		);
	}

	@SubscribeEvent
	public static void onBlockDrops(BlockEvent.HarvestDropsEvent event) {
		final PortalType type = positions.get(new AbstractMap.SimpleEntry<>(
				event.getWorld(), event.getPos()
		));

		if(type != null && !type.frame.doGeneratedFramesDrop) {
			event.getDrops().clear();
		}
	}

	@SubscribeEvent
	public static void onWorldTick(TickEvent.WorldTickEvent event) {
		if(event.phase != TickEvent.Phase.END) {
			return;
		}

		final RPOSavedData savedData = RPOSavedData.get(event.world);

		for(Map.Entry<Map.Entry<World, BlockPos>, PortalType> entry : positions.entrySet()) {
			final PortalType type = entry.getValue();

			if(type == null) {
				continue;
			}

			final Map.Entry<World, BlockPos> key = entry.getKey();

			if(key.getKey() == event.world) {
				savedData.removeGeneratedNetherPortalFramePos(type.toString(), key.getValue());
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
