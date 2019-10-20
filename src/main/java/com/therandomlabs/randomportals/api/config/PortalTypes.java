package com.therandomlabs.randomportals.api.config;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import com.google.common.collect.ImmutableMap;
import com.therandomlabs.randomportals.RandomPortals;
import com.therandomlabs.randomportals.api.frame.Frame;
import com.therandomlabs.randomportals.api.frame.FrameType;
import com.therandomlabs.randomportals.api.util.FrameStatePredicate;
import com.therandomlabs.randomportals.config.RPOConfig;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

public final class PortalTypes {
	public static final String VANILLA_NETHER_PORTAL_ID = "vanilla_nether_portal";

	public static final PortalTypeGroup VANILLA_NETHER_PORTAL =
			new PortalTypeGroup(VANILLA_NETHER_PORTAL_ID);

	private static final Map<String, PortalTypeGroup> builtinGroups = new HashMap<>();
	private static final Map<String, PortalTypeGroup> builtinTypes = new HashMap<>();

	private static ImmutableMap<String, PortalTypeGroup> groups;
	private static FrameStatePredicate validBlocks;
	private static Predicate<ItemStack> validActivators;
	private static FrameSizeData maximumSize;

	static {
		final PortalType type = new PortalType();
		type.group = VANILLA_NETHER_PORTAL;
		type.dimensionID = 0;
		VANILLA_NETHER_PORTAL.types.put(0, type);
	}

	private PortalTypes() {}

	public static boolean hasGroup(String id) {
		return groups.containsKey(id);
	}

	public static PortalTypeGroup getGroup(String id) {
		final PortalTypeGroup type = groups.get(id);
		return type == null ? getDefaultGroup() : type;
	}

	public static PortalType get(int dimensionID, String id) {
		return getGroup(id).getType(dimensionID);
	}

	public static PortalType get(World world, String id) {
		return get(world.provider.getDimension(), id);
	}

	public static PortalType getSpecific(String id) {
		final String[] split = StringUtils.split(id, ':');

		if (split.length == 1) {
			return getGroup(id).getDefaultType();
		}

		return getGroup(split[0]).getType(Integer.parseInt(split[1]));
	}

	public static PortalTypeGroup getDefaultGroup() {
		final PortalTypeGroup group = groups.get(VANILLA_NETHER_PORTAL_ID);
		return group == null ? groups.values().asList().get(0) : group;
	}

	public static PortalType getDefault(int dimensionID) {
		return getDefaultGroup().getType(dimensionID);
	}

	public static PortalType getDefault(World world) {
		return getDefault(world.provider.getDimension());
	}

	public static ImmutableMap<String, PortalTypeGroup> getGroups() {
		return groups;
	}

	public static FrameStatePredicate getValidBlocks() {
		return validBlocks;
	}

	public static FrameStatePredicate getValidBlocks(PortalType... types) {
		return getValidBlocks(Arrays.asList(types));
	}

