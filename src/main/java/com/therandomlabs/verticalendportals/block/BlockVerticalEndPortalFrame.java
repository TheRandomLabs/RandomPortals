package com.therandomlabs.verticalendportals.block;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import com.therandomlabs.verticalendportals.VEPConfig;
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

public class BlockVerticalEndPortalFrame extends BlockEndPortalFrame {
	private static final Map<EnumFacing, AxisAlignedBB> AABB_BLOCK = new EnumMap<>(EnumFacing.class);
	private static final Map<EnumFacing, AxisAlignedBB> AABB_EYE = new EnumMap<>(EnumFacing.class);

	private static final Map<EnumFacing, BlockPattern> portalShape = new EnumMap<>(EnumFacing.class);

	private static BlockPattern horizontalPortalShape;

	private final Random random = new Random();

	static {
		AABB_BLOCK.put(EnumFacing.NORTH, new AxisAlignedBB(
				0.0, 0.0, 0.1875, 1.0, 1.0, 1.0
		));

		AABB_BLOCK.put(EnumFacing.SOUTH, new AxisAlignedBB(
				0.0, 0.0, 0.0, 1.0, 1.0, 0.8125
		));

		AABB_BLOCK.put(EnumFacing.WEST, new AxisAlignedBB(
				0.1875, 0.0, 0.0, 1.0, 1.0, 1.0
		));

		AABB_BLOCK.put(EnumFacing.EAST, new AxisAlignedBB(
				0.0, 0.0, 0.0, 0.8125, 1.0, 1.0
		));

		AABB_EYE.put(EnumFacing.NORTH, new AxisAlignedBB(
				0.3125, 0.3125, 0.0, 0.6875, 0.6875, 0.1875
		));

		AABB_EYE.put(EnumFacing.SOUTH, new AxisAlignedBB(
				0.3125, 0.3125, 0.1875, 0.6875, 0.6875, 1.0
		));

		AABB_EYE.put(EnumFacing.WEST, new AxisAlignedBB(
				0.0, 0.3125, 0.3125, 0.1875, 0.6875, 0.6875
		));

		AABB_EYE.put(EnumFacing.EAST, new AxisAlignedBB(
				0.8125, 0.3125, 0.3125, 1.0, 0.6875, 0.6875
		));
	}

	public BlockVerticalEndPortalFrame() {
		setSoundType(SoundType.GLASS);
		setLightLevel(0.125F);
		setHardness(-1.0F);
		setResistance(6000000.0F);
		setCreativeTab(CreativeTabs.DECORATIONS);
		setTranslationKey("endPortalFrameVertical");
		setRegistryName("vertical_end_portal_frame");
	}

	@Override
	public boolean canEntityDestroy(IBlockState state, IBlockAccess world, BlockPos pos,
			Entity entity) {
		return false;
	}

