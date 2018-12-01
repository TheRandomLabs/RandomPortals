package com.therandomlabs.verticalendportals.block;

import java.util.List;
import com.therandomlabs.verticalendportals.VEPConfig;
import com.therandomlabs.verticalendportals.api.event.NetherPortalEvent;
import com.therandomlabs.verticalendportals.api.frame.Frame;
import com.therandomlabs.verticalendportals.frame.NetherPortalFrames;
import net.minecraft.block.BlockFire;
import net.minecraft.block.SoundType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;

public class BlockVEPFire extends BlockFire {
	private static final EnumFacing[] facings = EnumFacing.values();

	public BlockVEPFire() {
		setHardness(0.0F);
		setLightLevel(1.0F);
		setSoundType(SoundType.CLOTH);
		disableStats();
		setTranslationKey("fire");
		setRegistryName("minecraft:fire");
	}

	@Override
	public void onBlockAdded(World world, BlockPos pos, IBlockState state) {
		if(trySpawnPortal(world, pos)) {
			return;
		}

		final BlockPos down = pos.down();

		if(!world.getBlockState(down).isSideSolid(world, down, EnumFacing.UP) &&
				!canNeighborCatchFire(world, pos)) {
			world.setBlockToAir(pos);
		} else {
			world.scheduleUpdate(pos, this, tickRate(world) + world.rand.nextInt(10));
		}
	}

	private boolean trySpawnPortal(World world, BlockPos pos) {
		Frame frame = null;
		BlockPos framePos = null;

		for(EnumFacing facing : facings) {
			final BlockPos offset = pos.offset(facing);

			if(VEPConfig.netherPortalFrameBlocks.contains(world.getBlockState(offset).getBlock())) {
				frame = NetherPortalFrames.EMPTY_FRAMES.detectWithCondition(
						world, offset, potentialFrame -> potentialFrame.isFacingInwards(
								offset, facing.getOpposite()
						)
				);

				if(frame != null) {
					framePos = offset;
					break;
				}
			}
		}

		if(frame == null) {
			return false;
		}

		if(MinecraftForge.EVENT_BUS.post(new NetherPortalEvent.Activate(frame, framePos))) {
			return false;
		}

		final EnumFacing.Axis axis = frame.getType().getAxis();
		IBlockState state;

		if(axis == EnumFacing.Axis.Y) {
			state = VEPBlocks.lateral_nether_portal.getDefaultState();
		} else {
			state = VEPBlocks.vertical_nether_portal.getDefaultState().
					withProperty(BlockNetherPortal.AXIS, axis);
		}

		state = state.withProperty(BlockNetherPortal.USER_PLACED, false);

		final List<BlockPos> innerBlockPositions = frame.getInnerBlockPositions();

		for(BlockPos innerPos : innerBlockPositions) {
			world.setBlockState(innerPos, state, 2);
		}

		return true;
	}

	private boolean canNeighborCatchFire(World world, BlockPos pos) {
		for(EnumFacing facing : facings) {
			if(canCatchFire(world, pos.offset(facing), facing.getOpposite())) {
				return true;
			}
		}

		return false;
	}
}
