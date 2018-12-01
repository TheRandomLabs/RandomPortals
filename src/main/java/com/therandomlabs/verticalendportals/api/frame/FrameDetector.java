package com.therandomlabs.verticalendportals.api.frame;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;
import net.minecraft.block.state.BlockWorldState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public abstract class FrameDetector {
	public static final int UNKNOWN = 0;
	public static final int CORNER = 1;

	private static final FrameSide[] SIDES = FrameSide.values();

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

	private final Map<BlockPos, BlockWorldState> posCache = new HashMap<>();

	public final Frame detect(World world, BlockPos pos) {
		return detect(world, pos, getDefaultType());
	}

	public final Frame detect(World world, BlockPos pos, FrameType type) {
		return detect(world, pos, type, getDefaultSize());
	}

	public final Frame detect(World world, BlockPos pos, Function<FrameType, FrameSize> size) {
		return detect(world, pos, getDefaultType(), size);
	}

	public final Frame detect(World world, BlockPos pos, FrameType type,
			Function<FrameType, FrameSize> size) {
		return detectWithCondition(world, pos, type, size, frame -> true);
	}

	public final Frame detectWithCondition(World world, BlockPos pos,
			Predicate<Frame> frameCondition) {
		return detectWithCondition(world, pos, getDefaultType(), getDefaultSize(), frameCondition);
	}

	public final Frame detectWithCondition(World world, BlockPos pos, FrameType type,
			Predicate<Frame> frameCondition) {
		return detectWithCondition(world, pos, type, getDefaultSize(), frameCondition);
	}

	public final Frame detectWithCondition(World world, BlockPos pos,
			Function<FrameType, FrameSize> size, Predicate<Frame> frameCondition) {
		return detectWithCondition(world, pos, getDefaultType(), size, frameCondition);
	}

	public final Frame detectWithCondition(World world, BlockPos pos, FrameType type,
			Function<FrameType, FrameSize> size, Predicate<Frame> frameCondition) {
		final BlockWorldState state = getState(world, pos);

		if(type == FrameType.LATERAL || type == FrameType.LATERAL_OR_VERTICAL) {
			final Frame frame = detect(
					FrameType.LATERAL, LATERAL, world, state, pos, size.apply(FrameType.LATERAL),
					frameCondition
			);

			if(frame != null) {
				return frame;
			}
		}

		if(type == FrameType.VERTICAL || type == FrameType.VERTICAL_X ||
				type == FrameType.LATERAL_OR_VERTICAL) {
			final Frame frame = detect(
					FrameType.VERTICAL_X, VERTICAL_X, world, state, pos,
					size.apply(FrameType.VERTICAL_X), frameCondition
			);

			if(frame != null) {
				return frame;
			}
		}

		if(type == FrameType.VERTICAL || type == FrameType.VERTICAL_Z ||
				type == FrameType.LATERAL_OR_VERTICAL) {
			final Frame frame = detect(
					FrameType.VERTICAL_Z, VERTICAL_Z, world, state, pos,
					size.apply(FrameType.VERTICAL_Z), frameCondition
			);

			if(frame != null) {
				return frame;
			}
		}

		return null;
	}

	public FrameType getDefaultType() {
		return FrameType.LATERAL_OR_VERTICAL;
	}

	public Function<FrameType, FrameSize> getDefaultSize() {
		return type -> new FrameSize();
	}

	//If the position is unknown, 0 is used
	//Corners always have position 1 since they're always first on their side
	protected abstract boolean test(World world, FrameType type, BlockWorldState state,
			FrameSide side, int position);

	protected abstract boolean test(Frame frame);

	protected boolean testInner(World world, BlockWorldState state) {
		return true;
	}

	@SuppressWarnings("Duplicates")
	private Frame detect(FrameType type, EnumFacing[] facings, World world, BlockWorldState state,
			BlockPos pos, FrameSize size, Predicate<Frame> frameCondition) {
		for(int index = 0; index < 4; index++) {
			final FrameSide side = SIDES[index];

			if(!test(world, type, state, side, 0)) {
				continue;
			}

			final int previousIndex = index == 0 ? 3 : index - 1;

			final EnumFacing facing = facings[index];
			final EnumFacing previousFacing = facings[previousIndex].getOpposite();

			final EnumFacing opposite = facing.getOpposite();

			final FrameSide previousSide = SIDES[previousIndex];

			//Get the frame most opposite to the detection direction
			//If this is a lateral top frame, then we're looking east first,
			//so we find the westmost corner

			final int maxLength = index % 2 == 0 ? size.maxWidth : size.maxHeight;

			final List<BlockPos> possibleCorners = new ArrayList<>();
			BlockPos checkPos = pos;
			BlockWorldState checkState;

			int length = 1;

			do {
				length++;

				checkPos = checkPos.offset(opposite);
				checkState = getState(world, checkPos);
				final BlockWorldState checkState2 =
						getState(world, checkPos.offset(previousFacing));

				//For example, if this is a lateral frame and facing is EAST (i.e. the top side),
				//we look for the top-left corner (which has position 1)
				//The length of the previous side is unknown, so we pass in 0 for position
				if(test(world, type, checkState, side, 1) &&
						test(world, type, checkState2, previousSide, UNKNOWN)) {
					possibleCorners.add(checkPos);
				} else if(!testInner(world, checkState2)) {
					//Then checkState2 is an inner block
					break;
				}

				if(length == maxLength) {
					break;
				}
			} while(test(world, type, checkState, side, UNKNOWN));

			if(possibleCorners.isEmpty()) {
				continue;
			}

			for(BlockPos possibleCorner : possibleCorners) {
				final HashMap<Integer, Corner> corners = new HashMap<>();

				corners.put(index, new Corner(possibleCorner, 0));

				final Frame frame = detect(
						corners, type, facings, world, possibleCorner, size.minWidth, size.maxWidth,
						size.minHeight, size.maxHeight, index, index, frameCondition
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

	@SuppressWarnings("Duplicates")
	private Frame detect(HashMap<Integer, Corner> corners, FrameType type, EnumFacing[] facings,
			World world, BlockPos pos, int minWidth, int maxWidth, int minHeight, int maxHeight,
			int startIndex, int index, Predicate<Frame> frameCondition) {
		final int actualIndex = index > 3 ? index - 4 : index;
		final int nextIndex = actualIndex == 3 ? 0 : actualIndex + 1;

		final EnumFacing facing = facings[actualIndex];
		final EnumFacing nextFacing = facings[nextIndex];

		final FrameSide side = SIDES[actualIndex];
		final FrameSide nextSide = SIDES[nextIndex];

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

		//Possible corner, current side length if corner is valid
		final List<Map.Entry<BlockPos, Integer>> possibleCorners = new ArrayList<>();
		BlockPos checkPos = pos;
		BlockWorldState checkState;

		int length = 1;

		//Since the corner block and the second block is tested in the following while loop,
		//but only the corner block is tested in the first detection
		if(index != startIndex) {
			length++;
			checkPos = checkPos.offset(facing);
		}

		do {
			length++;

			checkPos = checkPos.offset(facing);
			checkState = getState(world, checkPos);

			final BlockWorldState checkState2 = getState(world, checkPos.offset(nextFacing));

			//For example, if this is a lateral frame and facing is EAST (i.e. the top side),
			//we look for the top-right corner (which has position 1 on the right side)
			//We're testing for the second block on the next side (i.e. position 2)
			if(length >= minLength &&
					test(world, type, checkState, nextSide, CORNER) &&
					test(world, type, checkState2, nextSide, 2)) {
				possibleCorners.add(new AbstractMap.SimpleEntry<>(checkPos, length));
			} else if(!testInner(world, checkState2)) {
				//Then checkState2 is an inner block
				break;
			}

			if(length == maxLength) {
				break;
			}
		} while(test(world, type, checkState, side, length));

		if(possibleCorners.isEmpty()) {
			return null;
		}

		//There should only be one possible corner by this time since the length is already known

		if(nextIndex == startIndex) {
			corners.get(actualIndex).sideLength = possibleCorners.get(0).getValue();
			final Frame frame = new Frame(this, world, type, corners, facings);
			return test(frame) && frameCondition.test(frame) ? frame : null;
		}

		for(Map.Entry<BlockPos, Integer> corner : possibleCorners) {
			final BlockPos cornerPos = corner.getKey();

			corners.get(actualIndex).sideLength = corner.getValue();
			corners.put(nextIndex, new Corner(cornerPos, 0));

			final Frame frame = detect(
					corners, type, facings, world, cornerPos, minWidth, maxWidth, minHeight,
					maxHeight, startIndex, index + 1, frameCondition
			);

			if(frame != null) {
				return frame;
			}
		}

		return null;
	}

	private BlockWorldState getState(World world, BlockPos pos) {
		return posCache.computeIfAbsent(pos, key -> new BlockWorldState(world, key, true));
	}
}
