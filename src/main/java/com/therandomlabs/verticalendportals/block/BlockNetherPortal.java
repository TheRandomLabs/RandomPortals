package com.therandomlabs.verticalendportals.block;

import com.therandomlabs.verticalendportals.VerticalEndPortals;
import com.therandomlabs.verticalendportals.util.VEPTeleporter;
import net.minecraft.block.Block;
import net.minecraft.block.BlockPortal;
import net.minecraft.block.SoundType;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.DimensionType;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@Mod.EventBusSubscriber(modid = VerticalEndPortals.MOD_ID)
public class BlockNetherPortal extends BlockPortal {
	public static final PropertyBool MANUALLY_PLACED = PropertyBool.create("manually_placed");

	public static final AxisAlignedBB AABB_X = new AxisAlignedBB(
			0.0, 0.0, 0.375, 1.0, 1.0, 0.625
	);

	public static final AxisAlignedBB AABB_Y = new AxisAlignedBB(
			0.0, 0.375, 0.0, 1.0, 0.625, 1.0
	);

	public static final AxisAlignedBB AABB_Z = new AxisAlignedBB(
			0.375, 0.0, 0.0, 0.625, 1.0, 1.0
	);

	public BlockNetherPortal() {
		this(true);
		setDefaultState(blockState.getBaseState().withProperty(AXIS, EnumFacing.Axis.X).
				withProperty(MANUALLY_PLACED, true));
		setTranslationKey("netherPortalVertical");
		setRegistryName("minecraft:portal");
	}

	protected BlockNetherPortal(boolean flag) {
		setTickRandomly(true);
		setHardness(-1.0F);
		setSoundType(SoundType.GLASS);
		setLightLevel(0.75F);
		setCreativeTab(CreativeTabs.DECORATIONS);
	}

	@SuppressWarnings("deprecation")
	@Override
	public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess world, BlockPos pos) {
		switch(getAxis(state)) {
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
		if(state.getValue(MANUALLY_PLACED)) {
			return;
		}

		final IBlockState fromState = world.getBlockState(fromPos);

		if(fromState.getBlock() == this && !fromState.getValue(MANUALLY_PLACED)) {
			return;
		}

		final EnumFacing checkFacing1;
		final EnumFacing checkFacing2;

		switch(getAxis(state)) {
		case X:
			checkFacing1 = EnumFacing.NORTH;
			checkFacing2 = EnumFacing.SOUTH;
			break;
		case Y:
			checkFacing1 = EnumFacing.UP;
			checkFacing2 = EnumFacing.DOWN;
			break;
		default:
			checkFacing1 = EnumFacing.EAST;
			checkFacing2 = EnumFacing.WEST;
		}

		if(pos.offset(checkFacing1).equals(fromPos) || pos.offset(checkFacing2).equals(fromPos)) {
			return;
		}

		world.setBlockToAir(pos);
	}

	@SuppressWarnings("deprecation")
	@SideOnly(Side.CLIENT)
	@Override
	public boolean shouldSideBeRendered(IBlockState state, IBlockAccess world, BlockPos pos,
			EnumFacing side) {
		pos = pos.offset(side);
		EnumFacing.Axis axis = null;

		if(state.getBlock() == this) {
			axis = getAxis(state);

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
		if(!entity.isRiding() && !entity.isBeingRidden() && entity.isNonBoss()) {
			entity.setPortal(pos);

			if(world.isRemote) {
				return;
			}

			final MinecraftServer server = world.getMinecraftServer();
			final WorldServer overworld = server.getWorld(DimensionType.OVERWORLD.getId());

			if(!(overworld.worldTeleporter instanceof VEPTeleporter)) {
				overworld.worldTeleporter = new VEPTeleporter(overworld);
				final WorldServer nether = server.getWorld(DimensionType.NETHER.getId());
				nether.worldTeleporter = new VEPTeleporter(nether);
			}

			VerticalEndPortals.LOGGER.error(overworld.worldTeleporter);
		}
	}

	@Override
	public ItemStack getItem(World world, BlockPos pos, IBlockState state) {
		return new ItemStack(this);
	}

	@Override
	public IBlockState getStateFromMeta(int meta) {
		meta = meta & 3;
		final EnumFacing.Axis axis;

		if(meta == 0) {
			axis = EnumFacing.Axis.Y;
		} else if(meta == 1) {
			axis = EnumFacing.Axis.X;
		} else {
			axis = EnumFacing.Axis.Z;
		}

		return getDefaultState().withProperty(AXIS, axis);
	}

	@Override
	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, AXIS, MANUALLY_PLACED);
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

	protected EnumFacing.Axis getAxis(IBlockState state) {
		return state.getValue(AXIS);
	}
}
