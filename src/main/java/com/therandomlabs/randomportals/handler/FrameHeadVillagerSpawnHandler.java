package com.therandomlabs.randomportals.handler;

import com.therandomlabs.randomportals.RPOConfig;
import com.therandomlabs.randomportals.block.RPOBlocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingSpawnEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public final class FrameHeadVillagerSpawnHandler {
	@SubscribeEvent(priority = EventPriority.LOWEST)
	public static void onCheckSpawn(LivingSpawnEvent.CheckSpawn event) {
		final double chance = RPOConfig.endPortals.frameHeadVillagerSpawnChance;

		if(chance == 0.0) {
			return;
		}

		final Entity entity = event.getEntity();

		if(entity.getClass() != EntityVillager.class) {
			return;
		}

		final EntityVillager villager = (EntityVillager) entity;

		if(chance == 1.0 || villager.getRNG().nextDouble() <= chance) {
			villager.replaceItemInInventory(
					100 + EntityEquipmentSlot.HEAD.getIndex(),
					new ItemStack(RPOBlocks.vertical_end_portal_frame)
			);
		}
	}
}
