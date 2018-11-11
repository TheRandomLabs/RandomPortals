package com.therandomlabs.verticalendportals.block;

import java.util.List;
import java.util.Random;
import com.therandomlabs.verticalendportals.util.BlockPattern;
import com.therandomlabs.verticalendportals.util.BlockPatternFactory;
import net.minecraft.block.BlockEndPortalFrame;
import net.minecraft.block.SoundType;
import net.minecraft.block.state.BlockWorldState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.block.state.pattern.BlockStateMatcher;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BlockUpsideDownEndPortalFrame extends BlockEndPortalFrame {
	private static final AxisAlignedBB AABB_BLOCK = new AxisAlignedBB(
			0.0, 0.1875, 0.0, 1.0, 1.0, 1.0
	);

	private static final AxisAlignedBB AABB_EYE = new AxisAlignedBB(
			0.3125, 0, 0.3125, 0.6875, 0.1875, 0.6875
	);

	private static BlockPattern portalShape;

	private final Random random = new Random();

	public BlockUpsideDownEndPortalFrame() {
		setSoundType(SoundType.GLASS);
		setLightLevel(0.125F);
		setHardness(-1.0F);
		setResistance(6000000.0F);
		setCreativeTab(CreativeTabs.DECORATIONS);
		setTranslationKey("endPortalFrameUpsideDown");
		setRegistryName("upside_down_end_portal_frame");
	}

	@Override
	public boolean canEntityDestroy(IBlockState state, IBlockAccess world, BlockPos pos,
			Entity entity) {
		return false;
	}

	@SuppressWarnings("deprecation")
	@Override
	public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess world, BlockPos pos) {
		return AABB_BLOCK;
	}

	@SuppressWarnings("deprecation")
	@Override
	public void addCollisionBoxToList(IBlockState state, World world, BlockPos pos,
			AxisAlignedBB entityBox, List<AxisAlignedBB> collidingBoxes, Entity entity,
			boolean isActualState) {
		addCollisionBoxToList(pos, entityBox, collidingBoxes, AABB_BLOCK);

		if(state.getValue(EYE)) {
			addCollisionBoxToList(pos, entityBox, collidingBoxes, AABB_EYE);
		}
	}

	@SuppressWarnings("Duplicates")
	@Override
	public boolean onBlockActivated(World world, BlockPos pos, IBlockState state,
			EntityPlayer player, EnumHand hand, EnumFacing facing, float hitX, float hitY,
			float hitZ) {
		final ItemStack stack = player.getHeldItem(hand);

		if(stack.getItem() != Items.ENDER_EYE || state.getBlock() != this ||
				!player.canPlayerEdit(pos.offset(facing), facing, stack) || state.getValue(EYE)) {
			return false;
		}

		if(world.isRemote) {
			return true;
		}

		world.setBlockState(pos, state.withProperty(EYE, true), 2);
		world.updateComparatorOutputLevel(pos, this);

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

		final BlockPattern.PatternHelper patternHelper = getPortalShape().match(world, pos);

		if(patternHelper == null) {
			return false;
		}

		final BlockPos portalPos = patternHelper.getFrontTopLeft().add(-3, 0, -3);

		for(int i = 0; i < 3; i++) {
			for(int j = 0; j < 3; j++) {
				world.setBlockState(
						portalPos.add(i, 0, j), VEPBlocks.upside_down_end_portal.getDefaultState(), 2
				);
			}
		}

		world.playBroadcastSound(1038, portalPos.add(1, 0, 1), 0);

		return true;
	}

	public static BlockPattern getPortalShape() {
		if(portalShape == null) {
			portalShape = BlockPatternFactory.start().
					aisle("?vvv?", ">???<", ">???<", ">???<", "?^^^?").
					where('?', BlockWorldState.hasState(BlockStateMatcher.ANY)).
					where('^', BlockWorldState.hasState(
							BlockStateMatcher.forBlock(VEPBlocks.upside_down_end_portal_frame).
									where(EYE, eye -> eye).
									where(FACING, facing -> facing == EnumFacing.SOUTH)
					)).
					where('>', BlockWorldState.hasState(
							BlockStateMatcher.forBlock(VEPBlocks.upside_down_end_portal_frame).
									where(EYE, eye -> eye).
									where(FACING, facing -> facing == EnumFacing.WEST)
					)).
					where('v', BlockWorldState.hasState(
							BlockStateMatcher.forBlock(VEPBlocks.upside_down_end_portal_frame).
									where(EYE, eye -> eye).
									where(FACING, facing -> facing == EnumFacing.NORTH)
					)).
					where('<', BlockWorldState.hasState(
							BlockStateMatcher.forBlock(VEPBlocks.upside_down_end_portal_frame).
									where(EYE, eye -> eye).
									where(FACING, facing -> facing == EnumFacing.EAST)
					)).
					build();
		}

		return portalShape;
	}
}
