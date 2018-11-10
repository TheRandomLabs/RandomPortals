package com.therandomlabs.verticalendportals.util;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import com.google.common.base.Joiner;
import net.minecraft.block.state.BlockWorldState;

public class BlockPatternFactory {
	private static final Joiner COMMA_JOIN = Joiner.on(",");

	private final List<String[]> depth = new ArrayList<>();
	private final Map<Character, Predicate<BlockWorldState>> symbolMap = new HashMap<>();

	private int aisleHeight;
	private int rowWidth;

	private BlockPatternFactory() {
		symbolMap.put(' ', state -> true);
	}

	public BlockPatternFactory aisle(String... aisle) {
		if(aisle.length == 0 || aisle[0].isEmpty()) {
			throw new IllegalArgumentException("Empty pattern for aisle");
		}

		if(depth.isEmpty()) {
			aisleHeight = aisle.length;
			rowWidth = aisle[0].length();
		}

		if(aisle.length != aisleHeight) {
			throw new IllegalArgumentException(
					"Expected aisle with height of " + aisleHeight +
							", but was given one with a height of " + aisle.length + ")"
			);
		}

		for(String string : aisle) {
			if(string.length() != rowWidth) {
				throw new IllegalArgumentException(
						"Not all rows in the given aisle are the correct width (expected " +
								rowWidth + ", found one with " + string.length() + ")");
			}

			for(char character : string.toCharArray()) {
				if(!symbolMap.containsKey(character)) {
					symbolMap.put(character, null);
				}
			}
		}

		depth.add(aisle);
		return this;
	}

	public BlockPatternFactory where(char symbol, Predicate<BlockWorldState> blockMatcher) {
		symbolMap.put(symbol, blockMatcher);
		return this;
	}

	public BlockPattern build() {
		return new BlockPattern(makePredicateArray());
	}

	private Predicate<BlockWorldState>[][][] makePredicateArray() {
		checkMissingPredicates();

		@SuppressWarnings("unchecked")
		final Predicate<BlockWorldState>[][][] predicate = (Predicate[][][]) Array.newInstance(
				Predicate.class, depth.size(), aisleHeight, rowWidth
		);

		for(int i = 0; i < depth.size(); ++i) {
			for(int j = 0; j < aisleHeight; ++j) {
				for(int k = 0; k < rowWidth; ++k) {
					predicate[i][j][k] = symbolMap.get(depth.get(i)[j].charAt(k));
				}
			}
		}

		return predicate;
	}

	private void checkMissingPredicates() {
		final List<Character> characters = new ArrayList<>();

		for(Map.Entry<Character, Predicate<BlockWorldState>> entry : symbolMap.entrySet()) {
			if(entry.getValue() == null) {
				characters.add(entry.getKey());
			}
		}

		if(!characters.isEmpty()) {
			throw new IllegalStateException(
					"Predicates for character(s) " + COMMA_JOIN.join(characters) + " are missing"
			);
		}
	}

	public static BlockPatternFactory start() {
		return new BlockPatternFactory();
	}
}
