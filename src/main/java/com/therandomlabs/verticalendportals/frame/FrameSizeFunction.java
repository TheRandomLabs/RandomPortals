package com.therandomlabs.verticalendportals.frame;

import java.util.function.BooleanSupplier;
import java.util.function.Function;

public abstract class FrameSizeFunction implements Function<FrameType, FrameSize> {
	public final FrameSize apply(FrameType type) {
		if(type == FrameType.LATERAL) {
			return getLateral();
		}

		if(type == FrameType.VERTICAL_X) {
			return getVerticalX();
		}

		return getVerticalZ();
	}

	public abstract FrameSize getLateral();

	public abstract FrameSize getVerticalX();

	public abstract FrameSize getVerticalZ();

	public static FrameSizeFunction fromJSONs(String species, BooleanSupplier useAllTypes) {
		return new FrameSizeFunction() {
			@Override
			public FrameSize getLateral() {
				final FrameSize all = getAllTypes();
				return all == null ? FrameSize.get(species, "lateral") : all;
			}

			@Override
			public FrameSize getVerticalX() {
				final FrameSize all = getAllTypes();
				return all == null ? FrameSize.get(species, "vertical_x") : all;
			}

			@Override
			public FrameSize getVerticalZ() {
				final FrameSize all = getAllTypes();
				return all == null ? FrameSize.get(species, "vertical_z") : all;
			}

			private FrameSize getAllTypes() {
				return useAllTypes.getAsBoolean() ? FrameSize.get(species, "all_types") : null;
			}
		};
	}
}
