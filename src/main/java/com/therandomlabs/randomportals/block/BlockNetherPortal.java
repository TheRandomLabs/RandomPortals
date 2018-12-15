package com.therandomlabs.randomportals.block;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import com.google.common.collect.ImmutableList;
import com.therandomlabs.randomportals.RPOConfig;
import com.therandomlabs.randomportals.RandomPortals;
import com.therandomlabs.randomportals.api.config.FrameSize;
import com.therandomlabs.randomportals.api.config.NetherPortalTypes;
import com.therandomlabs.randomportals.api.frame.Frame;
import com.therandomlabs.randomportals.api.frame.FrameDetector;
import com.therandomlabs.randomportals.api.frame.FrameType;
import com.therandomlabs.randomportals.api.netherportal.NetherPortal;
import com.therandomlabs.randomportals.api.netherportal.PortalBlockRegistry;
import com.therandomlabs.randomportals.api.util.FrameStatePredicate;
import com.therandomlabs.randomportals.frame.NetherPortalFrames;
import com.therandomlabs.randomportals.handler.NetherPortalTeleportHandler;
import com.therandomlabs.randomportals.world.storage.RPOSavedData;
import net.minecraft.block.Block;
import net.minecraft.block.BlockPortal;
import net.minecraft.block.SoundType;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@Mod.EventBusSubscriber(modid = RandomPortals.MOD_ID)
public class BlockNetherPortal extends BlockPortal {
	public static final class Matcher {
		public static final FrameStatePredicate LATERAL = FrameStatePredicate.ofBlock(
				block -> block.getClass() == BlockLateralNetherPortal.class
		);

		public static final FrameStatePredicate VERTICAL_X = FrameStatePredicate.ofBlock(
				block -> block.getClass() == BlockNetherPortal.class
		).where(BlockNetherPortal.AXIS, axis -> axis == EnumFacing.Axis.X);

		public static final FrameStatePredicate VERTICAL_Z = FrameStatePredicate.ofBlock(
				block -> block.getClass() == BlockNetherPortal.class
		).where(BlockNetherPortal.AXIS, axis -> axis == EnumFacing.Axis.Z);

		private Matcher() {}

