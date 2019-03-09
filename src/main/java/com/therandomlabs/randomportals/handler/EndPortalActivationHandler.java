package com.therandomlabs.randomportals.handler;

import java.util.Random;
import com.therandomlabs.randomportals.config.RPOConfig;
import com.therandomlabs.randomportals.advancements.RPOCriteriaTriggers;
import com.therandomlabs.randomportals.block.RPOBlocks;
import com.therandomlabs.randomportals.frame.endportal.EndPortalFrames;
import net.minecraft.block.Block;
import net.minecraft.block.BlockEndPortalFrame;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public final class EndPortalActivationHandler {
	private static final Random random = new Random();

	@SubscribeEvent
	public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
		final World world = event.getWorld();
		final EntityPlayer player = event.getEntityPlayer();
		final ItemStack stack = event.getItemStack();
		final BlockPos pos = event.getPos();
		final IBlockState state = world.getBlockState(pos);
		final Block block = state.getBlock();

		if(stack.getItem() != Items.ENDER_EYE || !isFrameBlock(block) ||
				!player.canPlayerEdit(pos, event.getFace(), stack) ||
				state.getValue(BlockEndPortalFrame.EYE)) {
			return;
		}

		event.setCanceled(true);

		if(world.isRemote) {
			event.setCancellationResult(EnumActionResult.SUCCESS);
			return;
		}

		world.setBlockState(pos, state.withProperty(BlockEndPortalFrame.EYE, true), 2);
		world.updateComparatorOutputLevel(pos, block);

		if(!player.capabilities.isCreativeMode) {
			stack.shrink(1);
		}

		for(int i = 0; i < 16; i++) {
			world.spawnParticle(
					EnumParticleTypes.SMOKE_NORMAL,
					pos.getX() + (5.0 + random.nextDouble() * 6.0) / 16.0,
					pos.getY() + 0.8125,
					pos.getZ() + (5.0 + random.nextDouble() * 6.0) / 16.0,
					0.0,
					0.0,
					0.0
			);
		}

		if(EndPortalFrames.activate(world, pos) == null) {
			event.setCancellationResult(EnumActionResult.FAIL);
		} else {
			if(RPOConfig.Misc.advancements) {
				RPOCriteriaTriggers.PORTALS.trigger((EntityPlayerMP) player);
			}

			event.setCancellationResult(EnumActionResult.SUCCESS);
		}
	}

	@SuppressWarnings("ConditionCoveredByFurtherCondition")
	public static boolean isFrameBlock(Block block) {
		return block == Blocks.END_PORTAL_FRAME || block == RPOBlocks.vertical_end_portal_frame ||
				block == RPOBlocks.upside_down_end_portal_frame;
	}
}
