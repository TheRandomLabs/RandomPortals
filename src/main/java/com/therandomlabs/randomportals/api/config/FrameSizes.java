package com.therandomlabs.randomportals.api.config;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import com.therandomlabs.randomportals.config.RPOConfig;

public final class FrameSizes {
	private static final Set<String> names = new HashSet<>();
	private static final Map<String, FrameSize> frameSizes = new HashMap<>();

	private FrameSizes() {}

	public static FrameSize get(String species, String name) {
		return frameSizes.get(species + "/" + name);
	}

	public static void reload() {
		for(String name : names) {
			FrameSize size = RPOConfig.readJson(name, FrameSize.class);

			if(size == null) {
				size = new FrameSize();
			} else {
				size.ensureCorrect();
			}

			RPOConfig.writeJson(name, size);
			frameSizes.put(name, size);
		}
	}

	public static void register(String species) {
		register(species, "lateral", "vertical_x", "vertical_z");
	}

	public static void registerVertical(String species) {
		register(species, "vertical_x", "vertical_z");
	}

	public static void register(String species, String... names) {
		for(String name : names) {
			FrameSizes.names.add(species + "/" + name);
		}
	}
}
