package com.therandomlabs.randomportals.handler;

import com.therandomlabs.randomportals.api.config.NetherPortalType;
import com.therandomlabs.randomportals.api.config.NetherPortalTypes;
import com.therandomlabs.randomportals.api.netherportal.NetherPortal;
import com.therandomlabs.randomportals.api.netherportal.NetherPortalActivator;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public final class NetherPortalActivationHandler {
	private static final NetherPortalActivator PORTAL_ACTIVATOR = new NetherPortalActivator();

	@SubscribeEvent
	public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
		final World world = event.getWorld();
		final ItemStack stack = event.getItemStack();
		final BlockPos pos = event.getPos();
		final EntityPlayer player = event.getEntityPlayer();
		final EnumFacing face = event.getFace();

		if(!NetherPortalTypes.getValidActivators().test(stack) ||
				!NetherPortalTypes.getValidBlocks().test(world, pos, world.getBlockState(pos)) ||
				!player.canPlayerEdit(pos, face, stack)) {
			return;
		}

		final NetherPortal portal = PORTAL_ACTIVATOR.activate(world, pos.offset(face), stack);

		if(portal == null) {
			return;
		}

		event.setCanceled(true);
		event.setCancellationResult(EnumActionResult.SUCCESS);

		if(world.isRemote || player.capabilities.isCreativeMode) {
			return;
		}

		final NetherPortalType.ConsumeBehavior behavior = portal.getType().activatorConsumeBehavior;

		if(behavior == NetherPortalType.ConsumeBehavior.CONSUME) {
			stack.shrink(1);
		} else if(behavior == NetherPortalType.ConsumeBehavior.DAMAGE) {
			stack.damageItem(1, player);
		}
	}
}
