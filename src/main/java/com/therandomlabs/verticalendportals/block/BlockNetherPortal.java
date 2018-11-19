package com.therandomlabs.verticalendportals.block;

import java.util.Random;
import net.minecraft.block.Block;
import net.minecraft.block.BlockPortal;
import net.minecraft.block.SoundType;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityPigZombie;
import net.minecraft.init.Blocks;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemMonsterPlacer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.Rotation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class BlockNetherPortal extends BlockPortal {
	public static final PropertyEnum<EnumFacing.Axis> AXIS = PropertyEnum.create(
			"axis", EnumFacing.Axis.class, EnumFacing.Axis.X, EnumFacing.Axis.Z
	);

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
		setDefaultState(blockState.getBaseState().withProperty(AXIS, EnumFacing.Axis.X));
		setTranslationKey("netherPortalVertical");
		setRegistryName("minecraft:portal");
	}

	protected BlockNetherPortal(boolean flag) {
		super();//super(Material.PORTAL, false);
		setTickRandomly(true);
		setHardness(-1.0F);
		setSoundType(SoundType.GLASS);
		setLightLevel(0.75F);
		setCreativeTab(CreativeTabs.DECORATIONS);
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
	public int getMetaFromState(IBlockState state) {
		return BlockPortal.getMetaForAxis(state.getValue(AXIS));
	}

	@SuppressWarnings("deprecation")
	@Override
	public IBlockState withRotation(IBlockState state, Rotation rotation) {
		switch(rotation) {
		case COUNTERCLOCKWISE_90:
		case CLOCKWISE_90:
			switch(state.getValue(AXIS)) {
			case X:
				return state.withProperty(AXIS, EnumFacing.Axis.Z);
			case Z:
				return state.withProperty(AXIS, EnumFacing.Axis.X);
			default:
				return state;
			}
		default:
			return state;
		}
	}

	@SuppressWarnings("deprecation")
	@Override
	public boolean isFullCube(IBlockState state) {
		return false;
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

	@SuppressWarnings("deprecation")
	@Override
	public AxisAlignedBB getCollisionBoundingBox(IBlockState state, IBlockAccess world,
			BlockPos pos) {
		return NULL_AABB;
	}

	@Override
	public void updateTick(World world, BlockPos pos, IBlockState state, Random random) {
		if(!world.provider.isSurfaceWorld() || !world.getGameRules().getBoolean("doMobSpawning") ||
				random.nextInt(2000) > world.getDifficulty().getId()) {
			return;
		}

		final int y = pos.getY();
		BlockPos spawnPos = pos;

		while(!world.getBlockState(spawnPos).isSideSolid(world, spawnPos, EnumFacing.UP) &&
				spawnPos.getY() > 0) {
			spawnPos = spawnPos.down();
		}

		if(y > 0 && !world.getBlockState(spawnPos.up()).isNormalCube()) {
			final Entity pigZombie = ItemMonsterPlacer.spawnCreature(
					world,
					EntityList.getKey(EntityPigZombie.class),
					spawnPos.getX() + 0.5,
					spawnPos.getY() + 1.1,
					spawnPos.getZ() + 0.5
			);

			if(pigZombie != null) {
				pigZombie.timeUntilPortal = pigZombie.getPortalCooldown();
			}
		}
	}

	@SideOnly(Side.CLIENT)
	@Override
	public void randomDisplayTick(IBlockState state, World world, BlockPos pos, Random random) {
		if(random.nextInt(100) == 0) {
			world.playSound(
					pos.getX() + 0.5,
					pos.getY() + 0.5,
					pos.getZ() + 0.5,
					SoundEvents.BLOCK_PORTAL_AMBIENT,
					SoundCategory.BLOCKS,
					0.5F,
					random.nextFloat() * 0.4F + 0.8F,
					false
			);
		}

		for(int i = 0; i < 4; ++i) {
			double x = pos.getX() + random.nextFloat();
			final double y = pos.getY() + random.nextFloat();
			double z = pos.getZ() + random.nextFloat();

			double xSpeed = (random.nextFloat() - 0.5) * 0.5;
			final double ySpeed = (random.nextFloat() - 0.5) * 0.5;
			double zSpeed = (random.nextFloat() - 0.5) * 0.5;

			final int multiplier = random.nextInt(2) * 2 - 1;

			if(world.getBlockState(pos.west()).getBlock() != this &&
					world.getBlockState(pos.east()).getBlock() != this) {
				x = pos.getX() + 0.5 + 0.25 * multiplier;
				xSpeed = random.nextFloat() * 2.0F * multiplier;
			} else {
				z = pos.getZ() + 0.5 + 0.25 * multiplier;
				zSpeed = random.nextFloat() * 2.0F * multiplier;
			}

			world.spawnParticle(EnumParticleTypes.PORTAL, x, y, z, xSpeed, ySpeed, zSpeed);
		}
	}

	@Override
	public void neighborChanged(IBlockState state, World world, BlockPos pos, Block block,
			BlockPos fromPos) {
		final BlockPos checkPos1;
		final BlockPos checkPos2;

		switch(getAxis(state)) {
		case X:
			checkPos1 = pos.offset(EnumFacing.NORTH);
			checkPos2 = pos.offset(EnumFacing.SOUTH);
			break;
		case Y:
			checkPos1 = pos.offset(EnumFacing.UP);
			checkPos2 = pos.offset(EnumFacing.DOWN);
			break;
		default:
			checkPos1 = pos.offset(EnumFacing.EAST);
			checkPos2 = pos.offset(EnumFacing.WEST);
		}

		if(!checkPos1.equals(fromPos) && !checkPos2.equals(fromPos)) {
			world.setBlockState(pos, Blocks.AIR.getDefaultState());
		}
	}

	@Override
	public int quantityDropped(Random random) {
		return 0;
	}

	@SideOnly(Side.CLIENT)
	@Override
	public BlockRenderLayer getRenderLayer() {
		return BlockRenderLayer.TRANSLUCENT;
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
	public void onEntityCollision(World world, BlockPos pos, IBlockState state, Entity entity) {
		if(!entity.isRiding() && !entity.isBeingRidden() && entity.isNonBoss()) {
			entity.setPortal(pos);
		}
	}

	@Override
	public ItemStack getItem(World world, BlockPos pos, IBlockState state) {
		return new ItemStack(Blocks.PORTAL);
	}

	@Override
	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, AXIS);
	}

	protected EnumFacing.Axis getAxis(IBlockState state) {
		return state.getValue(AXIS);
	}
}
