package com.therandomlabs.verticalendportals.block;

import com.therandomlabs.verticalendportals.frame.NetherPortalFrames;
import net.minecraft.block.BlockFire;
import net.minecraft.block.SoundType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class BlockVEPFire extends BlockFire {
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
		if(NetherPortalFrames.trySpawn(world, pos, null, true)) {
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

	private boolean canNeighborCatchFire(World world, BlockPos pos) {
		for(EnumFacing facing : EnumFacing.values()) {
			if(canCatchFire(world, pos.offset(facing), facing.getOpposite())) {
				return true;
			}
		}

		return false;
	}
}
