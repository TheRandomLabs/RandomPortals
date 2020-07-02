package com.therandomlabs.randomportals.config;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import blue.endless.jankson.Jankson;
import blue.endless.jankson.api.SyntaxError;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.therandomlabs.randomlib.config.Config;
import com.therandomlabs.randompatches.RandomPatches;
import com.therandomlabs.randomportals.RandomPortals;
import com.therandomlabs.randomportals.api.config.FrameSizes;
import com.therandomlabs.randomportals.api.config.PortalTypes;
import org.apache.commons.lang3.StringUtils;

@Config(value = RandomPortals.MOD_ID, path = RandomPortals.MOD_ID + "/" + RandomPortals.MOD_ID)
public final class RPOConfig {
	public static final class Client {
		@Config.Property("Enables the Portals creative tab.")
		public static boolean portalsCreativeTab = true;

		@Config.RequiresMCRestart
		@Config.Property("Enables the client-sided /rporeloadclient command.")
		public static boolean rporeloadclientCommand = true;
	}

	public static final class EndPortals {
		@Config.RequiresMCRestart
		@Config.Property("Enables vertical End portals and a variety of End portal tweaks.")
		public static boolean enabled = true;

		@Config.RangeDouble(min = 0.0, max = 1.0)
		@Config.Property(
				"The chance that a villager spawns with a vertical End portal frame on " +
						"their head."
		)
		public static double frameHeadVillagerSpawnChance =
				RandomPatches.IS_DEOBFUSCATED ? 0.5 : 0.01;

		@Config.Property(
				"Whether players can right click villagers with vertical End portals to " +
						"put them on their heads."
		)
		public static boolean rightClickVillagersToConvertToFrameHeads = true;
	}

	public static final class Misc {
		@Config.RequiresMCRestart
		@Config.Property("Whether to add an item for the End gateway.")
		public static boolean addEndGatewayItem = true;

		@Config.Property("Whether to trigger advancements related to portals.")
		public static boolean advancements = true;

		@Config.RequiresWorldReload
		@Config.Property("Enables the /rporeload command.")
		public static boolean rporeloadCommand = true;
	}

	public static final class NetherPortals {
		@Config.Property(
				"Whether all entities can cause portal generation when teleported through a " +
						"portal."
		)
		public static boolean allEntitiesCanCausePortalGeneration = RandomPatches.IS_DEOBFUSCATED;

		@Config.RequiresMCRestart
		@Config.Property("Whether to enable colored portals.")
		public static boolean coloredPortals = true;

		@Config.Property(
				"Whether portals should consume dyes even if they are invalid colors " +
						"(as defined by the Nether portal type)."
		)
		public static boolean consumeDyesEvenIfInvalidColor;

		@Config.Property("Whether portals should consume dyes even if they are the same color.")
		public static boolean consumeDyesEvenIfSameColor = true;

		@Config.Property("Whether portals should be dyeable.")
		public static boolean dyeablePortals = true;

		@Config.Property({
				"Whether single portal blocks can be dyed by right-clicking on them.",
				"This property and the above are independent of each other."
		})
		public static boolean dyeableSinglePortalBlocks = true;

		@Config.RequiresMCRestart
		@Config.Property(
				"Enables lateral Nether portals, custom portal types and a variety of " +
						"Nether portal tweaks and improvements."
		)
		public static boolean enabled = true;

		@Config.RequiresMCRestart
		@Config.Property(
				"Whether to always create the \"vanilla_nether_portal\" Nether portal " +
						"type when it doesn't exist."
		)
		public static boolean forceCreateVanillaType = true;

		@Config.Property({
				"Whether receiving Nether portals should be persistent.",
				"This makes mods like Netherless obsolete."
		})
		public static boolean persistentReceivingPortals = true;

		@Config.Property("Whether portals contribute to beacon colors.")
		public static boolean portalsContributeToBeaconColors = true;

