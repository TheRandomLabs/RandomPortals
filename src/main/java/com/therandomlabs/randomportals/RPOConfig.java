package com.therandomlabs.randomportals;

import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import blue.endless.jankson.Jankson;
import blue.endless.jankson.impl.SyntaxError;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.therandomlabs.randompatches.RandomPatches;
import com.therandomlabs.randompatches.util.RPUtils;
import com.therandomlabs.randomportals.api.config.FrameSizes;
import com.therandomlabs.randomportals.api.config.PortalTypes;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigCategory;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.apache.commons.lang3.StringUtils;

@Mod.EventBusSubscriber(modid = RandomPortals.MOD_ID)
@Config(modid = RandomPortals.MOD_ID, name = RPOConfig.NAME, category = "")
public final class RPOConfig {
	public static final class Client {
		@Config.LangKey("randomportals.config.client.portalsCreativeTab")
		@Config.Comment("Enables the Portals creative tab.")
		public boolean portalsCreativeTab = true;

		@Config.RequiresMcRestart
		@Config.LangKey("randomportals.config.client.rporeloadclientCommand")
		@Config.Comment("Enables the client-sided /rporeloadclient command.")
		public boolean rporeloadclientCommand = true;
	}

	public static final class EndPortals {
		@Config.RequiresMcRestart
		@Config.LangKey("randomportals.config.endPortals.enabled")
		@Config.Comment("Enables vertical End portals and a variety of End portal tweaks.")
		public boolean enabled = true;

		@Config.RangeDouble(min = 0.0, max = 1.0)
		@Config.LangKey("randomportals.config.endPortals.frameHeadVillagerSpawnChance")
		@Config.Comment(
				"The chance that a villager spawns with a vertical End portal frame on " +
						"their head."
		)
		public double frameHeadVillagerSpawnChance = RandomPatches.IS_DEOBFUSCATED ? 0.5 : 0.01;

		@Config.LangKey("randomportals.config.endPortals.rightClickVillagersToConvertToFrameHeads")
		@Config.Comment(
				"Whether players can right click villagers with vertical End portals to " +
						"put them on their heads."
		)
		public boolean rightClickVillagersToConvertToFrameHeads = true;
	}

	public static final class Misc {
		@Config.LangKey("randomportals.config.misc.advancements")
		@Config.Comment("Whether to trigger advancements related to portals.")
		public boolean advancements = true;

		@Config.RequiresWorldRestart
		@Config.LangKey("randomportals.config.misc.rporeloadCommand")
		@Config.Comment("Enables the /rporeload command.")
		public boolean rporeloadCommand = true;
	}

	public static final class NetherPortals {
		@Config.LangKey("randomportals.config.netherPortals.allEntitiesCanCausePortalGeneration")
		@Config.Comment(
				"Whether all entities can cause portal generation when teleported through a " +
						"portal."
		)
		public boolean allEntitiesCanCausePortalGeneration;

		@Config.RequiresMcRestart
		@Config.LangKey("randomportals.config.netherPortals.coloredPortals")
		@Config.Comment("Whether to enable colored portals.")
		public boolean coloredPortals = true;

		@Config.LangKey("randomportals.config.netherPortals.consumeDyesEvenIfInvalidColor")
		@Config.Comment(
				"Whether portals should consume dyes even if they are invalid colors " +
						"(as defined by the Nether portal type)."
		)
		public boolean consumeDyesEvenIfInvalidColor;

		@Config.LangKey("randomportals.config.netherPortals.consumeDyesEvenIfSameColor")
		@Config.Comment("Whether portals should consume dyes even if they are the same color.")
		public boolean consumeDyesEvenIfSameColor = true;

		@Config.LangKey("randomportals.config.netherPortals.dyeablePortals")
		@Config.Comment("Whether portals should be dyeable.")
		public boolean dyeablePortals = true;

		@Config.LangKey("randomportals.config.netherPortals.dyeableSinglePortalBlocks")
		@Config.Comment({
				"Whether single portal blocks can be dyed by right-clicking on them.",
				"This property and the above are independent of each other."
		})
		public boolean dyeableSinglePortalBlocks = true;

		@Config.RequiresMcRestart
		@Config.LangKey("randomportals.config.netherPortals.enabled")
		@Config.Comment(
				"Enables lateral Nether portals, custom portal types and a variety of " +
						"Nether portal tweaks and improvements."
		)
		public boolean enabled = true;

