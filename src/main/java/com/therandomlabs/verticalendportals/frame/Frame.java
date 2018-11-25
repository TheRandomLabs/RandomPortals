package com.therandomlabs.verticalendportals.frame;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.BiPredicate;
import java.util.stream.Collectors;
import com.google.common.collect.ImmutableList;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockWorldState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class Frame {
	private final World world;
	private final FrameType type;

	private final int width;
	private final int height;

	private final BlockPos topLeft;
	private final BlockPos topRight;
	private final BlockPos bottomLeft;
	private final BlockPos bottomRight;

	private final EnumFacing widthDirection;
	private final EnumFacing heightDirection;

	private final ImmutableList<BlockPos> topBlocks;
	private final ImmutableList<BlockPos> rightBlocks;
	private final ImmutableList<BlockPos> bottomBlocks;
	private final ImmutableList<BlockPos> leftBlocks;
	private final ImmutableList<BlockPos> innerBlocks;

	Frame(World world, FrameType type, Map<Integer, Corner> corners, EnumFacing[] facings) {
		this.world = world;
		this.type = type;

		final Corner topLeftCorner = corners.get(0);
		final Corner rightCorner = corners.get(1);

		width = topLeftCorner.sideLength;
		height = rightCorner.sideLength;

		topLeft = topLeftCorner.pos;
		topRight = rightCorner.pos;
		bottomLeft = corners.get(3).pos;
		bottomRight = corners.get(2).pos;

		widthDirection = facings[0];
		heightDirection = facings[1];

		final List<BlockPos> topBlocks = new ArrayList<>(width);

		for(int width = 0; width < this.width; width++) {
			topBlocks.add(
					topLeft.offset(widthDirection, width)
			);
		}

		this.topBlocks = ImmutableList.copyOf(topBlocks);

		final List<BlockPos> rightBlocks = new ArrayList<>(height);

		for(int height = 0; height < this.height; height++) {
			rightBlocks.add(
					topLeft.offset(widthDirection, width - 1).
							offset(heightDirection, height)
			);
		}

		this.rightBlocks = ImmutableList.copyOf(rightBlocks);

		final List<BlockPos> bottomBlocks = new ArrayList<>(width);

		for(int width = 0; width < this.width; width++) {
			bottomBlocks.add(
					topLeft.offset(widthDirection, width).
							offset(heightDirection, height - 1)
			);
		}

		this.bottomBlocks = ImmutableList.copyOf(bottomBlocks);

		final List<BlockPos> leftBlocks = new ArrayList<>(height);

		for(int height = 0; height < this.height; height++) {
			leftBlocks.add(
					topLeft.offset(heightDirection, height)
			);
		}

		this.leftBlocks = ImmutableList.copyOf(leftBlocks);

		final List<BlockPos> innerBlocks = new ArrayList<>((width - 2) * (height - 2));

		for(int width = 1; width < this.width - 1; width++) {
			for(int height = 1; height < this.height - 1; height++) {
				innerBlocks.add(
						topLeft.offset(widthDirection, width).
								offset(heightDirection, height)
				);
			}
		}

		this.innerBlocks = ImmutableList.copyOf(innerBlocks);
	}

	public World getWorld() {
		return world;
	}

	public FrameType getType() {
		return type;
	}

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}

	public BlockPos getTopLeft() {
		return topLeft;
	}

	public BlockPos getTopRight() {
		return topRight;
	}

	public BlockPos getBottomLeft() {
		return bottomLeft;
	}

	public BlockPos getBottomRight() {
		return bottomRight;
	}

	public EnumFacing getWidthDirection() {
		return widthDirection;
	}

	public EnumFacing getHeightDirection() {
		return heightDirection;
	}

	public boolean isCorner(BlockPos pos) {
		return topLeft.equals(pos) || topRight.equals(pos) || bottomLeft.equals(pos) ||
				bottomRight.equals(pos);
	}

	public boolean isTopBlock(BlockPos pos) {
		return isBetween(pos, topLeft, topRight);
	}

	public ImmutableList<BlockPos> getTopBlockPositions() {
		return topBlocks;
	}

	public List<IBlockState> getTopBlocks() {
		return topBlocks.stream().map(world::getBlockState).collect(Collectors.toList());
	}

	public boolean isRightBlock(BlockPos pos) {
		return isBetween(pos, topRight, bottomRight);
	}

	public ImmutableList<BlockPos> getRightBlockPositions() {
		return rightBlocks;
	}

	public List<IBlockState> getRightBlocks() {
		return rightBlocks.stream().map(world::getBlockState).collect(Collectors.toList());
	}

	public boolean isBottomBlock(BlockPos pos) {
		return isBetween(pos, bottomRight, bottomLeft);
	}

	public ImmutableList<BlockPos> getBottomBlockPositions() {
		return bottomBlocks;
	}

	public List<IBlockState> getBottomBlocks() {
		return bottomBlocks.stream().map(world::getBlockState).collect(Collectors.toList());
	}

	public boolean isLeftBlock(BlockPos pos) {
		return isBetween(pos, bottomLeft, topLeft);
	}

	public ImmutableList<BlockPos> getLeftBlockPositions() {
		return leftBlocks;
	}

	public List<IBlockState> getLeftBlocks() {
		return leftBlocks.stream().map(world::getBlockState).collect(Collectors.toList());
	}

	public boolean isInnerBlock(BlockPos pos) {
		return isBetween(pos, topLeft, bottomRight, false);
	}

	public ImmutableList<BlockPos> getInnerBlockPositions() {
		return innerBlocks;
	}

	public List<IBlockState> getInnerBlocks() {
		return innerBlocks.stream().map(world::getBlockState).collect(Collectors.toList());
	}

	public FrameSide getSide(BlockPos pos) {
		if(isTopBlock(pos)) {
			return FrameSide.TOP;
		}

		if(isRightBlock(pos)) {
			return FrameSide.RIGHT;
		}

		if(isBottomBlock(pos)) {
			return FrameSide.BOTTOM;
		}

		if(isLeftBlock(pos)) {
			return FrameSide.LEFT;
		}

		if(isInnerBlock(pos)) {
			return FrameSide.INNER;
		}

		return FrameSide.NONE;
	}

	public boolean contains(BlockPos pos) {
		return isBetween(pos, topLeft, bottomRight);
	}

	public boolean isFacingInwards(BlockPos pos, EnumFacing facing) {
		if(isCorner(pos)) {
			return false;
		}

		switch(getSide(pos)) {
		case TOP:
			return facing == heightDirection;
		case RIGHT:
			return facing == widthDirection.getOpposite();
		case BOTTOM:
			return facing == heightDirection.getOpposite();
		case LEFT:
			return facing == widthDirection;
		default:
			return false;
		}
	}

	public boolean isEmpty() {
		for(BlockPos innerPos : innerBlocks) {
			final IBlockState state = world.getBlockState(innerPos);

			if(state.getMaterial() == Material.FIRE) {
				continue;
			}

			final Block block = state.getBlock();

			if(!block.isReplaceable(world, innerPos)) {
				return false;
			}
		}

		return true;
	}

	public boolean testInnerBlocks(BiPredicate<World, BlockWorldState> predicate) {
		for(BlockPos innerPos : innerBlocks) {
			final BlockWorldState state = new BlockWorldState(world, innerPos, true);

			if(!predicate.test(world, state)) {
				return false;
			}
		}

		return true;
	}

	private boolean isBetween(BlockPos pos, BlockPos corner1, BlockPos corner2) {
		return isBetween(pos, corner1, corner2, true);
	}

	private boolean isBetween(BlockPos pos, BlockPos corner1, BlockPos corner2, boolean inclusive) {
		final int corner1X = corner1.getX();
		final int corner1Y = corner1.getY();
		final int corner1Z = corner1.getZ();

		final int corner2X = corner2.getX();
		final int corner2Y = corner2.getY();
		final int corner2Z = corner2.getZ();

		final int minX = Math.min(corner1X, corner2X);
		final int minY = Math.min(corner1Y, corner2Y);
		final int minZ = Math.min(corner1Z, corner2Z);

		final int maxX = Math.max(corner1X, corner2X);
		final int maxY = Math.max(corner1Y, corner2Y);
		final int maxZ = Math.max(corner1Z, corner2Z);

		final int x = pos.getX();
		final int y = pos.getY();
		final int z = pos.getZ();

		if(inclusive) {
			return x >= minX && y >= minY && z >= minZ && x <= maxX && y <= maxY && z <= maxZ;
		}

		return x > minX && y > minY && z > minZ && x < maxX && y < maxY && z < maxZ;
	}
}
