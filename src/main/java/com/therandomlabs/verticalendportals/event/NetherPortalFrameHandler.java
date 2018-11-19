package com.therandomlabs.verticalendportals.event;

import com.therandomlabs.verticalendportals.VerticalEndPortals;
import com.therandomlabs.verticalendportals.block.BlockNetherPortal;
import com.therandomlabs.verticalendportals.block.VEPBlocks;
import com.therandomlabs.verticalendportals.frame.BasicFrameDetector;
import com.therandomlabs.verticalendportals.frame.Frame;
import com.therandomlabs.verticalendportals.frame.FrameDetector;
import com.therandomlabs.verticalendportals.frame.RequiredCorner;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Mod.EventBusSubscriber(modid = VerticalEndPortals.MOD_ID)
public final class NetherPortalFrameHandler {
	public static final FrameDetector FRAMES = new BasicFrameDetector(
			Blocks.OBSIDIAN,
			RequiredCorner.ANY_NON_AIR,
			Frame::isEmpty
	);

	@SuppressWarnings("Duplicates")
	@SubscribeEvent(priority = EventPriority.LOW)
	public static void onBlockActivated(PlayerInteractEvent.RightClickBlock event) {
		final World world = event.getWorld();
		final EntityPlayer player = event.getEntityPlayer();
		final ItemStack stack = event.getItemStack();
		final BlockPos pos = event.getPos();

		if(stack.getItem() != Items.FLINT_AND_STEEL ||
				event.getWorld().getBlockState(pos).getBlock() != Blocks.OBSIDIAN ||
				!player.canPlayerEdit(pos, event.getFace(), stack)) {
			return;
		}

		event.setCanceled(true);

		if(world.isRemote) {
			event.setCancellationResult(EnumActionResult.SUCCESS);
			return;
		}

		world.updateComparatorOutputLevel(pos, Blocks.OBSIDIAN);
		stack.damageItem(1, player);

		final Frame frame = FRAMES.detect(
				world, pos, 3, 9000, 3, 9000,
				potentialFrame -> potentialFrame.isFacingInwards(pos, event.getFace())
		);

		if(frame == null) {
			event.setCancellationResult(EnumActionResult.FAIL);
			return;
		}

		final EnumFacing.Axis axis = frame.getType().getAxis();
		final IBlockState state;

		if(axis == EnumFacing.Axis.Y) {
			state = VEPBlocks.lateral_nether_portal.getDefaultState();
		} else {
			state = Blocks.PORTAL.getDefaultState().withProperty(BlockNetherPortal.AXIS, axis);
		}

		for(BlockPos innerPos : frame.getInnerBlockPositions()) {
			world.setBlockState(innerPos, state, 2);
		}

		event.setCancellationResult(EnumActionResult.SUCCESS);
	}
}
