package com.therandomlabs.randomportals.api.config;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import com.google.common.collect.ImmutableMap;
import com.therandomlabs.randomportals.RPOConfig;
import com.therandomlabs.randomportals.api.frame.Frame;
import com.therandomlabs.randomportals.api.frame.FrameType;
import com.therandomlabs.randomportals.api.util.FrameStatePredicate;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.world.DimensionType;

public final class PortalTypes {
	public static final String VANILLA_NETHER_PORTAL_NAME = "vanilla_nether_portal";

	public static final PortalType VANILLA_NETHER_PORTAL = new PortalType(
			VANILLA_NETHER_PORTAL_NAME,
			Collections.singletonList(new FrameBlock(Blocks.OBSIDIAN, 0)),
			DimensionType.NETHER.getId()
	);

	private static final Map<String, PortalType> builtinTypes = new HashMap<>();
	private static final Map<String, PortalType> defaultTypes = new HashMap<>();

	private static ImmutableMap<String, PortalType> types;
	private static FrameStatePredicate validBlocks;
	private static Predicate<ItemStack> validActivators;
	private static FrameSizeData size;

	private PortalTypes() {}

	public static boolean hasType(String name) {
		return types.containsKey(name);
	}

	public static PortalType get(String name) {
		final PortalType type = types.get(name);
		return type == null ? getDefault() : type;
	}

	public static PortalType getDefault() {
		final PortalType type = types.get("vanilla_nether_portal");

		if(type != null) {
			return type;
		}

		return types.values().asList().get(0);
	}

	public static ImmutableMap<String, PortalType> getTypes() {
		return types;
	}

	public static FrameStatePredicate getValidBlocks() {
		return validBlocks;
	}

	public static FrameStatePredicate getValidBlocks(PortalType... types) {
		return getValidBlocks(Arrays.asList(types));
	}

	public static FrameStatePredicate getValidBlocks(Collection<PortalType> types) {
		final List<Predicate<IBlockState>> matchers = new ArrayList<>();

		for(PortalType type : types) {
			for(FrameBlock frameBlock : type.frameBlocks) {
				matchers.add(frameBlock::test);
			}
		}

		return (world, pos, state, type) -> {
			for(Predicate<IBlockState> matcher : matchers) {
				if(matcher.test(state)) {
					return true;
				}
			}

			return false;
		};
	}

	public static Predicate<ItemStack> getValidActivators() {
		return validActivators;
	}

	public static Predicate<ItemStack> getValidActivators(Collection<PortalType> types) {
		final List<Predicate<ItemStack>> matchers = new ArrayList<>();

		for(PortalType type : types) {
			for(FrameActivator activator : type.activation.activators) {
				matchers.add(activator::test);
			}
		}

		return stack -> {
			for(Predicate<ItemStack> matcher : matchers) {
				if(matcher.test(stack)) {
					return true;
				}
			}

			return false;
		};
	}

	public static FrameSizeData getSize() {
		return size;
	}

	public static FrameSize getSize(FrameType type) {
		return size.get(type);
	}

	public static PortalType get(Frame frame) {
		for(PortalType type : types.values()) {
			if(type.test(frame)) {
				return type;
			}
		}

		return getDefault();
	}

	public static void reload() throws IOException {
		final Path directory = RPOConfig.getDirectory("portal_types");

		//TODO remove eventually
		final Path oldDirectory = RPOConfig.getDirectory("nether_portal_types");

		if(Files.isDirectory(oldDirectory)) {
			Files.move(oldDirectory, directory, StandardCopyOption.REPLACE_EXISTING);
		}

		List<Path> paths;

		try(final Stream<Path> pathStream = Files.list(directory)) {
			paths = pathStream.collect(Collectors.toList());
		}

		final Map<String, PortalType> types = new HashMap<>(paths.size());

		for(int i = 0; i < paths.size(); i++) {
			final Path path = paths.get(i);
			final String fileName = path.getFileName().toString();

			if(!fileName.endsWith(".json")) {
				Files.delete(path);
				paths.remove(i--);
				continue;
			}

			final PortalType type = RPOConfig.readJson(path, PortalType.class);

			if(type == null) {
				Files.delete(path);
				paths.remove(i--);
			}

			type.ensureCorrect();
			RPOConfig.writeJson(path, type);

			final String name = fileName.substring(0, fileName.length() - 5);
			type.name = name;
			types.put(name, type);
		}

		if(types.isEmpty() || (RPOConfig.netherPortals.forceCreateVanillaType &&
				!types.containsKey(VANILLA_NETHER_PORTAL_NAME))) {
			RPOConfig.writeJson(
					directory.resolve(VANILLA_NETHER_PORTAL_NAME + ".json"), VANILLA_NETHER_PORTAL
			);

			types.put(VANILLA_NETHER_PORTAL_NAME, VANILLA_NETHER_PORTAL);
		}

		types.putAll(builtinTypes);

		for(Map.Entry<String, PortalType> entry : defaultTypes.entrySet()) {
			final String name = entry.getKey();

			if(!types.containsKey(name)) {
				types.put(name, entry.getValue());
			}
		}

		PortalTypes.types = ImmutableMap.copyOf(types);

		final Collection<PortalType> actualTypes = types.values();

		validBlocks = getValidBlocks(actualTypes);
		validActivators = getValidActivators(actualTypes);

		size = new FrameSizeData();
		size.lateral = loadSize(FrameType.LATERAL);
		size.verticalX = loadSize(FrameType.VERTICAL_X);
		size.verticalZ = loadSize(FrameType.VERTICAL_Z);
	}

	public static void registerBuiltinType(String name, PortalType type) {
		type.name = name;
		builtinTypes.put(name, type);
	}

	public static void unregisterBuiltinType(String name) {
		builtinTypes.remove(name);
	}

	public static void registerDefaultType(String name, PortalType type) {
		type.name = name;
		defaultTypes.put(name, type);
	}

	public static void unregisterDefaultType(String name) {
		defaultTypes.remove(name);
	}

	private static FrameSize loadSize(FrameType type) {
		int minWidth = Integer.MAX_VALUE;
		int maxWidth = 3;
		int minHeight = Integer.MAX_VALUE;
		int maxHeight = 3;

		for(PortalType portalType : PortalTypes.getTypes().values()) {
			final FrameSize size = portalType.size.get(type);

			if(size.minWidth < minWidth) {
				minWidth = size.minWidth;
			}

			if(size.maxWidth > maxWidth) {
				maxWidth = size.maxWidth;
			}

			if(size.minHeight < minHeight) {
				minHeight = size.minHeight;
			}

			if(size.maxHeight > maxHeight) {
				maxHeight = size.maxHeight;
			}
		}

		return new FrameSize(minWidth, maxWidth, minHeight, maxHeight);
	}
}
