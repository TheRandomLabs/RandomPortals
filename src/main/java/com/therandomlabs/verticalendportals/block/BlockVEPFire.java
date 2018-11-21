package com.therandomlabs.verticalendportals.block;

import java.util.List;
import com.therandomlabs.verticalendportals.frame.BasicFrameDetector;
import com.therandomlabs.verticalendportals.frame.Frame;
import com.therandomlabs.verticalendportals.frame.FrameDetector;
import com.therandomlabs.verticalendportals.frame.RequiredCorner;
import net.minecraft.block.BlockFire;
import net.minecraft.block.SoundType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class BlockVEPFire extends BlockFire {
	public static final FrameDetector NETHER_PORTAL_FRAMES = new BasicFrameDetector(
			Blocks.OBSIDIAN,
			RequiredCorner.ANY_NON_AIR,
			Frame::isEmpty
	);

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

		for(EnumFacing facing : facings) {
			final BlockPos offset = pos.offset(facing);

			if(world.getBlockState(offset).getBlock() == Blocks.OBSIDIAN) {
				frame = NETHER_PORTAL_FRAMES.detect(
						world, offset, 3, 9000, 3, 9000,
						potentialFrame -> potentialFrame.isFacingInwards(
								offset, facing.getOpposite()
						)
				);

				if(frame != null) {
					break;
				}
			}
		}

		if(frame == null) {
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

		state = state.withProperty(BlockNetherPortal.MANUALLY_PLACED, false);

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
