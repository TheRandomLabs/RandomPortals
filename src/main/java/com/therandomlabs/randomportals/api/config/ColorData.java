package com.therandomlabs.randomportals.api.config;

import java.util.HashSet;
import java.util.Set;
import net.minecraft.item.EnumDyeColor;

public final class ColorData {
	public enum DyeBehavior {
		DISABLE,
		ONLY_DEFINED_COLORS,
		ENABLE
	}

	public EnumDyeColor[] colors = {
			EnumDyeColor.PURPLE
	};
	public DyeBehavior dyeBehavior = DyeBehavior.ENABLE;

	public void ensureCorrect() {
		final Set<EnumDyeColor> colorSet = new HashSet<>();

		for(EnumDyeColor color : colors) {
			if(color != null) {
				colorSet.add(color);
			}
		}

		if(colorSet.isEmpty()) {
			colors = new EnumDyeColor[] {
					EnumDyeColor.PURPLE
			};
		} else {
			colors = colorSet.toArray(new EnumDyeColor[0]);
		}
	}
}
