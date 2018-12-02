package com.therandomlabs.verticalendportals.config;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import com.google.common.collect.ImmutableList;
import net.minecraft.world.DimensionType;

public final class NetherPortalTypes {
	private static ImmutableList<NetherPortalType> types;

	private NetherPortalTypes() {}

	public static ImmutableList<NetherPortalType> getTypes() {
		return types;
	}

	public static void reload() throws IOException {
		final Path directory = VEPConfig.getDirectory("nether_portal_types");
		List<Path> paths;

		try(final Stream<Path> pathStream = Files.list(directory)) {
			paths = pathStream.collect(Collectors.toList());
		}

		final List<NetherPortalType> types = new ArrayList<>(paths.size());

		for(int i = 0; i < paths.size(); i++) {
			final Path path = paths.get(i);

			if(!path.getFileName().toString().endsWith(".json")) {
				Files.delete(path);
				paths.remove(i--);
				continue;
			}

			final NetherPortalType type = VEPConfig.readJson(path, NetherPortalType.class);

			if(type == null) {
				Files.delete(path);
				paths.remove(i--);
			}

			type.ensureCorrect();
			VEPConfig.writeJson(path, type);
			types.add(type);
		}

		if(types.isEmpty()) {
			final NetherPortalType vanillaNetherPortal = new NetherPortalType(
					Collections.singletonList(new FrameBlock("minecraft:obsidian", 0)),
					DimensionType.NETHER.getId()
			);

			VEPConfig.writeJson(
					directory.resolve("vanilla_nether_portal.json"), vanillaNetherPortal
			);

			types.add(vanillaNetherPortal);
		}
	}
}
