package com.therandomlabs.randomportals.block;

import java.util.EnumMap;
import java.util.Map;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

//Because BlockPortal forces the AXIS property, which only accepts X and Z,
//we have to behave as if the block is on the Y-axis while ignoring the AXIS property,
//which is always X
public class BlockLateralNetherPortal extends BlockNetherPortal {
	private static final Map<EnumDyeColor, BlockLateralNetherPortal> colors =
			new EnumMap<>(EnumDyeColor.class);

	public BlockLateralNetherPortal(EnumDyeColor color) {
		super(color.getName() + "_lateral_nether_portal", color);

		final String translationKey = color.getTranslationKey();
		setTranslationKey(
				"netherPortalLateral" + Character.toUpperCase(translationKey.charAt(0)) +
						translationKey.substring(1)
		);

		if (!colors.containsKey(color)) {
			colors.put(color, this);
		}
	}

	@Override
	public ItemStack getItem(World world, BlockPos pos, IBlockState state) {
		return new ItemStack(this);
	}

	@Override
	public int getMetaFromState(IBlockState state) {
		return state.getValue(USER_PLACED) ? 4 : 1;
	}

	@Override
	public IBlockState getStateForPlacement(
			World world, BlockPos pos, EnumFacing facing,
			float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer
	) {
		return getDefaultState();
	}

	@Override
	public EnumFacing.Axis getEffectiveAxis(IBlockState state) {
		return EnumFacing.Axis.Y;
	}

	@Override
	public BlockNetherPortal getByColor(EnumDyeColor color) {
		return get(color);
	}

	@SuppressWarnings("deprecation")
	@Override
	public IBlockState withRotation(IBlockState state, Rotation rotation) {
		return getDefaultState();
	}

	public static BlockLateralNetherPortal get(EnumDyeColor color) {
		final BlockLateralNetherPortal block = colors.get(color);
		return block == null ? RPOBlocks.purple_lateral_nether_portal : block;
	}
}