		public static FrameStatePredicate ofType(FrameType type) {
			return type.get(LATERAL, VERTICAL_X, VERTICAL_Z);
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

	private static final Map<EnumDyeColor, BlockNetherPortal> colors =
			new EnumMap<>(EnumDyeColor.class);

	private final EnumDyeColor color;

	public BlockNetherPortal(EnumDyeColor color) {
		this(
				color == EnumDyeColor.PURPLE ?
						"minecraft:portal" : color.getName() + "_vertical_nether_portal",
				color
		);

		final String translationKey = color.getTranslationKey();
		setTranslationKey(
				"netherPortalVertical" + Character.toUpperCase(translationKey.charAt(0)) +
						translationKey.substring(1)
		);

		if(!colors.containsKey(color)) {
			colors.put(color, this);
		}
	}

	protected BlockNetherPortal(String registryName, EnumDyeColor color) {
		setDefaultState(blockState.getBaseState().
				withProperty(AXIS, EnumFacing.Axis.X).
				withProperty(USER_PLACED, true));
		setTickRandomly(true);
		setHardness(-1.0F);
		setSoundType(SoundType.GLASS);
		setLightLevel(0.75F);
		setCreativeTab(CreativeTabs.DECORATIONS);
		setRegistryName(registryName);
		PortalBlockRegistry.register(this);
		this.color = color;
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

	@Override
	public void neighborChanged(IBlockState state, World world, BlockPos pos, Block block,
			BlockPos fromPos) {
		if(removing.contains(fromPos)) {
			return;
		}

		final RPOSavedData savedData = RPOSavedData.get(world);
		NetherPortal portal = savedData.getNetherPortal(pos);

		//If there is an activated portal here, then ignore userPlaced
		if(state.getValue(USER_PLACED) && portal == null) {
			return;
		}

		final EnumFacing.Axis axis = getEffectiveAxis(state);
		final IBlockState fromState = world.getBlockState(fromPos);

		if(fromState.getBlock() == this && !fromState.getValue(USER_PLACED) &&
				getEffectiveAxis(fromState) == axis) {
			return;
		}

		final EnumFacing irrelevantFacing = getIrrelevantFacing(axis);

		if(pos.offset(irrelevantFacing).equals(fromPos) ||
				pos.offset(irrelevantFacing.getOpposite()).equals(fromPos)) {
			return;
		}

		final Map.Entry<Boolean, NetherPortal> entry;

		if(portal == null) {
			entry = findFrame(NetherPortalFrames.FRAMES, world, pos);
		} else {
			entry = new AbstractMap.SimpleEntry<>(true, portal);
		}

		if(entry != null) {
			portal = entry.getValue();
			final Frame frame = portal.getFrame();

			//entry.getKey() returns whether the frame was retrieved from saved data
			//If true, the frame is not guaranteed to still exist, so we call NetherPortalType.test
			//The following loop then ensures that the inner blocks are all portal blocks
			boolean shouldBreak = entry.getKey() && !portal.getType().test(frame);

			for(BlockPos innerPos : frame.getInnerBlockPositions()) {
				final IBlockState innerState = world.getBlockState(innerPos);
				final Block innerBlock = innerState.getBlock();

				if(innerBlock != this || innerState.getValue(USER_PLACED) ||
						((BlockNetherPortal) innerBlock).getEffectiveAxis(innerState) != axis) {
					shouldBreak = true;
					break;
				}
			}


			if(!shouldBreak) {
				return;
			}

			for(BlockPos innerPos : frame.getInnerBlockPositions()) {
				final IBlockState innerState = world.getBlockState(innerPos);
				final Block innerBlock = innerState.getBlock();

				if(innerBlock == this && !innerState.getValue(USER_PLACED) &&
						((BlockNetherPortal) innerBlock).getEffectiveAxis(innerState) == axis) {
					removing.add(innerPos);
				}
			}
		} else {
			removing.addAll(getConnectedPortals(
					world, pos, this, axis, state.getValue(USER_PLACED)
			));
		}

		for(BlockPos removePos : removing) {
			world.setBlockToAir(removePos);
		}

		removing.clear();
		savedData.removeNetherPortal(pos);
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

	@Override
	public void onEntityCollision(World world, BlockPos pos, IBlockState state, Entity entity) {
		if(entity.isRiding() || entity.isBeingRidden() || !entity.isNonBoss()) {
			return;
		}

		final AxisAlignedBB aabb = entity.getEntityBoundingBox();

		if(!aabb.intersects(state.getBoundingBox(world, pos).offset(pos))) {
			return;
		}

		EnumDyeColor newColor = null;

		if(entity instanceof EntityItem) {
			final ItemStack stack = ((EntityItem) entity).getItem();

			if(stack.getItem() == Items.DYE) {
				newColor = EnumDyeColor.byDyeDamage(stack.getMetadata());
			}
		}

		if(color == newColor) {
			if(RPOConfig.netherPortals.consumeDyesEvenIfSameColor) {
				world.removeEntity(entity);
				return;
			}

			newColor = null;
		}

		if(world.isRemote) {
			if(newColor == null) {
				//On the client, the Nether portal logic is not changed
				entity.setPortal(pos);
			}

			return;
		}

		final NetherPortal portal = RPOSavedData.get(world).getNetherPortal(world, pos);

		if(newColor == null || (portal != null && portal.getType().forceColor)) {
			NetherPortalTeleportHandler.setPortal(entity, portal, pos);
			return;
		}

		final IBlockState newState = getByColor(newColor).getDefaultState().
				withProperty(AXIS, state.getValue(AXIS)).
				withProperty(USER_PLACED, state.getValue(USER_PLACED));

		for(BlockPos portalPos : getRelevantPortalBlockPositions(world, pos)) {
			world.setBlockState(portalPos, newState, 2);
		}

		world.removeEntity(entity);
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
			RPOSavedData.get(world).removeNetherPortal(pos);
		}

		return actuallyRemoved;
	}

	public EnumFacing.Axis getEffectiveAxis(IBlockState state) {
		return state.getValue(AXIS);
	}

	public EnumDyeColor getColor() {
		return color;
	}

	public BlockNetherPortal getByColor(EnumDyeColor color) {
		return get(color);
	}

	public static BlockNetherPortal get(EnumDyeColor color) {
		return colors.get(color);
	}

	public static ImmutableList<BlockPos> getRelevantPortalBlockPositions(World world,
			BlockPos portalPos) {
		final IBlockState state = world.getBlockState(portalPos);
		final BlockNetherPortal block = (BlockNetherPortal) state.getBlock();
		final EnumFacing.Axis axis = block.getEffectiveAxis(state);

		final Map.Entry<Boolean, NetherPortal> entry =
				findFrame(NetherPortalFrames.FRAMES, world, portalPos);

		if(entry != null) {
			return entry.getValue().getFrame().getInnerBlockPositions();
		}

		return getConnectedPortals(world, portalPos, block, axis, state.getValue(USER_PLACED));
	}

	public static Map.Entry<Boolean, NetherPortal> findFrame(FrameDetector detector,
			World world, BlockPos portalPos) {
		final NetherPortal portal =
				RPOSavedData.get(world).getNetherPortal(world, portalPos);

		if(portal != null) {
			return new AbstractMap.SimpleEntry<>(true, portal);
		}

		final IBlockState state = world.getBlockState(portalPos);

		final EnumFacing.Axis axis = ((BlockNetherPortal) state.getBlock()).getEffectiveAxis(state);
		final EnumFacing frameDirection = axis == EnumFacing.Axis.Y ?
				EnumFacing.NORTH : EnumFacing.DOWN;

		final FrameType type = FrameType.fromAxis(axis);
		final FrameSize size = NetherPortalFrames.SIZE.apply(type);
		final int maxSize = size.getMaxSize(frameDirection == EnumFacing.DOWN);

		final FrameStatePredicate portalMatcher = Matcher.ofType(type);

		BlockPos framePos = null;
		BlockPos checkPos = portalPos;

		for(int offset = 1; offset < maxSize - 1; offset++) {
			checkPos = checkPos.offset(frameDirection);

			final IBlockState checkState = world.getBlockState(checkPos);
			final Block checkBlock = checkState.getBlock();

			//If the frame block is a portal, the portal must be user-placed
			if(NetherPortalTypes.getValidBlocks().test(world, checkPos, state) &&
					(!(checkBlock instanceof BlockNetherPortal) ||
							checkState.getValue(USER_PLACED))) {
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

		final Frame frame = detector.detectWithCondition(
				world, framePos, type,
				potentialFrame -> potentialFrame.getInnerBlockPositions().contains(portalPos)
		);

		return new AbstractMap.SimpleEntry<>(false, new NetherPortal(
				frame, null, NetherPortalTypes.get(frame)
		));
	}

	private static ImmutableList<BlockPos> getConnectedPortals(World world, BlockPos portalPos,
			BlockNetherPortal block, EnumFacing.Axis axis, boolean userPlaced) {
		final List<BlockPos> positions = new ArrayList<>();
		final EnumFacing[] relevantFacings = getRelevantFacings(axis);

		positions.add(portalPos);
		int previousSize = 0;

		for(int i = 0; i < positions.size() || positions.size() != previousSize; i++) {
			previousSize = positions.size();
			final BlockPos removingPos = positions.get(i);

			for(EnumFacing facing : relevantFacings) {
				final BlockPos neighbor = removingPos.offset(facing);

				if(positions.contains(neighbor)) {
					continue;
				}

				final IBlockState neighborState = world.getBlockState(neighbor);

				if(neighborState.getBlock() == block &&
						block.getEffectiveAxis(neighborState) == axis &&
						neighborState.getValue(USER_PLACED) == userPlaced) {
					positions.add(neighbor);
				}
			}
		}

		return ImmutableList.copyOf(positions);
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
