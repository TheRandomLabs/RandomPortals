package com.therandomlabs.randomportals.handler;

import com.therandomlabs.randomportals.RPOConfig;
import com.therandomlabs.randomportals.block.RPOBlocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumActionResult;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public final class FrameHeadVillagerHandler {
	@SubscribeEvent(priority = EventPriority.LOWEST)
	public static void onCheckSpawn(EntityJoinWorldEvent event) {
		if(event.getWorld().isRemote) {
			return;
		}

		final double chance = RPOConfig.endPortals.frameHeadVillagerSpawnChance;

		if(chance == 0.0) {
			return;
		}

		final Entity entity = event.getEntity();

		if(entity.getClass() != EntityVillager.class) {
			return;
		}

		final EntityVillager villager = (EntityVillager) entity;
		final NBTTagCompound data = villager.getEntityData();

		if(data.getBoolean("FrameHeadChecked")) {
			return;
		}

		data.setBoolean("FrameHeadChecked", true);

		if(chance == 1.0 || villager.getRNG().nextDouble() <= chance) {
			villager.setItemStackToSlot(
					EntityEquipmentSlot.HEAD,
					new ItemStack(RPOBlocks.vertical_end_portal_frame)
			);
		}
	}

	@SubscribeEvent
	public static void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
		final ItemStack stack = event.getItemStack();

		if(stack.getItem() != ItemBlock.getItemFromBlock(RPOBlocks.vertical_end_portal_frame)) {
			return;
		}

		final Entity entity = event.getEntity();

		if(entity.getClass() != EntityVillager.class) {
			return;
		}

		final EntityVillager villager = (EntityVillager) entity;

		if(!villager.getItemStackFromSlot(EntityEquipmentSlot.HEAD).isEmpty()) {
			return;
		}

		villager.setItemStackToSlot(
				EntityEquipmentSlot.HEAD,
				new ItemStack(RPOBlocks.vertical_end_portal_frame)
		);

		if(!event.getEntityPlayer().capabilities.isCreativeMode) {
			stack.shrink(1);
		}

		event.setCanceled(true);
		event.setCancellationResult(EnumActionResult.SUCCESS);
	}
}
