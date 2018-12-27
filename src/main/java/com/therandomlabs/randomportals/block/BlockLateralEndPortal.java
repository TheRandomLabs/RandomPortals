package com.therandomlabs.randomportals.block;

import java.util.function.Function;
import java.util.function.Predicate;
import com.therandomlabs.randomportals.api.config.FrameSize;
import com.therandomlabs.randomportals.api.event.EndPortalEvent;
import com.therandomlabs.randomportals.api.frame.Frame;
import com.therandomlabs.randomportals.api.frame.FrameType;
import com.therandomlabs.randomportals.api.util.FrameStatePredicate;
import com.therandomlabs.randomportals.frame.endportal.EndPortalFrames;
import com.therandomlabs.randomportals.handler.EndPortalActivationHandler;
import com.therandomlabs.randomportals.world.storage.RPOSavedData;
import net.minecraft.block.Block;
import net.minecraft.block.BlockEndPortal;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.DimensionType;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import static net.minecraft.block.BlockHorizontal.FACING;

public class BlockLateralEndPortal extends BlockEndPortal {
	public BlockLateralEndPortal() {
		this("minecraft:end_portal");
	}

	protected BlockLateralEndPortal(String registryName) {
		super(Material.PORTAL);
		setHardness(-1.0F);
		setResistance(6000000.0F);
		setCreativeTab(CreativeTabs.DECORATIONS);
		setTranslationKey("endPortalLateral");
		setRegistryName(registryName);
	}

	@Override
	public void onEntityCollision(World world, BlockPos pos, IBlockState state, Entity entity) {
		if(world.isRemote || entity.isRiding() || entity.isBeingRidden() || !entity.isNonBoss()) {
			return;
		}

		final AxisAlignedBB aabb = entity.getEntityBoundingBox();

		if(!aabb.intersects(state.getBoundingBox(world, pos).offset(pos))) {
			return;
		}

		final Frame frame = findFrame(world, pos);

		if(MinecraftForge.EVENT_BUS.post(new EndPortalEvent.Teleport.Pre(frame, entity, pos))) {
			return;
		}

		if(world.provider.getDimensionType() == DimensionType.THE_END) {
			entity.changeDimension(DimensionType.OVERWORLD.getId());
		} else {
			entity.changeDimension(DimensionType.THE_END.getId());
		}

		MinecraftForge.EVENT_BUS.post(new EndPortalEvent.Teleport.Post(frame, entity, pos));
	}

	@Override
	public ItemStack getItem(World world, BlockPos pos, IBlockState state) {
		return new ItemStack(this);
	}

	public static Frame findFrame(World world, BlockPos portalPos) {
		Frame frame = RPOSavedData.get(world).getEndPortalByInner(portalPos);

		if(frame != null) {
			return frame;
		}

		final IBlockState state = world.getBlockState(portalPos);
		final Block block = state.getBlock();

		final EnumFacing facing;
		final FrameStatePredicate portalMatcher;
		final EnumFacing frameDirection;
		final FrameType type;

		if(block == Blocks.END_PORTAL || block == RPOBlocks.upside_down_end_portal) {
			facing = null;
			portalMatcher = FrameStatePredicate.of(block);
			frameDirection = EnumFacing.NORTH;
			type = FrameType.LATERAL;
		} else {
			facing = state.getValue(FACING);
			portalMatcher = FrameStatePredicate.of(block).with(FACING, facing);
			frameDirection = EnumFacing.DOWN;
			type = facing.getAxis() == EnumFacing.Axis.X ?
					FrameType.VERTICAL_Z : FrameType.VERTICAL_X;
		}

		int maxSize = 0;

		for(Function<FrameType, FrameSize> sizeFunction : EndPortalFrames.SIZES) {
			final int size = sizeFunction.apply(type).getMaxSize(frameDirection == EnumFacing.DOWN);

			if(size > maxSize) {
				maxSize = size;
			}
		}

		BlockPos framePos = null;
		Block frameBlock = null;
		BlockPos checkPos = portalPos;

		for(int offset = 1; offset < maxSize - 1; offset++) {
			checkPos = checkPos.offset(frameDirection);

			final IBlockState checkState = world.getBlockState(checkPos);
			frameBlock = checkState.getBlock();

			if(EndPortalActivationHandler.isFrameBlock(frameBlock)) {
				framePos = checkPos;
				break;
			}

			if(!portalMatcher.test(world, checkPos, checkState)) {
				break;
			}
		}

		if(framePos == null) {
			return null;
		}

		final Predicate<Frame> condition =
				potentialFrame -> potentialFrame.getInnerBlockPositions().contains(portalPos);

		if(frameBlock == Blocks.END_PORTAL_FRAME) {
			frame = EndPortalFrames.LATERAL.detectWithCondition(world, framePos, condition);
		} else if(frameBlock == RPOBlocks.vertical_end_portal_frame) {
			frame = EndPortalFrames.LATERAL_WITH_VERTICAL_FRAMES.detectWithCondition(
					world, framePos, condition
			);
		} else {
			frame = EndPortalFrames.UPSIDE_DOWN.detectWithCondition(world, framePos, condition);
		}

		if(frame != null) {
			return frame;
		}

		if(frameBlock == RPOBlocks.vertical_end_portal_frame) {
			frame = EndPortalFrames.VERTICAL.get(facing).detectWithCondition(
					world, framePos, condition
			);

			if(frame != null) {
				return frame;
			}
		}

		return EndPortalFrames.VERTICAL_INWARDS_FACING.detectWithCondition(
				world, framePos, condition
		);
	}
}