		@Config.RangeInt(min = 1)
		@Config.Property(
				"The radius in which a suitable location to generate a portal should be " +
						"searched for upon teleportation through a portal."
		)
		public static int portalGenerationLocationSearchRadius = 16;

		@Config.RangeInt(min = 1)
		@Config.Property(
				"The radius in which existing portals in the destination dimension should be " +
						"searched for upon teleportation through a portal."
		)
		public static int portalSearchRadius = 128;

		@Config.Property({
				"Whether user placed portals inside the frame of the same type as the " +
						"portal should be replaced upon activation.",
				"Leaving this false is recommended for building purposes, as it allows players " +
						"to more easily create colored patterns in portals."
		})
		public static boolean replaceUserPlacedPortalsOnActivation;

		@Config.Property({
				"Whether portal ambient sounds should be server-sided instead of client-sided " +
						"as in vanilla.",
				"This must be enabled for custom portal ambient sounds to work."
		})
		public static boolean serverSidedAmbientSounds = true;
	}

	@Config.Category("Options related to features that only work client-side.")
	public static final Client client = null;

	@Config.Category("Options related to End portals.")
	public static final EndPortals endPortals = null;

	@Config.Category("Options that don't fit into any other categories.")
	public static final Misc misc = null;

	@Config.Category("Options related to Nether portals and custom portal types.")
	public static final NetherPortals netherPortals = null;

	public static final Gson GSON =
			new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

	public static void reload() {
		FrameSizes.reload();

		try {
			PortalTypes.reload();
		} catch (IOException ex) {
			RandomPortals.LOGGER.error("Error while reloading Nether portal types", ex);
		}
	}

	public static Path getConfigPath(String name) {
		return Paths.get("config", RandomPortals.MOD_ID, name);
	}

	public static Path getConfig(String name) {
		final Path path = getConfigPath(name);
		final Path parent = path.getParent();

		try {
			if (parent != null) {
				if (Files.isRegularFile(parent)) {
					Files.delete(parent);
				}

				Files.createDirectories(parent);
			}
		} catch (IOException ex) {
			RandomPortals.LOGGER.error("Failed to create parent: " + path, ex);
		}

		return path;
	}

	public static Path getDirectory(String name) {
		final Path path = getConfig(name);

		try {
			if (Files.exists(path)) {
				if (Files.isRegularFile(path)) {
					Files.delete(path);
					Files.createDirectory(path);
				}
			} else {
				Files.createDirectory(path);
			}
		} catch (IOException ex) {
			RandomPortals.LOGGER.error("Failed to create directory " + path, ex);
		}

		return path;
	}

	public static String read(Path path) {
		try {
			return StringUtils.join(Files.readAllLines(path), System.lineSeparator());
		} catch (IOException ex) {
			RandomPortals.LOGGER.error("Failed to read file: " + path, ex);
		}

		return null;
	}

	public static <T> T readJson(String jsonName, Class<T> clazz) {
		return readJson(getConfig(jsonName + ".json"), clazz);
	}

	public static <T> T readJson(Path path, Class<T> clazz) {
		if (!Files.exists(path)) {
			return null;
		}

		String raw = read(path);

		if (raw != null) {
			try {
				final Jankson jankson = Jankson.builder().build();
				raw = jankson.load(raw).toJson();
				return GSON.fromJson(raw, clazz);
			} catch (SyntaxError | JsonSyntaxException ex) {
				RandomPortals.LOGGER.error("Failed to read JSON: " + path, ex);
			}
		}

		return null;
	}

	public static void writeJson(String jsonName, Object object) {
		writeJson(getConfig(jsonName + ".json"), object);
	}

	public static void writeJson(Path path, Object object) {
		final String raw = GSON.toJson(object).replaceAll(" {2}", "\t");

		try {
			Files.write(path, (raw + System.lineSeparator()).getBytes(StandardCharsets.UTF_8));
		} catch (IOException ex) {
			RandomPortals.LOGGER.error("Failed to write to: " + path, ex);
		}
	}
}
