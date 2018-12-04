package com.therandomlabs.verticalendportals.block;

import java.util.ArrayList;
import java.util.List;
import com.therandomlabs.verticalendportals.VerticalEndPortals;
import com.therandomlabs.verticalendportals.api.frame.Frame;
import com.therandomlabs.verticalendportals.api.frame.FrameDetector;
import com.therandomlabs.verticalendportals.api.frame.FrameType;
import com.therandomlabs.verticalendportals.config.NetherPortalTypes;
import com.therandomlabs.verticalendportals.frame.NetherPortalFrames;
import com.therandomlabs.verticalendportals.handler.NetherPortalTeleportHandler;
import com.therandomlabs.verticalendportals.world.storage.NetherPortalSavedData;
import net.minecraft.block.Block;
import net.minecraft.block.BlockPortal;
import net.minecraft.block.SoundType;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.BlockWorldState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.block.state.pattern.BlockStateMatcher;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@Mod.EventBusSubscriber(modid = VerticalEndPortals.MOD_ID)
public class BlockNetherPortal extends BlockPortal {
	public static final class Matcher {
		public static final BlockStateMatcher LATERAL =
				BlockStateMatcher.forBlock(VEPBlocks.lateral_nether_portal);

		public static final BlockStateMatcher VERTICAL_X = BlockStateMatcher.forBlock(
				VEPBlocks.vertical_nether_portal
		).where(BlockNetherPortal.AXIS, axis -> axis == EnumFacing.Axis.X);

		public static final BlockStateMatcher VERTICAL_Z = BlockStateMatcher.forBlock(
				VEPBlocks.vertical_nether_portal
		).where(BlockNetherPortal.AXIS, axis -> axis == EnumFacing.Axis.Z);

		private Matcher() {}

		public static BlockStateMatcher ofType(FrameType type) {
			if(type == FrameType.LATERAL) {
				return BlockNetherPortal.Matcher.LATERAL;
			}

			if(type == FrameType.VERTICAL_X) {
				return BlockNetherPortal.Matcher.VERTICAL_X;
			}

			return BlockNetherPortal.Matcher.VERTICAL_Z;
		}
	}

	public static final PropertyBool USER_PLACED = PropertyBool.create("user_placed");

	public static final AxisAlignedBB AABB_X = new AxisAlignedBB(
			0.0, 0.0, 0.375, 1.0, 1.0, 0.625
	);

	public static final AxisAlignedBB AABB_Y = new AxisAlignedBB(
			0.0, 0.375, 0.0, 1.0, 0.625, 1.0
	);

	public static final AxisAlignedBB AABB_Z = new AxisAlignedBB(
			0.375, 0.0, 0.0, 0.625, 1.0, 1.0
	);

	private static final EnumFacing[] xRelevantFacings = {
			EnumFacing.UP,
			EnumFacing.EAST,
			EnumFacing.DOWN,
			EnumFacing.WEST
	};

	private static final EnumFacing[] yRelevantFacings = {
			EnumFacing.NORTH,
			EnumFacing.EAST,
			EnumFacing.SOUTH,
			EnumFacing.WEST
	};

	private static final EnumFacing[] zRelevantFacings = {
			EnumFacing.UP,
			EnumFacing.NORTH,
			EnumFacing.DOWN,
			EnumFacing.SOUTH
	};

	private static final List<BlockPos> removing = new ArrayList<>();

	public BlockNetherPortal() {
		this(true);
		setTranslationKey("netherPortalVertical");
		setRegistryName("minecraft:portal");
	}

	protected BlockNetherPortal(boolean flag) {
		setDefaultState(blockState.getBaseState().
				withProperty(AXIS, EnumFacing.Axis.X).
				withProperty(USER_PLACED, true));
		setTickRandomly(true);
		setHardness(-1.0F);
		setSoundType(SoundType.GLASS);
		setLightLevel(0.75F);
		setCreativeTab(CreativeTabs.DECORATIONS);
	}

