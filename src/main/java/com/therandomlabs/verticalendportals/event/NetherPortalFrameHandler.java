package com.therandomlabs.verticalendportals.event;

import com.therandomlabs.verticalendportals.VerticalEndPortals;
import com.therandomlabs.verticalendportals.util.FrameDetector;
import net.minecraft.block.state.BlockWorldState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Mod.EventBusSubscriber(modid = VerticalEndPortals.MOD_ID)
public final class NetherPortalFrameHandler {
	private static final FrameDetector FRAMES = new FrameDetector(
			FrameDetector.Type.LATERAL_OR_VERTICAL,
			state -> state.getBlockState().getBlock() == Blocks.OBSIDIAN,
			BlockWorldState.hasState(state -> state.getBlock() != Blocks.AIR),
			NetherPortalFrameHandler::validFrame
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

		final FrameDetector.Frame frame = FRAMES.detect(world, pos, 3, 9000, 3, 9000);

		if(frame == null) {
			event.setCancellationResult(EnumActionResult.FAIL);
			return;
		}

		for(BlockPos innerPos : frame.getInnerBlocks()) {
			world.setBlockState(innerPos, Blocks.PORTAL.getDefaultState(), 2);
		}

		event.setCancellationResult(EnumActionResult.SUCCESS);
	}

	private static boolean validFrame(FrameDetector.Frame frame) {
		final World world = frame.getWorld();

		for(BlockPos innerPos : frame.getInnerBlocks()) {
			if(world.getBlockState(innerPos).getBlock() != Blocks.AIR) {
				return false;
			}
		}

		return true;
	}
}
