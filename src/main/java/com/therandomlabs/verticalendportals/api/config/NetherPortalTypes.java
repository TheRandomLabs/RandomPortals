package com.therandomlabs.verticalendportals.api.config;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import com.google.common.collect.ImmutableMap;
import com.therandomlabs.verticalendportals.VEPConfig;
import com.therandomlabs.verticalendportals.api.frame.Frame;
import com.therandomlabs.verticalendportals.api.util.StatePredicate;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.world.DimensionType;

//TODO default types, so addons can add their own portal types
public final class NetherPortalTypes {
	private static ImmutableMap<String, NetherPortalType> types;
	private static StatePredicate validBlocks;

	private NetherPortalTypes() {}

	public static boolean hasType(String name) {
		return types.containsKey(name);
	}

	public static NetherPortalType get(String name) {
		final NetherPortalType type = types.get(name);
		return type == null ? getDefault() : type;
	}

	public static NetherPortalType getDefault() {
		final NetherPortalType type = types.get("vanilla_nether_portal");

		if(type != null) {
			return type;
		}

		return types.values().asList().get(0);
	}

	public static ImmutableMap<String, NetherPortalType> getTypes() {
		return types;
	}

	public static StatePredicate getValidBlocks() {
		return validBlocks;
	}

	public static NetherPortalType get(Frame frame) {
		for(NetherPortalType type : types.values()) {
			if(type.test(frame)) {
				return type;
			}
		}

		return getDefault();
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

			type.ensureCorrect();
			VEPConfig.writeJson(path, type);

			final String name = fileName.substring(0, fileName.length() - 5);
			type.name = name;
			types.put(name, type);
		}

		if(types.isEmpty() || (VEPConfig.netherPortals.forceCreateVanillaType &&
				!types.containsKey("vanilla_nether_portal"))) {
			final NetherPortalType vanillaNetherPortal = new NetherPortalType(
					Collections.singletonList(new FrameBlock(Blocks.OBSIDIAN, 0)),
					DimensionType.NETHER.getId()
			);

			vanillaNetherPortal.name = "vanilla_nether_portal";

			VEPConfig.writeJson(
					directory.resolve("vanilla_nether_portal.json"), vanillaNetherPortal
			);

			types.put("vanilla_nether_portal", vanillaNetherPortal);
		}

		NetherPortalTypes.types = ImmutableMap.copyOf(types);

		final List<Predicate<IBlockState>> matchers = new ArrayList<>();

		for(NetherPortalType type : types.values()) {
			for(FrameBlock frameBlock : type.frameBlocks) {
				matchers.add(frameBlock::test);
			}
		}

		validBlocks = (world, pos, state) -> {
			for(Predicate<IBlockState> matcher : matchers) {
				if(matcher.test(state)) {
					return true;
				}
			}

			return false;
		};
	}
}
