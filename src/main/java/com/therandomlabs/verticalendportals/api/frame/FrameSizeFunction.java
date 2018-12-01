package com.therandomlabs.verticalendportals.api.frame;

import java.util.function.BooleanSupplier;
import java.util.function.Function;

public abstract class FrameSizeFunction implements Function<FrameType, FrameSize> {
	@Override
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

	public static FrameSizeFunction fromJSONs(String species, BooleanSupplier useAllVariants) {
		return new FrameSizeFunction() {
			@Override
			public FrameSize getLateral() {
				final FrameSize all = getAllVariants();
				return all == null ? FrameSize.get(species, "lateral") : all;
			}

			@Override
			public FrameSize getVerticalX() {
				final FrameSize all = getAllVariants();
				return all == null ? FrameSize.get(species, "vertical_x") : all;
			}

			@Override
			public FrameSize getVerticalZ() {
				final FrameSize all = getAllVariants();
				return all == null ? FrameSize.get(species, "vertical_z") : all;
			}

			private FrameSize getAllVariants() {
				return useAllVariants.getAsBoolean() ?
						FrameSize.get(species, "all_variants") : null;
			}
		};
	}

	public static FrameSizeFunction fromJSONsVertical(String species,
			BooleanSupplier useAllVariants) {
		return new FrameSizeFunction() {
			@Override
			public FrameSize getLateral() {
				return getVerticalX();
			}

			@Override
			public FrameSize getVerticalX() {
				final FrameSize all = getAllVariants();
				return all == null ? FrameSize.get(species, "vertical_x") : all;
			}

			@Override
			public FrameSize getVerticalZ() {
				final FrameSize all = getAllVariants();
				return all == null ? FrameSize.get(species, "vertical_z") : all;
			}

			private FrameSize getAllVariants() {
				return useAllVariants.getAsBoolean() ?
						FrameSize.get(species, "all_variants") : null;
			}
		};
	}
}
