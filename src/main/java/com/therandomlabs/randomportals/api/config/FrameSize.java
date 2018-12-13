package com.therandomlabs.randomportals.api.config;

import java.util.Locale;
import java.util.function.BooleanSupplier;
import java.util.function.Function;
import com.therandomlabs.randomportals.api.frame.FrameType;

public class FrameSize {
	public int minWidth = 3;
	public int maxWidth = 9000;
	public int minHeight = 3;
	public int maxHeight = 9000;

	public FrameSize() {}

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

	public static Function<FrameType, FrameSize> fromJSONs(String species,
			BooleanSupplier useAllVariants) {
		return type -> {
			if(useAllVariants.getAsBoolean()) {
				return FrameSizes.get(species, "all_variants");
			}

			return FrameSizes.get(species, type.toString().toLowerCase(Locale.ENGLISH));
		};
	}
}