	public static FrameStatePredicate getValidBlocks(Collection<PortalType> types) {
		final List<Predicate<IBlockState>> matchers = new ArrayList<>();

		for (PortalType type : types) {
			for (FrameBlock block : type.frame.blocks) {
				matchers.add(block::test);
			}
		}

		return (world, pos, state, type) -> {
			for (Predicate<IBlockState> matcher : matchers) {
				if (matcher.test(state)) {
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

		for (PortalType type : types) {
			for (PortalActivator activator : type.activation.activators) {
				matchers.add(activator::test);
			}
		}

		return stack -> {
			for (Predicate<ItemStack> matcher : matchers) {
				if (matcher.test(stack)) {
					return true;
				}
			}

			return false;
		};
	}

	public static FrameSizeData getMaximumSize() {
		return maximumSize;
	}

	public static FrameSize getMaximumSize(FrameType type) {
		return maximumSize.get(type);
	}

	public static PortalType get(Frame frame) {
		for (PortalTypeGroup group : groups.values()) {
			for (PortalType type : group.types.values()) {
				if (type.test(frame)) {
					return type;
				}
			}
		}

		final PortalTypeGroup defaultGroup = getDefaultGroup();
		return defaultGroup.types.get(defaultGroup.defaultDimensionID);
	}

	public static void reload() throws IOException {
		final Path directory = RPOConfig.getDirectory("portal_types");
		List<Path> paths;

		try (final Stream<Path> pathStream = Files.list(directory)) {
			paths = pathStream.collect(Collectors.toList());
		}

		final Map<String, PortalTypeGroup> types = new HashMap<>(paths.size());

		for (Path groupPath : paths) {
			if (!Files.isDirectory(groupPath)) {
				Files.delete(groupPath);
				continue;
			}

			final String id = groupPath.getFileName().toString();
			final Path groupData = groupPath.resolve("group_data.json");

			if (!Files.exists(groupData) || !Files.isRegularFile(groupData)) {
				RandomPortals.LOGGER.error("Invalid portal type group: " + id);
				continue;
			}

			final PortalTypeGroup group = RPOConfig.readJson(groupData, PortalTypeGroup.class);

			if (group == null) {
				RandomPortals.LOGGER.error("Invalid portal type group: " + id);
				continue;
			}

			RPOConfig.writeJson(groupData, group);

			group.id = id;

			List<Path> typePaths;

			try (final Stream<Path> pathStream = Files.list(groupPath)) {
				typePaths = pathStream.collect(Collectors.toList());
			}

			for (Path typePath : typePaths) {
				if (groupData.equals(typePath)) {
					continue;
				}

				if (Files.isDirectory(typePath)) {
					FileUtils.deleteDirectory(typePath.toFile());
					continue;
				}

				final String typeFileName = typePath.getFileName().toString();

				if (!typeFileName.endsWith(".json")) {
					Files.delete(typePath);
					continue;
				}

				int dimensionID;

				try {
					dimensionID = Integer.parseInt(StringUtils.removeEnd(typeFileName, ".json"));
				} catch (NumberFormatException ex) {
					Files.delete(typePath);
					continue;
				}

				final PortalType type = RPOConfig.readJson(typePath, PortalType.class);

				if (type == null) {
					continue;
				}

				type.ensureCorrect();
				RPOConfig.writeJson(typePath, type);

				type.group = group;
				type.dimensionID = dimensionID;
				group.types.put(dimensionID, type);
			}

			if (group.isValid()) {
				group.ensureCorrect();
				RPOConfig.writeJson(groupData, group);
				types.put(id, group);
			} else {
				RandomPortals.LOGGER.error("Invalid portal type group: " + id);
			}
		}

		if (types.isEmpty() || (RPOConfig.NetherPortals.forceCreateVanillaType &&
				!types.containsKey(VANILLA_NETHER_PORTAL_ID))) {
			write(directory, VANILLA_NETHER_PORTAL);
		}

		types.putAll(builtinGroups);

		for (Map.Entry<String, PortalTypeGroup> entry : builtinTypes.entrySet()) {
			final String name = entry.getKey();

			if (!types.containsKey(name)) {
				final PortalTypeGroup group = entry.getValue();
				write(directory, group);
				types.put(name, group);
			}
		}

		PortalTypes.groups = ImmutableMap.copyOf(types);

		final Collection<PortalTypeGroup> groups = types.values();
		final List<PortalType> actualTypes = new ArrayList<>();

		groups.stream().map(group -> group.types.values()).forEach(actualTypes::addAll);

		validBlocks = getValidBlocks(actualTypes);
		validActivators = getValidActivators(actualTypes);

		maximumSize = new FrameSizeData();
		maximumSize.lateral = loadMaximumSize(FrameType.LATERAL);
		maximumSize.verticalX = loadMaximumSize(FrameType.VERTICAL_X);
		maximumSize.verticalZ = loadMaximumSize(FrameType.VERTICAL_Z);
	}

	public static void registerBuiltinGroup(String name, PortalTypeGroup group) {
		group.id = name;
		builtinGroups.put(name, group);
	}

	public static void unregisterBuiltinGroup(String name) {
		builtinGroups.remove(name);
	}

	public static void registerDefaultGroup(String name, PortalTypeGroup group) {
		group.id = name;
		builtinTypes.put(name, group);
	}

	public static void unregisterDefaultGroup(String name) {
		builtinTypes.remove(name);
	}

	private static void write(Path directory, PortalTypeGroup group) throws IOException {
		final Path groupPath = directory.resolve(group.id);

		if (Files.exists(groupPath)) {
			if (!Files.isDirectory(groupPath)) {
				Files.delete(groupPath);
				Files.createDirectories(groupPath);
			}
		} else {
			Files.createDirectories(groupPath);
		}

		RPOConfig.writeJson(groupPath.resolve("group_data.json"), group);

		group.types.forEach((dimensionID, type) -> RPOConfig.writeJson(
				groupPath.resolve(dimensionID + ".json"), type
		));
	}

	private static FrameSize loadMaximumSize(FrameType type) {
		int minWidth = Integer.MAX_VALUE;
		int maxWidth = 3;
		int minHeight = Integer.MAX_VALUE;
		int maxHeight = 3;

		for (PortalTypeGroup group : PortalTypes.getGroups().values()) {
			for (PortalType portalType : group.types.values()) {
				final FrameSize size = portalType.frame.size.get(type);

				if (size.minWidth < minWidth) {
					minWidth = size.minWidth;
				}

				if (size.maxWidth > maxWidth) {
					maxWidth = size.maxWidth;
				}

				if (size.minHeight < minHeight) {
					minHeight = size.minHeight;
				}

				if (size.maxHeight > maxHeight) {
					maxHeight = size.maxHeight;
				}
			}
		}

		return new FrameSize(minWidth, maxWidth, minHeight, maxHeight);
	}
}
