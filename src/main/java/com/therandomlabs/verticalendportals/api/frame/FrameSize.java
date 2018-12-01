package com.therandomlabs.verticalendportals.api.frame;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.therandomlabs.verticalendportals.VEPConfig;

public class FrameSize {
	private static final List<String> names = new ArrayList<>();
	private static final Map<String, FrameSize> frameSizes = new HashMap<>();

	public int minWidth = 3;
	public int maxWidth = 9000;
	public int minHeight = 3;
	public int maxHeight = 9000;

	static {
		register("end_portal/normal");
		register("end_portal", "lateral_with_vertical_frames", "upside_down");
		registerVertical("end_portal/inwards_facing");
		register("nether_portal");
	}

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

	public static FrameSize get(String species, String name) {
		return frameSizes.get(species + "/" + name);
	}

	public static void reload() {
		for(String name : names) {
			FrameSize size = VEPConfig.readJson(name, FrameSize.class);

			if(size == null) {
				size = new FrameSize();
			} else {
				size.ensureCorrect();
			}

			VEPConfig.writeJson(name, size);
			frameSizes.put(name, size);
		}
	}

	public static void register(String species) {
		register(species, "all_variants", "lateral", "vertical_x", "vertical_z");
	}

	public static void registerVertical(String species) {
		register(species, "all_variants", "vertical_x", "vertical_z");
	}

	public static void register(String species, String... names) {
		for(String name : names) {
			FrameSize.names.add(species + "/" + name);
		}
	}
}