	@SuppressWarnings("deprecation")
	@Override
	public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess world, BlockPos pos) {
		switch(getEffectiveAxis(state)) {
		case X:
			return AABB_X;
		case Y:
			return AABB_Y;
		default:
			return AABB_Z;
		}
	}

	@SuppressWarnings("Duplicates")
	@Override
	public void neighborChanged(IBlockState state, World world, BlockPos pos, Block block,
			BlockPos fromPos) {
		if(removing.contains(fromPos) || state.getValue(USER_PLACED)) {
			return;
		}

		final EnumFacing.Axis axis = getEffectiveAxis(state);
		final IBlockState fromState = world.getBlockState(fromPos);

		if(fromState.getBlock() == this && !fromState.getValue(USER_PLACED) &&
				getEffectiveAxis(fromState) == axis) {
			return;
		}

		final EnumFacing irrelevantFacing = getIrrelevantFacing(axis);
		final EnumFacing[] relevantFacings = getRelevantFacings(axis);

		if(pos.offset(irrelevantFacing).equals(fromPos) ||
				pos.offset(irrelevantFacing.getOpposite()).equals(fromPos)) {
			return;
		}

		final Frame frame = findFrame(NetherPortalFrames.FRAMES, world, pos);

		if(frame != null) {
			boolean nonPortalFound = false;

			for(BlockPos innerPos : frame.getInnerBlockPositions()) {
				if(!isPortal(world, innerPos)) {
					nonPortalFound = true;
					break;
				}
			}

			if(!nonPortalFound) {
				return;
			}

			for(BlockPos innerPos : frame.getInnerBlockPositions()) {
				final IBlockState innerState = world.getBlockState(innerPos);

				if(innerState.getBlock() == this && !state.getValue(USER_PLACED) &&
						getEffectiveAxis(innerState) == axis) {
					removing.add(innerPos);
				}
			}
		} else {
			removing.add(pos);

			int previousSize = -1;

			for(int i = 0; i < removing.size() || removing.size() != previousSize; i++) {
				previousSize = removing.size();
				final BlockPos removingPos = removing.get(i);

				for(EnumFacing facing : relevantFacings) {
					final BlockPos neighbor = removingPos.offset(facing);

					if(removing.contains(neighbor)) {
						continue;
					}

					final IBlockState neighborState = world.getBlockState(neighbor);

					if(neighborState.getBlock() == this && !neighborState.getValue(USER_PLACED) &&
							getEffectiveAxis(neighborState) == axis) {
						removing.add(neighbor);
					}
				}
			}
		}

		for(BlockPos removePos : removing) {
			world.setBlockToAir(removePos);
		}

		removing.clear();
		NetherPortalSavedData.get(world).removePortal(pos);
	}

	@SuppressWarnings("deprecation")
	@SideOnly(Side.CLIENT)
	@Override
	public boolean shouldSideBeRendered(IBlockState state, IBlockAccess world, BlockPos pos,
			EnumFacing side) {
		pos = pos.offset(side);
		EnumFacing.Axis axis = null;

		if(state.getBlock() == this) {
			axis = getEffectiveAxis(state);

			if(axis == null) {
				return false;
			}

			if(axis == EnumFacing.Axis.Z && side != EnumFacing.EAST && side != EnumFacing.WEST) {
				return false;
			}

			if(axis == EnumFacing.Axis.X && side != EnumFacing.SOUTH && side != EnumFacing.NORTH) {
				return false;
			}
		}

		final boolean west = world.getBlockState(pos.west()).getBlock() == this &&
				world.getBlockState(pos.west(2)).getBlock() != this;

		final boolean east = world.getBlockState(pos.east()).getBlock() == this &&
				world.getBlockState(pos.east(2)).getBlock() != this;

		final boolean north = world.getBlockState(pos.north()).getBlock() == this &&
				world.getBlockState(pos.north(2)).getBlock() != this;

		final boolean south = world.getBlockState(pos.south()).getBlock() == this &&
				world.getBlockState(pos.south(2)).getBlock() != this;

		final boolean x = west || east || axis == EnumFacing.Axis.X;
		final boolean z = north || south || axis == EnumFacing.Axis.Z;

		if(x) {
			return side == EnumFacing.WEST || side == EnumFacing.EAST;
		}

		return z && (side == EnumFacing.NORTH || side == EnumFacing.SOUTH);
	}

	@SuppressWarnings("Duplicates")
	@Override
	public void onEntityCollision(World world, BlockPos pos, IBlockState state, Entity entity) {
		if(entity.isRiding() || entity.isBeingRidden() || !entity.isNonBoss()) {
			return;
		}

		final AxisAlignedBB aabb = entity.getEntityBoundingBox();

		if(!aabb.intersects(state.getBoundingBox(world, pos).offset(pos))) {
			return;
		}

		if(world.isRemote) {
			//Use vanilla Minecraft logic
			entity.setPortal(pos);
			return;
		}

		final NetherPortalSavedData.Portal portal = NetherPortalSavedData.get(world).getPortal(pos);
		NetherPortalTeleportHandler.setPortal(entity, portal, pos);
	}

	@Override
	public ItemStack getItem(World world, BlockPos pos, IBlockState state) {
		return new ItemStack(this);
	}

	@Override
	public IBlockState getStateFromMeta(int meta) {
		final boolean userPlaced;

		if(meta > 2) {
			userPlaced = true;
			meta %= 3;
		} else {
			userPlaced = false;
		}

		final EnumFacing.Axis axis;

		if(meta == 0) {
			axis = EnumFacing.Axis.Y;
		} else if(meta == 1) {
			axis = EnumFacing.Axis.X;
		} else {
			axis = EnumFacing.Axis.Z;
		}

		return getDefaultState().
				withProperty(AXIS, axis).
				withProperty(USER_PLACED, userPlaced);
	}

	@Override
	public int getMetaFromState(IBlockState state) {
		final int toAdd = state.getValue(USER_PLACED) ? 3 : 0;
		return super.getMetaFromState(state) + toAdd;
	}

	@Override
	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, AXIS, USER_PLACED);
	}

	@SuppressWarnings("deprecation")
	@Override
	public IBlockState getStateForPlacement(World world, BlockPos pos, EnumFacing facing,
			float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer) {
		final EnumFacing.Axis axis = placer.getHorizontalFacing().getAxis();
		return getDefaultState().withProperty(
				AXIS,
				axis == EnumFacing.Axis.X ? EnumFacing.Axis.Z : EnumFacing.Axis.X
		);
	}

	@Override
	public boolean removedByPlayer(IBlockState state, World world, BlockPos pos,
			EntityPlayer player, boolean willHarvest) {
		final boolean actuallyRemoved =
				super.removedByPlayer(state, world, pos, player, willHarvest);

		if(!world.isRemote) {
			NetherPortalSavedData.get(world).removePortal(pos);
		}

		return actuallyRemoved;
	}

	public EnumFacing.Axis getEffectiveAxis(IBlockState state) {
		return state.getValue(AXIS);
	}

	public static boolean isPortal(IBlockState state) {
		return isPortal(state.getBlock());
	}

	public static boolean isPortal(World world, BlockWorldState state) {
		return isPortal(world, state.getPos());
	}

	public static boolean isPortal(World world, BlockPos pos) {
		return isPortal(world.getBlockState(pos).getBlock());
	}

	@SuppressWarnings("ConditionCoveredByFurtherCondition")
	public static boolean isPortal(Block block) {
		return block == VEPBlocks.vertical_nether_portal ||
				block == VEPBlocks.lateral_nether_portal;
	}

	public static Frame findFrame(FrameDetector detector, World world, BlockPos portalPos) {
		final NetherPortalSavedData.Portal portal =
				NetherPortalSavedData.get(world).getPortal(portalPos);

		if(portal != null) {
			return portal.getFrame();
		}

		final IBlockState state = world.getBlockState(portalPos);

		final EnumFacing.Axis axis = ((BlockNetherPortal) state.getBlock()).getEffectiveAxis(state);
		final EnumFacing frameDirection = axis == EnumFacing.Axis.Y ?
				EnumFacing.NORTH : EnumFacing.DOWN;

		final FrameType type = FrameType.fromAxis(axis);
		final int maxWidth = NetherPortalFrames.SIZE.apply(type).maxWidth;

		final BlockStateMatcher portalMatcher = Matcher.ofType(type);

		BlockPos framePos = null;
		BlockPos checkPos = portalPos;

		for(int offset = 1; offset < maxWidth - 1; offset++) {
			checkPos = checkPos.offset(frameDirection);

			final IBlockState checkState = world.getBlockState(checkPos);
			final Block checkBlock = checkState.getBlock();

			//If the frame block is a portal, the portal must be user-placed
			if(NetherPortalTypes.getValidBlocks().contains(checkBlock) &&
					(!(checkBlock instanceof BlockNetherPortal) ||
							checkState.getValue(USER_PLACED))) {
				framePos = checkPos;
				break;
			}

			if(!portalMatcher.apply(checkState)) {
				break;
			}
		}

		if(framePos == null) {
			return null;
		}

		return detector.detectWithCondition(
				world, framePos, type,
				potentialFrame -> potentialFrame.getInnerBlockPositions().contains(portalPos)
		);
	}

	private static EnumFacing getIrrelevantFacing(EnumFacing.Axis axis) {
		if(axis == EnumFacing.Axis.X) {
			return EnumFacing.NORTH;
		}

		if(axis == EnumFacing.Axis.Y) {
			return EnumFacing.UP;
		}

		return EnumFacing.EAST;
	}

	private static EnumFacing[] getRelevantFacings(EnumFacing.Axis axis) {
		if(axis == EnumFacing.Axis.X) {
			return xRelevantFacings;
		}

		if(axis == EnumFacing.Axis.Y) {
			return yRelevantFacings;
		}

		return zRelevantFacings;
	}
}