	@SuppressWarnings("deprecation")
	@Override
	public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess world, BlockPos pos) {
		return AABB_BLOCK.get(state.getValue(FACING));
	}

	@SuppressWarnings("deprecation")
	@Override
	public void addCollisionBoxToList(IBlockState state, World world, BlockPos pos,
			AxisAlignedBB entityBox, List<AxisAlignedBB> collidingBoxes, Entity entity,
			boolean isActualState) {
		final EnumFacing facing = state.getValue(FACING);

		addCollisionBoxToList(pos, entityBox, collidingBoxes, AABB_BLOCK.get(facing));

		if(state.getValue(EYE)) {
			addCollisionBoxToList(pos, entityBox, collidingBoxes, AABB_EYE.get(facing));
		}
	}

	@Override
	public boolean onBlockActivated(World world, BlockPos pos, IBlockState state,
			EntityPlayer player, EnumHand hand, EnumFacing facing, float hitX, float hitY,
			float hitZ) {
		final ItemStack stack = player.getHeldItem(hand);

		if(stack.getItem() != Items.ENDER_EYE ||
				state.getBlock() != VEPBlocks.vertical_end_portal_frame ||
				!player.canPlayerEdit(pos.offset(facing), facing, stack) || state.getValue(EYE)) {
			return false;
		}

		if(world.isRemote) {
			return true;
		}

		world.setBlockState(pos, state.withProperty(EYE, true), 2);
		world.updateComparatorOutputLevel(pos, VEPBlocks.vertical_end_portal_frame);

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

		BlockPattern.PatternHelper patternHelper =
				getPortalShape(state.getValue(FACING)).match(world, pos);
		boolean horizontal = false;

		if(patternHelper == null) {
			if(!VEPConfig.misc.allowHorizontalPortals) {
				return false;
			}

			patternHelper = getPortalShape(null).match(world, pos);

			if(patternHelper == null) {
				return false;
			}

			horizontal = true;
		}

		if(horizontal) {
			final BlockPos portalPos = patternHelper.getFrontTopLeft().add(-3, 0, -3);

			for(int i = 0; i < 3; i++) {
				for(int j = 0; j < 3; j++) {
					world.setBlockState(
							portalPos.add(i, 0, j), Blocks.END_PORTAL.getDefaultState(), 2
					);
				}
			}

			world.playBroadcastSound(1038, portalPos.add(1, 0, 1), 0);

			return true;
		}

		final EnumFacing portalFacing = state.getValue(FACING);
		final List<BlockPos> positions = patternHelper.getPositions();
		final BlockPos first = positions.get(0);

		int z = first.getZ();
		int x = first.getX();
		int y = first.getY();

		if(portalFacing == EnumFacing.NORTH || portalFacing == EnumFacing.SOUTH) {
			for(BlockPos position : patternHelper.getPositions()) {
				final int newX = position.getX();
				final int newY = position.getY();

				if(position.getZ() != z) {
					return false;
				}

				if(newX < x) {
					x = newX;
				}

				if(newY > y) {
					y = newY;
				}
			}

			final BlockPos topLeft = new BlockPos(x, y, z);

			for(int i = 1; i < 4; i++) {
				for(int j = 1; j < 4; j++) {
					world.setBlockState(
							topLeft.add(i, -j, 0),
							VEPBlocks.vertical_end_portal.getDefaultState().withProperty(
									BlockVerticalEndPortal.FACING,
									portalFacing
							),
							2
					);
				}
			}

			world.playBroadcastSound(1038, topLeft.add(1, 0, 1), 0);

			return true;
		}

		if(portalFacing != EnumFacing.WEST && portalFacing != EnumFacing.EAST) {
			return false;
		}

		for(BlockPos position : patternHelper.getPositions()) {
			final int newY = position.getY();
			final int newZ = position.getZ();

			if(position.getX() != x) {
				return false;
			}

			if(newY > y) {
				y = newY;
			}

			if(newZ < z) {
				z = newZ;
			}
		}

		final BlockPos topLeft = new BlockPos(x, y, z);

		for(int i = 1; i < 4; i++) {
			for(int j = 1; j < 4; j++) {
				world.setBlockState(
						topLeft.add(0, -i, j),
						VEPBlocks.vertical_end_portal.getDefaultState().withProperty(
								BlockVerticalEndPortal.FACING,
								portalFacing
						),
						2
				);
			}
		}

		world.playBroadcastSound(1038, topLeft.add(1, 0, 1), 0);

		return true;
	}

	public static BlockPattern getPortalShape(EnumFacing portalFacing) {
		if(horizontalPortalShape == null) {
			horizontalPortalShape = BlockPatternFactory.start().
					aisle("?vvv?", ">???<", ">???<", ">???<", "?^^^?").
					where('?', BlockWorldState.hasState(BlockStateMatcher.ANY)).
					where('^', BlockWorldState.hasState(
							BlockStateMatcher.forBlock(VEPBlocks.vertical_end_portal_frame).
									where(EYE, eye -> eye).
									where(FACING, facing -> facing == EnumFacing.SOUTH)
					)).
					where('>', BlockWorldState.hasState(
							BlockStateMatcher.forBlock(VEPBlocks.vertical_end_portal_frame).
									where(EYE, eye -> eye).
									where(FACING, facing -> facing == EnumFacing.WEST)
					)).
					where('v', BlockWorldState.hasState(
							BlockStateMatcher.forBlock(VEPBlocks.vertical_end_portal_frame).
									where(EYE, eye -> eye).
									where(FACING, facing -> facing == EnumFacing.NORTH)
					)).
					where('<', BlockWorldState.hasState(
							BlockStateMatcher.forBlock(VEPBlocks.vertical_end_portal_frame).
									where(EYE, eye -> eye).
									where(FACING, facing -> facing == EnumFacing.EAST)
					)).
					build();

			portalShape.put(
					EnumFacing.NORTH,
					BlockPatternFactory.start().
							aisle("?vvv?").
							aisle("v???v").
							aisle("v???v").
							aisle("v???v").
							aisle("?vvv?").
							where('?', BlockWorldState.hasState(BlockStateMatcher.ANY)).
							where('v', BlockWorldState.hasState(
									BlockStateMatcher.forBlock(VEPBlocks.vertical_end_portal_frame).
											where(EYE, eye -> eye).
											where(FACING, facing -> facing == EnumFacing.NORTH)
							)).
							build()
			);

			portalShape.put(EnumFacing.SOUTH, BlockPatternFactory.start().
					aisle("?vvv?").
					aisle("v???v").
					aisle("v???v").
					aisle("v???v").
					aisle("?vvv?").
					where('?', BlockWorldState.hasState(BlockStateMatcher.ANY)).
					where('v', BlockWorldState.hasState(
							BlockStateMatcher.forBlock(VEPBlocks.vertical_end_portal_frame).
									where(EYE, eye -> eye).
									where(FACING, facing -> facing == EnumFacing.SOUTH)
					)).
					build());

			portalShape.put(EnumFacing.WEST, BlockPatternFactory.start().
					aisle("?vvv?").
					aisle("v???v").
					aisle("v???v").
					aisle("v???v").
					aisle("?vvv?").
					where('?', BlockWorldState.hasState(BlockStateMatcher.ANY)).
					where('v', BlockWorldState.hasState(
							BlockStateMatcher.forBlock(VEPBlocks.vertical_end_portal_frame).
									where(EYE, eye -> eye).
									where(FACING, facing -> facing == EnumFacing.WEST)
					)).
					build());

			portalShape.put(EnumFacing.EAST, BlockPatternFactory.start().
					aisle("?vvv?").
					aisle("v???v").
					aisle("v???v").
					aisle("v???v").
					aisle("?vvv?").
					where('?', BlockWorldState.hasState(BlockStateMatcher.ANY)).
					where('v', BlockWorldState.hasState(
							BlockStateMatcher.forBlock(VEPBlocks.vertical_end_portal_frame).
									where(EYE, eye -> eye).
									where(FACING, facing -> facing == EnumFacing.EAST)
					)).
					build());
		}

		return portalFacing == null ? horizontalPortalShape : portalShape.get(portalFacing);
	}
}
