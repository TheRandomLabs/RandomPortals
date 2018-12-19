package com.therandomlabs.randomportals.handler;

import java.util.ArrayList;
import java.util.List;
import com.therandomlabs.randomportals.api.config.ActivationData;
import com.therandomlabs.randomportals.api.config.NetherPortalTypes;
import com.therandomlabs.randomportals.api.netherportal.NetherPortal;
import com.therandomlabs.randomportals.api.netherportal.NetherPortalActivator;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public final class NetherPortalActivationHandler {
	private static class DelayedActivation {
		final World world;
		final List<BlockPos> portalPositions;
		final IBlockState portalState;

		DelayedActivation(World world, List<BlockPos> portalPositions, IBlockState portalState) {
			this.world = world;
			this.portalPositions = portalPositions;
			this.portalState = portalState;
		}
	}

	private static final NetherPortalActivator PORTAL_ACTIVATOR =
			new NetherPortalActivator().setActivationDelayed(true);

	private static final List<DelayedActivation> delayedActivations = new ArrayList<>();

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

		final BlockPos portalPos = pos.offset(face);
		final NetherPortal portal = PORTAL_ACTIVATOR.activate(world, portalPos, stack);

		if(portal == null) {
			return;
		}

		event.setCanceled(true);
		event.setCancellationResult(EnumActionResult.SUCCESS);

		if(world.isRemote) {
			return;
		}

		final ActivationData activation = portal.getType().activation;
		final ActivationData.ConsumeBehavior behavior = activation.activatorConsumeBehavior;

		if(!player.capabilities.isCreativeMode) {
			if(behavior == ActivationData.ConsumeBehavior.CONSUME) {
				stack.shrink(1);
			} else if(behavior == ActivationData.ConsumeBehavior.DAMAGE) {
				stack.damageItem(1, player);
			}
		}

		final SoundEvent[] sounds = activation.getActivationSoundEvents();

		if(sounds.length != 0) {
			world.playSound(
					null,
					pos,
					sounds[world.rand.nextInt(sounds.length)],
					SoundCategory.BLOCKS,
					1.0F,
					world.rand.nextFloat() * 0.4F + 0.8F
			);
		}

		if(activation.spawnFireBeforeActivating) {
			world.setBlockState(portalPos, Blocks.FIRE.getDefaultState(), 2);
			return;
		}

		//NetherPortalActivator adds a DelayedActivation to the queue because activationDelayed
		//is true
		final DelayedActivation delayedActivation =
				delayedActivations.remove(delayedActivations.size() - 1);

		for(BlockPos portalPosition : delayedActivation.portalPositions) {
			world.setBlockState(portalPosition, delayedActivation.portalState, 2);
		}
	}

	@SubscribeEvent
	public static void onWorldTick(TickEvent.WorldTickEvent event) {
		if(event.phase == TickEvent.Phase.END) {
			return;
		}

		for(DelayedActivation delayedActivation : delayedActivations) {
			if(delayedActivation.world == event.world) {
				for(BlockPos portalPosition : delayedActivation.portalPositions) {
					event.world.setBlockState(portalPosition, delayedActivation.portalState, 2);
				}
			}
		}
	}

	@SubscribeEvent
	public static void onServerTick(TickEvent.ServerTickEvent event) {
		if(event.phase == TickEvent.Phase.END) {
			delayedActivations.clear();
		}
	}

	public static void queueDelayedActivation(World world, List<BlockPos> portalPositions,
			IBlockState portalState) {
		delayedActivations.add(new DelayedActivation(world, portalPositions, portalState));
	}

	public static boolean isDelayedActivationQueued(World world, BlockPos pos) {
		for(DelayedActivation delayedActivation : delayedActivations) {
			if(delayedActivation.world == world &&
					delayedActivation.portalPositions.contains(pos)) {
				return true;
			}
		}

		return false;
	}
}