		@Config.RequiresMcRestart
		@Config.LangKey("randomportals.config.netherPortals.forceCreateVanillaType")
		@Config.Comment(
				"Whether to always create the \"vanilla_nether_portal\" Nether portal " +
						"type when it doesn't exist."
		)
		public boolean forceCreateVanillaType = true;

		@Config.LangKey("randomportals.config.netherPortals.persistentReceivingPortals")
		@Config.Comment({
				"Whether receiving Nether portals should be persistent.",
				"This makes mods like Netherless obsolete."
		})
		public boolean persistentReceivingPortals = true;

		@Config.LangKey("randomportals.config.netherPortals.portalsContributeToBeaconColors")
		@Config.Comment("Whether portals contribute to beacon colors.")
		public boolean portalsContributeToBeaconColors = true;

		@Config.RangeInt(min = 1)
		@Config.LangKey("randomportals.config.netherPortals.portalGenerationLocationSearchRadius")
		@Config.Comment(
				"The radius in which a suitable location to generate a portal should be " +
						"searched for upon teleportation through a portal."
		)
		public int portalGenerationLocationSearchRadius = 16;

		@Config.RangeInt(min = 1)
		@Config.LangKey("randomportals.config.netherPortals.portalSearchRadius")
		@Config.Comment(
				"The radius in which existing portals in the destination dimension should be " +
						"searched for upon teleportation through a portal."
		)
		public int portalSearchRadius = 128;

		@Config.LangKey("randomportals.config.netherPortals.replaceUserPlacedPortalsOnActivation")
		@Config.Comment({
				"Whether user placed portals inside the frame of the same type as the " +
						"portal should be replaced upon activation.",
				"Leaving this false is recommended for building purposes, as it allows players " +
						"to more easily create colored patterns in portals."
		})
		public boolean replaceUserPlacedPortalsOnActivation;
	}

	@Config.Ignore
	public static final String NAME = RandomPortals.MOD_ID + "/" + RandomPortals.MOD_ID;

	@Config.Ignore
	public static final Gson GSON = new GsonBuilder().
			setPrettyPrinting().
			disableHtmlEscaping().
			create();

	@Config.LangKey("randomportals.config.client")
	@Config.Comment("Options related to features that only work client-side.")
	public static final Client client = new Client();

	@Config.LangKey("randomportals.config.endPortals")
	@Config.Comment("Options related to End portals.")
	public static final EndPortals endPortals = new EndPortals();

	@Config.LangKey("randomportals.config.misc")
	@Config.Comment("Options that don't fit into any other categories.")
	public static final Misc misc = new Misc();

	@Config.LangKey("randomportals.config.netherPortals")
	@Config.Comment("Options related to Nether portals and custom portal types.")
	public static final NetherPortals netherPortals = new NetherPortals();

	private static final Method GET_CONFIGURATION = RPUtils.findMethod(
			ConfigManager.class, "getConfiguration", "getConfiguration", String.class, String.class
	);

	private static final Method SYNC = RPUtils.findMethod(
			ConfigManager.class, "sync", "sync", Configuration.class, Class.class, String.class,
			String.class, boolean.class, Object.class
	);

	private static final Map<Property, String> comments = new HashMap<>();

	private static boolean firstReload = true;

