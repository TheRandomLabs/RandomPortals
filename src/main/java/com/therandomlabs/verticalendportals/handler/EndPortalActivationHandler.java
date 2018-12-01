package com.therandomlabs.verticalendportals.handler;

import java.util.List;
import java.util.Random;
import com.therandomlabs.verticalendportals.api.event.EndPortalEvent;
import com.therandomlabs.verticalendportals.api.frame.Frame;
import com.therandomlabs.verticalendportals.api.frame.FrameType;
import com.therandomlabs.verticalendportals.block.VEPBlocks;
import com.therandomlabs.verticalendportals.frame.endportal.EndPortalFrames;
import net.minecraft.block.Block;
import net.minecraft.block.BlockEndPortalFrame;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import static net.minecraft.block.BlockHorizontal.FACING;

public final class EndPortalActivationHandler {
	private static final Random random = new Random();

	@SubscribeEvent(priority = EventPriority.LOW)
	public void onBlockActivated(PlayerInteractEvent.RightClickBlock event) {
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

		world.playSound(
				null, pos, SoundEvents.BLOCK_END_PORTAL_FRAME_FILL, SoundCategory.BLOCKS, 1.0F, 1.0F
		);

		Frame frame = null;

		if(block == Blocks.END_PORTAL_FRAME) {
			frame = EndPortalFrames.LATERAL.detect(world, pos);
		} else if(block == VEPBlocks.vertical_end_portal_frame) {
			frame = EndPortalFrames.LATERAL_WITH_VERTICAL_FRAMES.detect(world, pos);
		} else if(block == VEPBlocks.upside_down_end_portal_frame) {
			frame = EndPortalFrames.UPSIDE_DOWN.detect(world, pos);
		}

		if(frame != null && !frame.isCorner(pos)) {
			if(MinecraftForge.EVENT_BUS.post(new EndPortalEvent.Activate(frame, pos))) {
				return;
			}

			final IBlockState portalState;

			if(block == VEPBlocks.upside_down_end_portal_frame) {
				portalState = VEPBlocks.upside_down_end_portal.getDefaultState();
			} else {
				portalState = VEPBlocks.lateral_end_portal.getDefaultState();
			}

			for(BlockPos innerPos : frame.getInnerBlockPositions()) {
				world.setBlockState(innerPos, portalState, 2);
			}

			world.playBroadcastSound(1038, frame.getTopLeft().add(1, 0, 1), 0);

			event.setCancellationResult(EnumActionResult.SUCCESS);
			return;
		}

		final EnumFacing facing = state.getValue(FACING);
		EnumFacing portalFacing = null;

		if(block == VEPBlocks.vertical_end_portal_frame) {
			frame = EndPortalFrames.VERTICAL.get(facing).detect(world, pos);
			portalFacing = facing;
		}

		if(frame == null) {
			frame = EndPortalFrames.VERTICAL_INWARDS_FACING.detect(world, pos);

			if(frame != null) {
				portalFacing = frame.getType() == FrameType.VERTICAL_X ?
						EnumFacing.NORTH : EnumFacing.EAST;
			}
		}

		if(frame == null || frame.isCorner(pos)) {
			event.setCancellationResult(EnumActionResult.FAIL);
			return;
		}

		if(MinecraftForge.EVENT_BUS.post(new EndPortalEvent.Activate(frame, pos))) {
			return;
		}

		final List<BlockPos> innerBlockPositions = frame.getInnerBlockPositions();

		for(BlockPos innerPos : innerBlockPositions) {
			world.setBlockState(
					innerPos, VEPBlocks.vertical_end_portal.getDefaultState().withProperty(
							FACING, portalFacing
					), 2
			);
		}

		world.playBroadcastSound(1038, innerBlockPositions.get(0), 0);

		event.setCancellationResult(EnumActionResult.SUCCESS);
	}

	@SuppressWarnings("ConditionCoveredByFurtherCondition")
	private static boolean isFrameBlock(Block block) {
		return block == Blocks.END_PORTAL_FRAME || block == VEPBlocks.vertical_end_portal_frame ||
				block == VEPBlocks.upside_down_end_portal_frame;
	}
}
