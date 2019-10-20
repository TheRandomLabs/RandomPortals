package com.therandomlabs.randomportals.api.frame;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.therandomlabs.randomportals.api.util.FrameStatePredicate;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.DimensionType;
import net.minecraft.world.World;

public class Frame {
	private final World world;
	private final DimensionType dimensionType;

	private final FrameType type;

	private final int width;
	private final int height;
	private final int size;

	private final EnumFacing widthDirection;
	private final EnumFacing heightDirection;

	private final BlockPos topLeft;
	private final BlockPos topRight;
	private final BlockPos bottomLeft;
	private final BlockPos bottomRight;

	private ImmutableList<BlockPos> topBlocks;
	private ImmutableList<BlockPos> rightBlocks;
	private ImmutableList<BlockPos> bottomBlocks;
	private ImmutableList<BlockPos> leftBlocks;

	private ImmutableList<BlockPos> frameBlocks;
	private ImmutableList<BlockPos> innerBlocks;

	public Frame(World world, FrameType type, BlockPos topLeft, int width, int height) {
		this(world, type, topLeft, null, null, null, width, height);
	}

	Frame(World world, FrameType type, Map<Integer, FrameDetector.Corner> corners) {
		this(
				world, type, corners.get(0).pos, corners.get(1).pos, corners.get(3).pos,
				corners.get(2).pos, corners.get(0).sideLength, corners.get(1).sideLength
		);
	}

	private Frame(
			World world, FrameType type, BlockPos topLeft, BlockPos topRight,
			BlockPos bottomLeft, BlockPos bottomRight, int width, int height
	) {
		if (width < 3 || height < 3) {
			throw new IllegalArgumentException("Frame cannot be smaller than 3 blocks");
		}

		this.world = world;
		dimensionType = world.provider.getDimensionType();

		this.type = type;

		this.width = width;
		this.height = height;
		size = width * height;

		widthDirection = type.getWidthDirection();
		heightDirection = type.getHeightDirection();

		this.topLeft = topLeft;

		if (topRight == null) {
			this.topRight = topLeft.offset(widthDirection, width - 1);
			this.bottomLeft = topLeft.offset(heightDirection, height - 1);
			this.bottomRight = this.bottomLeft.offset(widthDirection, width - 1);
		} else {
			this.topRight = topRight;
			this.bottomLeft = bottomLeft;
			this.bottomRight = bottomRight;
		}
	}

	@Override
	public int hashCode() {
		return topLeft.hashCode() * width * height * type.hashCode();
	}

	@Override
	public boolean equals(Object object) {
		if (this == object) {
			return true;
		}

		if (!(object instanceof Frame)) {
			return false;
		}

		final Frame frame = (Frame) object;
		return topLeft.equals(frame.topLeft) && width == frame.width && height == frame.height &&
				type == frame.type;
	}

	@Override
	public String toString() {
		return "Frame[topLeft=" + topLeft + ",topRight=" + topRight + ",bottomLeft=" + bottomLeft +
				",bottomRight=" + bottomRight + ",type=" + type + "]";
	}

	public World getWorld() {
		return world;
	}

