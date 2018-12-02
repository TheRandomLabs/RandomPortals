package com.therandomlabs.verticalendportals.config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.therandomlabs.verticalendportals.api.frame.FrameSize;

public final class FrameSizes {
	private static final List<String> names = new ArrayList<>();
	private static final Map<String, FrameSize> frameSizes = new HashMap<>();

	static {
		register("end_portal/normal");
		register("end_portal", "lateral_with_vertical_frames", "upside_down");
		registerVertical("end_portal/inwards_facing");
		register("nether_portal");
	}

	private FrameSizes() {}

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
			FrameSizes.names.add(species + "/" + name);
		}
	}
}
