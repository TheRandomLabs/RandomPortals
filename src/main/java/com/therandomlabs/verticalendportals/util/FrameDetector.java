package com.therandomlabs.verticalendportals.util;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import com.google.common.collect.ImmutableList;
import net.minecraft.block.state.BlockWorldState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.apache.commons.lang3.SerializationUtils;

public class FrameDetector {
	public enum Type {
		LATERAL,
		VERTICAL,
		VERTICAL_X,
		VERTICAL_Z,
		LATERAL_OR_VERTICAL
	}

	public static class Frame {
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

		private Frame(Map<Integer, Corner> corners, EnumFacing[] facings) {
			final Corner topLeftCorner = corners.get(0);
			final Corner rightCorner = corners.get(1);

			width = topLeftCorner.sideLength;
			height = rightCorner.sideLength;

			topLeft = topLeftCorner.pos();
			topRight = rightCorner.pos();
			bottomLeft = corners.get(3).pos();
			bottomRight = corners.get(2).pos();

			widthDirection = facings[0];
			heightDirection = facings[1];

			final List<BlockPos> topBlocks = new ArrayList<>(width);

			for(int width = 1; width < this.width - 1; width++) {
				topBlocks.add(
						topLeft.offset(widthDirection, width)
				);
			}

			this.topBlocks = ImmutableList.copyOf(topBlocks);

			final List<BlockPos> rightBlocks = new ArrayList<>(height);

			for(int height = 1; height < this.height - 1; height++) {
				rightBlocks.add(
						topLeft.offset(widthDirection, width - 1).
								offset(heightDirection, height)
				);
			}

			this.rightBlocks = ImmutableList.copyOf(rightBlocks);

			final List<BlockPos> bottomBlocks = new ArrayList<>(width);

			for(int width = 1; width < this.width - 1; width++) {
				bottomBlocks.add(
						topLeft.offset(widthDirection, width).
								offset(heightDirection, height - 1)
				);
			}

			this.bottomBlocks = ImmutableList.copyOf(bottomBlocks);

			final List<BlockPos> leftBlocks = new ArrayList<>(height);

			for(int height = 1; height < this.height - 1; height++) {
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

		public boolean isLateral() {
			return heightDirection == EnumFacing.SOUTH;
		}

		public boolean isVertical() {
			return heightDirection == EnumFacing.DOWN;
		}

		public boolean isVerticalX() {
			return widthDirection == EnumFacing.EAST && heightDirection == EnumFacing.DOWN;
		}

		public boolean isVerticalY() {
			return widthDirection == EnumFacing.NORTH && heightDirection == EnumFacing.DOWN;
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

		public ImmutableList<BlockPos> getTopBlocks() {
			return topBlocks;
		}

		public ImmutableList<BlockPos> getRightBlocks() {
			return rightBlocks;
		}

		public ImmutableList<BlockPos> getBottomBlocks() {
			return bottomBlocks;
		}

		public ImmutableList<BlockPos> getLeftBlocks() {
			return leftBlocks;
		}

		public ImmutableList<BlockPos> getInnerBlocks() {
			return innerBlocks;
		}
	}

	private static class Corner implements Serializable {
		private static final long serialVersionUID = 6688254550627695183L;

		int x;
		int y;
		int z;
		int sideLength;

		Corner(BlockPos pos) {
			x = pos.getX();
			y = pos.getY();
			z = pos.getZ();
		}

		BlockPos pos() {
			return new BlockPos(x, y, z);
		}
	}

	//Test from West->East (top), then South->North (right), then West->East (bottom),
	//then South->North (left)

	private static final EnumFacing[] LATERAL = {
			EnumFacing.EAST,
			EnumFacing.SOUTH,
			EnumFacing.WEST,
			EnumFacing.NORTH
	};

	//Test from West->East (top), then Down->Up (right), then West->East (bottom),
	//then Down->Up(left)

	private static final EnumFacing[] VERTICAL_X = {
			EnumFacing.EAST,
			EnumFacing.DOWN,
			EnumFacing.WEST,
			EnumFacing.UP
	};

	//Test from South->North (top), then Down->Up (right), then South->North (bottom),
	//then Down->Up(left)

	private static final EnumFacing[] VERTICAL_Z = {
			EnumFacing.NORTH,
			EnumFacing.DOWN,
			EnumFacing.SOUTH,
			EnumFacing.UP
	};

	private final Type type;

	private final List<Predicate<BlockWorldState>> sidePredicates;
	private final Predicate<BlockWorldState> cornerPredicate;

	private final Predicate<Frame> framePredicate;

	private final Map<BlockPos, BlockWorldState> posCache = new HashMap<>();

	public FrameDetector(Type type, Predicate<BlockWorldState> sidePredicate,
			Predicate<BlockWorldState> cornerPredicate, Predicate<Frame> framePredicate) {
		this(
				type, sidePredicate, sidePredicate, sidePredicate, sidePredicate, cornerPredicate,
				framePredicate
		);
	}

	public FrameDetector(Type type, Predicate<BlockWorldState> topPredicate,
			Predicate<BlockWorldState> rightPredicate, Predicate<BlockWorldState> bottomPredicate,
			Predicate<BlockWorldState> leftPredicate, Predicate<BlockWorldState> cornerPredicate,
			Predicate<Frame> framePredicate) {
		this.type = type;

		sidePredicates = ImmutableList.of(
				topPredicate,
				rightPredicate,
				bottomPredicate,
				leftPredicate
		);

		this.cornerPredicate = cornerPredicate;

		this.framePredicate = framePredicate;
	}

	public Frame detect(World world, BlockPos pos, int minWidth, int maxWidth, int minHeight,
			int maxHeight) {
		if(minWidth < 3 || minHeight < 3) {
			throw new IllegalArgumentException(
					"Portal frames must be at least 3 blocks in width and height"
			);
		}

		final BlockWorldState state = getState(world, pos);

		if(type == Type.LATERAL || type == Type.LATERAL_OR_VERTICAL) {
			final Frame frame = detect(
					LATERAL, world, state, pos, minWidth, maxWidth, minHeight, maxHeight
			);

			if(frame != null) {
				return frame;
			}
		}

		if(type == Type.VERTICAL || type == Type.VERTICAL_X) {
			final Frame frame = detect(
					VERTICAL_X, world, state, pos, minWidth, maxWidth, minHeight, maxHeight
			);

			if(frame != null) {
				return frame;
			}
		}

		if(type == Type.VERTICAL || type == Type.VERTICAL_Z) {
			final Frame frame = detect(
					VERTICAL_Z, world, state, pos, minWidth, maxWidth, minHeight, maxHeight
			);

			if(frame != null) {
				return frame;
			}
		}

		return null;
	}

	private Frame detect(EnumFacing[] facings, World world, BlockWorldState state, BlockPos pos,
			int minWidth, int maxWidth, int minHeight, int maxHeight) {
		for(int index = 0; index < 4; index++) {
			final Predicate<BlockWorldState> predicate = sidePredicates.get(index);

			if(!predicate.test(state)) {
				continue;
			}

			final int previousIndex = index == 0 ? 3 : index - 1;

			final EnumFacing facing = facings[index];
			final EnumFacing previousFacing = facings[previousIndex].getOpposite();

			final EnumFacing opposite = facing.getOpposite();

			final Predicate<BlockWorldState> previousPredicate = sidePredicates.get(previousIndex);

			//Get the frame most opposite to the detection direction
			//If this is a lateral top frame, then we're looking east first,
			//so we find the westmost frame

			final int maxLength = index % 2 == 0 ? maxWidth : maxHeight;

			final List<BlockPos> possibleCorners = new ArrayList<>();
			BlockPos checkPos = pos;
			BlockWorldState checkState;

			//Compensate for two corner blocks (length is incremented at least once in the loop)
			int length = 1;

			do {
				length++;

				checkPos = checkPos.offset(opposite);
				checkState = getState(world, checkPos);

				if(previousPredicate.test(getState(world, checkPos.offset(previousFacing)))) {
					possibleCorners.add(checkPos);
				}

				if(length == maxLength) {
					break;
				}
			} while(predicate.test(checkState));

			if(possibleCorners.isEmpty() || !cornerPredicate.test(checkState)) {
				continue;
			}

			for(BlockPos possibleCorner : possibleCorners) {
				final HashMap<Integer, Corner> corners = new HashMap<>();

				corners.put(index, new Corner(possibleCorner));

				final Frame frame = detect(
						corners, facings, world, possibleCorner, minWidth, maxWidth, minHeight,
						maxHeight, index, index
				);

				if(frame != null) {
					posCache.clear();
					return frame;
				}
			}
		}

		posCache.clear();
		return null;
	}

	private Frame detect(HashMap<Integer, Corner> corners, EnumFacing[] facings, World world,
			BlockPos pos, int minWidth, int maxWidth, int minHeight, int maxHeight, int startIndex,
			int index) {
		final int actualIndex = index > 3 ? index - 4 : index;
		final int nextIndex = actualIndex == 3 ? 0 : actualIndex + 1;

		final EnumFacing facing = facings[actualIndex];
		final EnumFacing nextFacing = facings[nextIndex];

		final Predicate<BlockWorldState> predicate = sidePredicates.get(actualIndex);
		final Predicate<BlockWorldState> nextPredicate = sidePredicates.get(nextIndex);

		//Find the minimum and maximum length of this side

		final int minLength;
		final int maxLength;

		if(index == startIndex || index == startIndex + 1) {
			if(index % 2 == 0) {
				minLength = minWidth;
				maxLength = maxWidth;
			} else {
				minLength = minHeight;
				maxLength = maxHeight;
			}
		} else {
			final Corner corner = corners.get(index == 6 ? 0 : index - 2);
			minLength = corner.sideLength;
			maxLength = corner.sideLength;
		}

		//Find the other end of the side, i.e. the next corner

		final List<BlockPos> possibleCorners = new ArrayList<>();
		BlockPos checkPos = pos;
		BlockWorldState checkState;

		//Compensate for two corner blocks (length is incremented at least once in the loop)
		int length = 1;

		do {
			length++;

			checkPos = checkPos.offset(facing);
			checkState = getState(world, checkPos);

			if(length >= minLength &&
					nextPredicate.test(getState(world, checkPos.offset(nextFacing)))) {
				possibleCorners.add(checkPos);
			}

			if(length == maxLength) {
				break;
			}
		} while(predicate.test(checkState));

		if(possibleCorners.isEmpty() || !cornerPredicate.test(checkState)) {
			return null;
		}

		corners.get(actualIndex).sideLength = length;

		if(nextIndex == startIndex) {
			final Frame frame = new Frame(
					corners,
					facings
			);

			return framePredicate.test(frame) ? frame : null;
		}

		if(possibleCorners.size() == 1) {
			final BlockPos cornerPos = possibleCorners.get(0);

			corners.put(nextIndex, new Corner(cornerPos));

			final Frame frame = detect(
					corners, facings, world, cornerPos,
					minWidth, maxWidth, minHeight, maxHeight, startIndex, index + 1
			);

			if(frame != null) {
				return frame;
			}
		} else {
			for(BlockPos cornerPos : possibleCorners) {
				corners.put(nextIndex, new Corner(cornerPos));

				final Frame frame = detect(
						SerializationUtils.clone(corners), facings, world, cornerPos,
						minWidth, maxWidth, minHeight, maxHeight, startIndex, index + 1
				);

				if(frame != null) {
					return frame;
				}
			}
		}

		return null;
	}

	private BlockWorldState getState(World world, BlockPos pos) {
		return posCache.computeIfAbsent(pos, key -> new BlockWorldState(world, key, true));
	}
}
