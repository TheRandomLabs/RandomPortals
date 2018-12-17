package com.therandomlabs.randomportals.api.config;

import java.util.Locale;
import java.util.function.Function;
import com.therandomlabs.randompatches.RandomPatches;
import com.therandomlabs.randomportals.api.frame.FrameType;

public final class FrameSize {
	public int minWidth;
	public int maxWidth;
	public int minHeight;
	public int maxHeight;

	public FrameSize() {
		if(RandomPatches.IS_DEOBFUSCATED) {
			minWidth = 3;
			maxWidth = Integer.MAX_VALUE;
			minHeight = 3;
			maxHeight = Integer.MAX_VALUE;
		} else {
			minWidth = 3;
			maxWidth = 100;
			minHeight = 3;
			maxHeight = 100;
		}
	}

	public FrameSize(int minWidth, int maxWidth, int minHeight, int maxHeight) {
		this.minWidth = minWidth;
		this.maxWidth = maxWidth;
		this.minHeight = minHeight;
		this.maxHeight = maxHeight;
	}

	public void ensureCorrect() {
		if(minWidth < 3) {
			minWidth = 3;
		}

		if(minHeight < 3) {
			minHeight = 3;
		}

		if(maxWidth < minWidth) {
			maxWidth = minWidth;
		}

		if(maxHeight < minHeight) {
			maxHeight = minHeight;
		}
	}

	public boolean test(int width, int height) {
		return width >= minWidth && width <= maxWidth && height >= minHeight && height <= maxHeight;
	}

	public int getMaxSize(boolean vertical) {
		return vertical ? maxHeight : Math.max(maxWidth, maxHeight);
	}

	public static Function<FrameType, FrameSize> fromJSONs(String species) {
		return type -> FrameSizes.get(species, type.toString().toLowerCase(Locale.ENGLISH));
	}
}
