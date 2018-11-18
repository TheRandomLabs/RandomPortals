package com.therandomlabs.verticalendportals.event;

import java.util.Random;
import com.therandomlabs.verticalendportals.VerticalEndPortals;
import com.therandomlabs.verticalendportals.block.VEPBlocks;
import com.therandomlabs.verticalendportals.util.FrameDetector;
import net.minecraft.block.Block;
import net.minecraft.block.BlockEndPortalFrame;
import net.minecraft.block.state.BlockWorldState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.block.state.pattern.BlockStateMatcher;
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
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.apache.commons.lang3.ArrayUtils;
import static net.minecraft.block.BlockEndPortalFrame.EYE;
import static net.minecraft.block.BlockHorizontal.FACING;

@Mod.EventBusSubscriber(modid = VerticalEndPortals.MOD_ID)
public final class EndPortalFrameHandler {
	public static final FrameDetector LATERAL_FRAMES = new FrameDetector(
			FrameDetector.Type.LATERAL,
			3, 6, 3, 6,
			BlockWorldState.hasState(
					BlockStateMatcher.forBlock(Blocks.END_PORTAL_FRAME).
							where(EYE, eye -> eye).
							where(FACING, facing1 -> facing1 == EnumFacing.SOUTH)
			),
			BlockWorldState.hasState(
					BlockStateMatcher.forBlock(Blocks.END_PORTAL_FRAME).
							where(EYE, eye -> eye).
							where(FACING, facing1 -> facing1 == EnumFacing.WEST)
			),
			BlockWorldState.hasState(
					BlockStateMatcher.forBlock(Blocks.END_PORTAL_FRAME).
							where(EYE, eye -> eye).
							where(FACING, facing1 -> facing1 == EnumFacing.NORTH)
			),
			BlockWorldState.hasState(
					BlockStateMatcher.forBlock(Blocks.END_PORTAL_FRAME).
							where(EYE, eye -> eye).
							where(FACING, facing1 -> facing1 == EnumFacing.EAST)
			),
			BlockWorldState.hasState(BlockStateMatcher.ANY),
			potentialFrame -> true
	);

	public static final FrameDetector LATERAL_VERTICAL_FRAMES = new FrameDetector(
			FrameDetector.Type.LATERAL,
			3, 6, 3, 6,
			BlockWorldState.hasState(
					BlockStateMatcher.forBlock(VEPBlocks.vertical_end_portal_frame).
							where(EYE, eye -> eye).
							where(FACING, facing1 -> facing1 == EnumFacing.SOUTH)
			),
			BlockWorldState.hasState(
					BlockStateMatcher.forBlock(VEPBlocks.vertical_end_portal_frame).
							where(EYE, eye -> eye).
							where(FACING, facing1 -> facing1 == EnumFacing.WEST)
			),
			BlockWorldState.hasState(
					BlockStateMatcher.forBlock(VEPBlocks.vertical_end_portal_frame).
							where(EYE, eye -> eye).
							where(FACING, facing1 -> facing1 == EnumFacing.NORTH)
			),
			BlockWorldState.hasState(
					BlockStateMatcher.forBlock(VEPBlocks.vertical_end_portal_frame).
							where(EYE, eye -> eye).
							where(FACING, facing1 -> facing1 == EnumFacing.EAST)
			),
			BlockWorldState.hasState(BlockStateMatcher.ANY),
			potentialFrame -> true
	);

	public static final FrameDetector UPSIDE_DOWN_FRAMES = new FrameDetector(
			FrameDetector.Type.LATERAL,
			3, 6, 3, 6,
			BlockWorldState.hasState(
					BlockStateMatcher.forBlock(VEPBlocks.upside_down_end_portal_frame).
							where(EYE, eye -> eye).
							where(FACING, facing1 -> facing1 == EnumFacing.SOUTH)
			),
			BlockWorldState.hasState(
					BlockStateMatcher.forBlock(VEPBlocks.upside_down_end_portal_frame).
							where(EYE, eye -> eye).
							where(FACING, facing1 -> facing1 == EnumFacing.WEST)
			),
			BlockWorldState.hasState(
					BlockStateMatcher.forBlock(VEPBlocks.upside_down_end_portal_frame).
							where(EYE, eye -> eye).
							where(FACING, facing1 -> facing1 == EnumFacing.NORTH)
			),
			BlockWorldState.hasState(
					BlockStateMatcher.forBlock(VEPBlocks.upside_down_end_portal_frame).
							where(EYE, eye -> eye).
							where(FACING, facing1 -> facing1 == EnumFacing.EAST)
			),
			BlockWorldState.hasState(BlockStateMatcher.ANY),
			potentialFrame -> true
	);

	private static final Block[] frames = {
			Blocks.END_PORTAL_FRAME,
			VEPBlocks.vertical_end_portal_frame,
			VEPBlocks.upside_down_end_portal_frame
	};

	private static final Random random = new Random();

	@SubscribeEvent
	public static void onBlockActivated(PlayerInteractEvent.RightClickBlock event) {
		final World world = event.getWorld();
		final EntityPlayer player = event.getEntityPlayer();
		final ItemStack stack = event.getItemStack();
		final BlockPos pos = event.getPos();
		final IBlockState state = event.getWorld().getBlockState(pos);
		final Block block = state.getBlock();

		if(stack.getItem() != Items.ENDER_EYE || !ArrayUtils.contains(frames, block) ||
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

		FrameDetector.Frame frame = null;

		if(block == Blocks.END_PORTAL_FRAME) {
			frame = LATERAL_FRAMES.detect(world, pos);
		} else if(block == VEPBlocks.vertical_end_portal_frame) {
			frame = LATERAL_VERTICAL_FRAMES.detect(world, pos);
		} else if(block == VEPBlocks.upside_down_end_portal_frame) {
			frame = UPSIDE_DOWN_FRAMES.detect(world, pos);
		}

		if(frame != null && !frame.isCorner(pos)) {
			if(block == VEPBlocks.upside_down_end_portal_frame) {
				frame.getInnerBlocks().forEach(innerPos -> world.setBlockState(
						innerPos, VEPBlocks.upside_down_end_portal.getDefaultState(), 2
				));
			} else {
				frame.getInnerBlocks().forEach(innerPos -> world.setBlockState(
						innerPos, Blocks.END_PORTAL.getDefaultState(), 2
				));
			}

			world.playBroadcastSound(1038, frame.getTopLeft().add(1, 0, 1), 0);

			event.setCancellationResult(EnumActionResult.SUCCESS);
			return;
		}

		if(block == VEPBlocks.vertical_end_portal_frame) {

		}
	}
}
