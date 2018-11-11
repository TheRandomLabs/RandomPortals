package com.therandomlabs.verticalendportals.block;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import com.therandomlabs.verticalendportals.VerticalEndPortals;
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
	private static BlockPattern inwardsFacingPortalShapeNorthSouth;
	private static BlockPattern inwardsFacingPortalShapeEastWest;
	private static BlockPattern lateralPortalShape;

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

		EnumFacing frameFacing = state.getValue(FACING);

		BlockPattern.PatternHelper patternHelper = getPortalShape(frameFacing).match(world, pos);
		boolean lateral = false;

		if(patternHelper == null) {
			patternHelper = getInwardsFacingPortalShape(frameFacing).match(world, pos);

			if(patternHelper == null) {
				patternHelper = getLateralPortalShape().match(world, pos);

				if(patternHelper == null) {
					return false;
				}

				lateral = true;
			} else {
				if(frameFacing == EnumFacing.NORTH) {
					frameFacing = EnumFacing.WEST;
				} else if(frameFacing == EnumFacing.WEST) {
					frameFacing = EnumFacing.SOUTH;
				} else if(frameFacing == EnumFacing.SOUTH) {
					frameFacing = EnumFacing.EAST;
				} else {
					frameFacing = EnumFacing.NORTH;
				}
			}
		}

		if(lateral) {
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

		final List<BlockPos> positions = patternHelper.getPositions();
		final BlockPos first = positions.get(0);

		int z = first.getZ();
		int x = first.getX();
		int y = first.getY();

		if(frameFacing == EnumFacing.NORTH || frameFacing == EnumFacing.SOUTH) {
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
									frameFacing
							),
							2
					);
				}
			}

			world.playBroadcastSound(1038, topLeft.add(1, 0, 1), 0);

			return true;
		}

		if(frameFacing != EnumFacing.WEST && frameFacing != EnumFacing.EAST) {
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
								frameFacing
						),
						2
				);
			}
		}

		world.playBroadcastSound(1038, topLeft.add(1, 0, 1), 0);

		return true;
	}

	public static BlockPattern getPortalShape(EnumFacing frameFacing) {
		initializePortalShapes();
		return portalShape.get(frameFacing);
	}

	public static BlockPattern getInwardsFacingPortalShape(EnumFacing frameFacing) {
		initializePortalShapes();

		if(frameFacing == EnumFacing.NORTH || frameFacing == EnumFacing.SOUTH) {
			return inwardsFacingPortalShapeNorthSouth;
		}

		return inwardsFacingPortalShapeEastWest;
	}

	public static BlockPattern getLateralPortalShape() {
		initializePortalShapes();
		return lateralPortalShape;
	}

	@SuppressWarnings("Duplicates")
	private static void initializePortalShapes() {
		if(lateralPortalShape != null) {
			return;
		}

		inwardsFacingPortalShapeNorthSouth = BlockPatternFactory.start().
				aisle("?vvv?").
				aisle(">???<").
				aisle(">???<").
				aisle(">???<").
				aisle("?^^^?").
				where('?', BlockWorldState.hasState(BlockStateMatcher.ANY)).
				where('^', BlockWorldState.hasState(
						BlockStateMatcher.forBlock(Blocks.END_PORTAL_FRAME).
								where(EYE, eye -> eye)
				)).
				where('>', BlockWorldState.hasState(
						BlockStateMatcher.forBlock(VEPBlocks.vertical_end_portal_frame).
								where(EYE, eye -> eye).
								where(FACING, facing -> facing == EnumFacing.SOUTH)
				)).
				where('v', BlockWorldState.hasState(
						BlockStateMatcher.forBlock(VEPBlocks.upside_down_end_portal_frame).
								where(EYE, eye -> eye)
				)).
				where('<', BlockWorldState.hasState(
						BlockStateMatcher.forBlock(VEPBlocks.vertical_end_portal_frame).
								where(EYE, eye -> eye).
								where(FACING, facing -> facing == EnumFacing.NORTH)
				)).
				build();

		inwardsFacingPortalShapeEastWest = BlockPatternFactory.start().
				aisle("?vvv?").
				aisle(">???<").
				aisle(">???<").
				aisle(">???<").
				aisle("?^^^?").
				where('?', BlockWorldState.hasState(BlockStateMatcher.ANY)).
				where('^', BlockWorldState.hasState(
						BlockStateMatcher.forBlock(Blocks.END_PORTAL_FRAME).
								where(EYE, eye -> eye)
				)).
				where('>', BlockWorldState.hasState(
						BlockStateMatcher.forBlock(VEPBlocks.vertical_end_portal_frame).
								where(EYE, eye -> eye).
								where(FACING, facing -> facing == EnumFacing.EAST)
				)).
				where('v', BlockWorldState.hasState(
						BlockStateMatcher.forBlock(VEPBlocks.upside_down_end_portal_frame).
								where(EYE, eye -> eye)
				)).
				where('<', BlockWorldState.hasState(
						BlockStateMatcher.forBlock(VEPBlocks.vertical_end_portal_frame).
								where(EYE, eye -> eye).
								where(FACING, facing -> facing == EnumFacing.WEST)
				)).
				build();

		lateralPortalShape = BlockPatternFactory.start().
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
				build()
		);

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
				build()
		);

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
				build()
		);
	}
}
