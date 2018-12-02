package com.therandomlabs.verticalendportals.config;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.world.DimensionType;

public final class NetherPortalTypes {
	private static ImmutableMap<String, NetherPortalType> types;
	private static ImmutableList<Block> validBlocks;

	private NetherPortalTypes() {}

	public static boolean hasType(String name) {
		return types.containsKey(name);
	}

	public static NetherPortalType get(String name) {
		NetherPortalType type = types.get(name);

		if(type != null) {
			return type;
		}

		type = types.get("vanilla_nether_portal");

		if(type != null) {
			return type;
		}

		return types.values().asList().get(0);
	}

	public static ImmutableMap<String, NetherPortalType> getTypes() {
		return types;
	}

	public static ImmutableList<Block> getValidBlocks() {
		return validBlocks;
	}

	public static void reload() throws IOException {
		final Path directory = VEPConfig.getDirectory("nether_portal_types");
		List<Path> paths;

		try(final Stream<Path> pathStream = Files.list(directory)) {
			paths = pathStream.collect(Collectors.toList());
		}

		final Map<String, NetherPortalType> types = new HashMap<>(paths.size());

		for(int i = 0; i < paths.size(); i++) {
			final Path path = paths.get(i);
			final String fileName = path.getFileName().toString();

			if(!fileName.endsWith(".json")) {
				Files.delete(path);
				paths.remove(i--);
				continue;
			}

			final NetherPortalType type = VEPConfig.readJson(path, NetherPortalType.class);

			if(type == null) {
				Files.delete(path);
				paths.remove(i--);
			}

			if(type.ensureCorrect()) {
				VEPConfig.writeJson(path, type);
			}

			types.put(fileName.substring(0, fileName.length() - 5), type);
		}

		if(types.isEmpty()) {
			final NetherPortalType vanillaNetherPortal = new NetherPortalType(
					Collections.singletonList(new FrameBlock(Blocks.OBSIDIAN, 0)),
					DimensionType.NETHER.getId()
			);

			VEPConfig.writeJson(
					directory.resolve("vanilla_nether_portal.json"), vanillaNetherPortal
			);

			types.put("vanilla_nether_portal", vanillaNetherPortal);
		}

		NetherPortalTypes.types = ImmutableMap.copyOf(types);

		final Set<Block> blocks = new HashSet<>();

		for(NetherPortalType type : types.values()) {
			for(FrameBlock frameBlock : type.frameBlocks) {
				blocks.add(frameBlock.getBlock());
			}
		}

		validBlocks = ImmutableList.copyOf(blocks);
	}
}
