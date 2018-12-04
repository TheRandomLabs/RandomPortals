package com.therandomlabs.verticalendportals.api.frame;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.therandomlabs.verticalendportals.api.util.StatePredicate;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class Frame {
	private final FrameType type;
	private final int width;
	private final int height;
	private final EnumFacing widthDirection;
	private final EnumFacing heightDirection;
	private final BlockPos topLeft;
	private final BlockPos topRight;
	private final BlockPos bottomLeft;
	private final BlockPos bottomRight;
	private World world;
	private ImmutableList<BlockPos> topBlocks;
	private ImmutableList<BlockPos> rightBlocks;
	private ImmutableList<BlockPos> bottomBlocks;
	private ImmutableList<BlockPos> leftBlocks;
	private ImmutableList<BlockPos> frameBlocks;
	private ImmutableList<BlockPos> innerBlocks;

	public Frame(World world, FrameType type, BlockPos topLeft, int width, int height) {
		this(world, type, topLeft, null, null, null, width, height);
	}

	Frame(World world, FrameType type, Map<Integer, Corner> corners) {
		this(
				world, type, corners.get(0).pos, corners.get(1).pos, corners.get(3).pos,
				corners.get(2).pos, corners.get(0).sideLength, corners.get(1).sideLength
		);
	}

	private Frame(World world, FrameType type, BlockPos topLeft, BlockPos topRight,
			BlockPos bottomLeft, BlockPos bottomRight, int width, int height) {
		this.world = world;
		this.type = type;

		this.width = width;
		this.height = height;

		widthDirection = type.rightDownLeftUp[0];
		heightDirection = type.rightDownLeftUp[1];

		this.topLeft = topLeft;

		if(topRight == null) {
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
	public String toString() {
		return "Frame[topLeft=" + topLeft + ",topRight=" + topRight + ",bottomLeft=" + bottomLeft +
				",bottomRight=" + bottomRight + ",type=" + type + "]";
	}

	public World getWorld() {
		return world;
	}

	public void setWorld(World world) {
		this.world = world;
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
		if(world == null) {
			return Collections.emptyList();
		}

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

	@SuppressWarnings("Duplicates")
	public ImmutableList<BlockPos> getTopBlockPositions() {
		if(topBlocks == null) {
			final List<BlockPos> topBlocks = new ArrayList<>(width);

			for(int offset = 0; offset < width; offset++) {
				topBlocks.add(
						topLeft.offset(widthDirection, offset)
				);
			}

			this.topBlocks = ImmutableList.copyOf(topBlocks);
		}

		return topBlocks;
	}

	public List<IBlockState> getTopBlocks() {
		if(world == null) {
			return Collections.emptyList();
		}

		return getTopBlockPositions().stream().map(world::getBlockState).
				collect(Collectors.toList());
	}

	public boolean isRightBlock(BlockPos pos) {
		return isBetween(pos, topRight, bottomRight);
	}

	public ImmutableList<BlockPos> getRightBlockPositions() {
		if(rightBlocks == null) {
			final List<BlockPos> rightBlocks = new ArrayList<>(height);

			for(int offset = 0; offset < height; offset++) {
				rightBlocks.add(
						topLeft.offset(widthDirection, width - 1).
								offset(heightDirection, offset)
				);
			}

			this.rightBlocks = ImmutableList.copyOf(rightBlocks);
		}

		return rightBlocks;
	}

	public List<IBlockState> getRightBlocks() {
		if(world == null) {
			return Collections.emptyList();
		}

		return getRightBlockPositions().stream().map(world::getBlockState).
				collect(Collectors.toList());
	}

	public boolean isBottomBlock(BlockPos pos) {
		return isBetween(pos, bottomRight, bottomLeft);
	}

	public ImmutableList<BlockPos> getBottomBlockPositions() {
		if(bottomBlocks == null) {
			final List<BlockPos> bottomBlocks = new ArrayList<>(width);

			for(int offset = 0; offset < width; offset++) {
				bottomBlocks.add(
						topLeft.offset(widthDirection, offset).
								offset(heightDirection, height - 1)
				);
			}

			this.bottomBlocks = ImmutableList.copyOf(bottomBlocks);
		}

		return bottomBlocks;
	}

	public List<IBlockState> getBottomBlocks() {
		if(world == null) {
			return Collections.emptyList();
		}

		return getBottomBlockPositions().stream().map(world::getBlockState).
				collect(Collectors.toList());
	}

	public boolean isLeftBlock(BlockPos pos) {
		return isBetween(pos, bottomLeft, topLeft);
	}

	@SuppressWarnings("Duplicates")
	public ImmutableList<BlockPos> getLeftBlockPositions() {
		if(leftBlocks == null) {
			final List<BlockPos> leftBlocks = new ArrayList<>(height);

			for(int offset = 0; offset < height; offset++) {
				leftBlocks.add(
						topLeft.offset(heightDirection, offset)
				);
			}

			this.leftBlocks = ImmutableList.copyOf(leftBlocks);
		}

		return leftBlocks;
	}

	public List<IBlockState> getLeftBlocks() {
		if(world == null) {
			return Collections.emptyList();
		}

		return getLeftBlockPositions().stream().map(world::getBlockState).
				collect(Collectors.toList());
	}

	public boolean isFrameBlock(BlockPos pos) {
		return isBetween(pos, topLeft, bottomRight) && !isInnerBlock(pos);
	}

	public ImmutableList<BlockPos> getFrameBlockPositions() {
		if(frameBlocks == null) {
			getTopBlockPositions();
			getRightBlockPositions();
			getBottomBlockPositions();
			getLeftBlockPositions();

			//Each corner is in two lists, so we use a set to remove duplicates
			final Set<BlockPos> frameBlocks = new HashSet<>(
					topBlocks.size() + rightBlocks.size() + bottomBlocks.size() + leftBlocks.size()
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
		if(world == null) {
			return Collections.emptyList();
		}

		return getFrameBlockPositions().stream().map(world::getBlockState).
				collect(Collectors.toList());
	}

	public boolean isInnerBlock(BlockPos pos) {
		return isBetween(pos, topLeft, bottomRight, false);
	}

	public ImmutableList<BlockPos> getInnerBlockPositions() {
		if(innerBlocks == null) {
			final List<BlockPos> innerBlocks = new ArrayList<>((width - 2) * (height - 2));

			for(int widthOffset = 1; widthOffset < width - 1; widthOffset++) {
				for(int heightOffset = 1; heightOffset < height - 1; heightOffset++) {
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
		if(world == null) {
			return Collections.emptyList();
		}

		return getInnerBlockPositions().stream().map(world::getBlockState).
				collect(Collectors.toList());
	}

	public FrameSide getSide(BlockPos pos) {
		if(isCorner(pos)) {
			return FrameSide.CORNER;
		}

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
		if(world == null) {
			return false;
		}

		for(BlockPos innerPos : getInnerBlockPositions()) {
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

	public boolean testInnerBlocks(StatePredicate predicate) {
		if(world == null) {
			return false;
		}

		for(BlockPos innerPos : getInnerBlockPositions()) {
			if(!predicate.test(world, innerPos, world.getBlockState(innerPos))) {
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

		//Because the frame is only on one axis

		switch(type.getAxis()) {
		case X:
			return x > minX && y > minY && x < maxX && y < maxY && z == minZ;
		case Y:
			return x > minX && z > minZ && x < maxX && z < maxZ && y == minY;
		default:
			return y > minY && z > minZ && y < maxY && z < maxZ && x == minX;
		}
	}
}