	public DimensionType getDimensionType() {
		return dimensionType;
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

	public int getSize() {
		return size;
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

	public ImmutableList<BlockPos> getCornerBlockPositions() {
		return ImmutableList.of(topLeft, topRight, bottomLeft, bottomRight);
	}

	public List<IBlockState> getCornerBlocks() {
		return Lists.newArrayList(
				world.getBlockState(topLeft),
				world.getBlockState(topRight),
				world.getBlockState(bottomLeft),
				world.getBlockState(bottomRight)
		);
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

	public boolean isTopBlock(BlockPos pos) {
		return isBetween(pos, topLeft, topRight);
	}

	public ImmutableList<BlockPos> getTopBlockPositions() {
		if (topBlocks == null) {
			topBlocks = getPositions(true, false);
		}

		return topBlocks;
	}

	public List<IBlockState> getTopBlocks() {
		return getTopBlockPositions().stream().map(world::getBlockState).
				collect(Collectors.toList());
	}

	public boolean isRightBlock(BlockPos pos) {
		return isBetween(pos, topRight, bottomRight);
	}

	public ImmutableList<BlockPos> getRightBlockPositions() {
		if (rightBlocks == null) {
			rightBlocks = getPositions(false, true);
		}

		return rightBlocks;
	}

	public List<IBlockState> getRightBlocks() {
		return getRightBlockPositions().stream().map(world::getBlockState).
				collect(Collectors.toList());
	}

	public boolean isBottomBlock(BlockPos pos) {
		return isBetween(pos, bottomRight, bottomLeft);
	}

	public ImmutableList<BlockPos> getBottomBlockPositions() {
		if (bottomBlocks == null) {
			bottomBlocks = getPositions(true, true);
		}

		return bottomBlocks;
	}

	public List<IBlockState> getBottomBlocks() {
		return getBottomBlockPositions().stream().map(world::getBlockState).
				collect(Collectors.toList());
	}

	public boolean isLeftBlock(BlockPos pos) {
		return isBetween(pos, bottomLeft, topLeft);
	}

	public ImmutableList<BlockPos> getLeftBlockPositions() {
		if (leftBlocks == null) {
			leftBlocks = getPositions(false, false);
		}

		return leftBlocks;
	}

	public List<IBlockState> getLeftBlocks() {
		return getLeftBlockPositions().stream().map(world::getBlockState).
				collect(Collectors.toList());
	}

	public boolean isFrameBlock(BlockPos pos) {
		return isBetween(pos, topLeft, bottomRight) && !isInnerBlock(pos);
	}

	public ImmutableList<BlockPos> getFrameBlockPositions() {
		if (frameBlocks == null) {
			getTopBlockPositions();
			getRightBlockPositions();
			getBottomBlockPositions();
			getLeftBlockPositions();

			//Each corner is in two lists, so we use a set to remove duplicates
			//The order should be preserved so frames can be modified consistently
			final Set<BlockPos> frameBlocks = new LinkedHashSet<>(
					topBlocks.size() + rightBlocks.size() + bottomBlocks.size() +
							leftBlocks.size() - 4
			);

			frameBlocks.addAll(topBlocks);
			frameBlocks.addAll(rightBlocks);
			frameBlocks.addAll(bottomBlocks);
			frameBlocks.addAll(leftBlocks);

			this.frameBlocks = ImmutableList.copyOf(frameBlocks);
		}

		return frameBlocks;
	}

	public List<IBlockState> getFrameBlocks() {
		return getFrameBlockPositions().stream().map(world::getBlockState).
				collect(Collectors.toList());
	}

	public boolean isInnerBlock(BlockPos pos) {
		return isBetween(pos, topLeft, bottomRight, false);
	}

	public ImmutableList<BlockPos> getInnerBlockPositions() {
		if (innerBlocks == null) {
			final List<BlockPos> innerBlocks = new ArrayList<>((width - 2) * (height - 2));

			for (int widthOffset = 1; widthOffset < width - 1; widthOffset++) {
				for (int heightOffset = 1; heightOffset < height - 1; heightOffset++) {
					innerBlocks.add(
							topLeft.offset(widthDirection, widthOffset).
									offset(heightDirection, heightOffset)
					);
				}
			}

			this.innerBlocks = ImmutableList.copyOf(innerBlocks);
		}

		return innerBlocks;
	}

	public List<IBlockState> getInnerBlocks() {
		return getInnerBlockPositions().stream().map(world::getBlockState).
				collect(Collectors.toList());
	}

	public List<BlockPos> getInnerRow(int row) {
		if (row < 1 || row > height - 2) {
			throw new IllegalArgumentException(String.format(
					"Invalid row %s for frame with width %s and height %s", row, width, height
			));
		}

		return getPositions(
				topLeft.offset(widthDirection), width - 2, widthDirection, row, heightDirection
		);
	}

	public List<BlockPos> getInnerRowFromBottom(int row) {
		return getInnerRow(height - 1 - row);
	}

	public List<BlockPos> getInnerColumn(int column) {
		if (column < 1 || column > width - 2) {
			throw new IllegalArgumentException(String.format(
					"Invalid column %s for frame with width %s and height %s", column, width,
					height
			));
		}

		return getPositions(
				topLeft.offset(heightDirection), height - 2, heightDirection, column,
				widthDirection
		);
	}

	public List<BlockPos> getInnerColumnFromBottom(int column) {
		return getInnerColumn(width - 1 - column);
	}

	public FrameSide getSide(BlockPos pos) {
		if (isCorner(pos)) {
			return FrameSide.CORNER;
		}

		if (isTopBlock(pos)) {
			return FrameSide.TOP;
		}

		if (isRightBlock(pos)) {
			return FrameSide.RIGHT;
		}

		if (isBottomBlock(pos)) {
			return FrameSide.BOTTOM;
		}

		if (isLeftBlock(pos)) {
			return FrameSide.LEFT;
		}

		if (isInnerBlock(pos)) {
			return FrameSide.INNER;
		}

		return FrameSide.NONE;
	}

	public boolean contains(BlockPos pos) {
		return isBetween(pos, topLeft, bottomRight);
	}

	public boolean isFacingInwards(BlockPos pos, EnumFacing facing) {
		switch (getSide(pos)) {
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
		for (BlockPos innerPos : getInnerBlockPositions()) {
			final IBlockState state = world.getBlockState(innerPos);

			if (state.getMaterial() == Material.FIRE) {
				continue;
			}

			final Block block = state.getBlock();

			if (!block.isReplaceable(world, innerPos)) {
				return false;
			}
		}

		return true;
	}

	public boolean testInnerBlocks(FrameStatePredicate predicate) {
		for (BlockPos innerPos : getInnerBlockPositions()) {
			if (!predicate.test(world, innerPos, world.getBlockState(innerPos), type)) {
				return false;
			}
		}

		return true;
	}

	private boolean isBetween(BlockPos pos, BlockPos corner1, BlockPos corner2) {
		return isBetween(pos, corner1, corner2, true);
	}

	private boolean isBetween(BlockPos pos, BlockPos corner1, BlockPos corner2,
			boolean inclusive) {
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

		if (inclusive) {
			return x >= minX && y >= minY && z >= minZ && x <= maxX && y <= maxY && z <= maxZ;
		}

		//Because the frame is only on one axis

		switch (type.getAxis()) {
		case X:
			return x > minX && y > minY && x < maxX && y < maxY && z == minZ;
		case Y:
			return x > minX && z > minZ && x < maxX && z < maxZ && y == minY;
		default:
			return y > minY && z > minZ && y < maxY && z < maxZ && x == minX;
		}
	}

	private ImmutableList<BlockPos> getPositions(boolean width, boolean offsetBoth) {
		if (width) {
			return getPositions(
					topLeft, this.width, widthDirection, offsetBoth ? height - 1 : 0,
					heightDirection
			);
		}

		return getPositions(
				topLeft, height, heightDirection, offsetBoth ? this.width - 1 : 0, widthDirection
		);
	}

	private ImmutableList<BlockPos> getPositions(
			BlockPos startingPos, int maxOffset,
			EnumFacing offsetDirection, int otherOffset, EnumFacing otherOffsetDirection
	) {
		final List<BlockPos> positions = new ArrayList<>(maxOffset);
		final BlockPos toOffset = startingPos.offset(otherOffsetDirection, otherOffset);

		positions.add(toOffset);

		for (int offset = 1; offset < maxOffset; offset++) {
			positions.add(toOffset.offset(offsetDirection, offset));
		}

		return ImmutableList.copyOf(positions);
	}
}