	@SubscribeEvent
	public static void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent event) {
		if(event.getModID().equals(RandomPortals.MOD_ID)) {
			reload();
		}
	}

	public static void reload() {
		try {
			final Configuration config =
					(Configuration) GET_CONFIGURATION.invoke(null, RandomPortals.MOD_ID, NAME);

			//reload() is only called by CommonProxy and RPOConfig
			//Forge syncs the config during mod construction, so this first sync is not necessary
			if(!firstReload) {
				SYNC.invoke(null, config, RPOConfig.class, RandomPortals.MOD_ID, "", false, null);
			}

			firstReload = false;

			onReload();

			//Remove old elements
			for(String name : config.getCategoryNames()) {
				final ConfigCategory category = config.getCategory(name);

				category.getValues().forEach((key, property) -> {
					final String comment = property.getComment();

					if(comment == null || comment.isEmpty()) {
						category.remove(key);
						return;
					}

					String newComment = comments.get(property);

					if(newComment == null) {
						newComment = comment + "\nDefault: " + property.getDefault();
						comments.put(property, newComment);
					}

					property.setComment(newComment);
				});

				if(category.getValues().isEmpty() || category.getComment() == null) {
					config.removeCategory(category);
				}
			}

			config.save();

			SYNC.invoke(null, config, RPOConfig.class, RandomPortals.MOD_ID, "", false, null);

			//Remove default values, min/max values and valid values from the comments so
			//they don't show up twice in the configuration GUI
			for(String name : config.getCategoryNames()) {
				final ConfigCategory category = config.getCategory(name);

				category.getValues().forEach((key, property) -> {
					final String[] comment = property.getComment().split("\n");
					final StringBuilder prunedComment = new StringBuilder();

					for(String line : comment) {
						if(line.startsWith("Default:") || line.startsWith("Min:")) {
							break;
						}

						prunedComment.append(line).append("\n");
					}

					final String commentString = prunedComment.toString();
					property.setComment(commentString.substring(0, commentString.length() - 1));
				});
			}
		} catch(Exception ex) {
			RandomPortals.LOGGER.error("Error while modifying config", ex);
		}

		//This method is first called in CommonProxy.preInit
		//FrameSizes.reload and PortalTypes.reload should first be called in CommonProxy.init
		if(firstReload) {
			firstReload = false;
			return;
		}

		FrameSizes.reload();

		try {
			PortalTypes.reload();
		} catch(IOException ex) {
			RandomPortals.LOGGER.error("Error while reloading Nether portal types", ex);
		}
	}

	public static void reloadFromDisk() {
		try {
			final Configuration config =
					(Configuration) GET_CONFIGURATION.invoke(null, RandomPortals.MOD_ID, NAME);
			final Configuration tempConfig = new Configuration(config.getConfigFile());

			tempConfig.load();

			for(String name : tempConfig.getCategoryNames()) {
				final Map<String, Property> properties = tempConfig.getCategory(name).getValues();

				for(Map.Entry<String, Property> entry : properties.entrySet()) {
					config.getCategory(name).get(entry.getKey()).set(entry.getValue().getString());
				}
			}

			reload();

			MinecraftForge.EVENT_BUS.post(new ConfigChangedEvent.PostConfigChangedEvent(
					RandomPortals.MOD_ID, null, true, false
			));
		} catch(Exception ex) {
			RandomPortals.LOGGER.error("Error while modifying config", ex);
		}
	}

	public static Path getConfigPath(String name) {
		return Paths.get("config", RandomPortals.MOD_ID, name);
	}

	public static Path getConfig(String name) {
		final Path path = getConfigPath(name);
		final Path parent = path.getParent();

		try {
			if(parent != null) {
				if(parent.toFile().exists() && parent.toFile().isFile()) {
					Files.delete(parent);
				}

				Files.createDirectories(parent);
			}
		} catch(IOException ex) {
			RandomPortals.LOGGER.error("Failed to create parent: " + path, ex);
		}

		return path;
	}

	public static Path getDirectory(String name) {
		final Path path = getConfig(name);

		try {
			if(Files.exists(path)) {
				if(Files.isRegularFile(path)) {
					Files.delete(path);
					Files.createDirectory(path);
				}
			} else {
				Files.createDirectory(path);
			}
		} catch(IOException ex) {
			RandomPortals.LOGGER.error("Failed to create directory " + path, ex);
		}

		return path;
	}

	public static String read(Path path) {
		try {
			return StringUtils.join(Files.readAllLines(path), System.lineSeparator());
		} catch(IOException ex) {
			RandomPortals.LOGGER.error("Failed to read file: " + path, ex);
		}

		return null;
	}

	public static <T> T readJson(String jsonName, Class<T> clazz) {
		return readJson(getConfig(jsonName + ".json"), clazz);
	}

	public static <T> T readJson(Path path, Class<T> clazz) {
		if(!Files.exists(path)) {
			return null;
		}

		String raw = read(path);

		if(raw != null) {
			try {
				final Jankson jankson = Jankson.builder().build();
				raw = jankson.load(raw).toJson();
				return GSON.fromJson(raw, clazz);
			} catch(SyntaxError | JsonSyntaxException ex) {
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
		} catch(IOException ex) {
			RandomPortals.LOGGER.error("Failed to write to: " + path, ex);
		}
	}

	private static void onReload() {}
}
