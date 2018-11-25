package com.therandomlabs.verticalendportals;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
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
import com.therandomlabs.randompatches.util.RPUtils;
import com.therandomlabs.verticalendportals.frame.FrameSize;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigCategory;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.apache.commons.lang3.StringUtils;

@Mod.EventBusSubscriber(modid = VerticalEndPortals.MOD_ID)
@Config(modid = VerticalEndPortals.MOD_ID, name = VEPConfig.NAME, category = "")
public class VEPConfig {
	public static class Client {
		@Config.LangKey("verticalendportals.config.client.portalsCreativeTab")
		@Config.Comment("Enables the Portals creative tab.")
		public boolean portalsCreativeTab = true;

		@Config.RequiresMcRestart
		@Config.LangKey("verticalendportals.config.client.vepreloadclientCommand")
		@Config.Comment("Enables the client-sided /vepreloadclient command.")
		public boolean vepreloadclientCommand = true;
	}

	public static class EndPortals {
		@Config.RequiresMcRestart
		@Config.LangKey("verticalendportals.config.endPortals.enabled")
		@Config.Comment("Enables vertical End portals and a variety of End portal tweaks.")
		public boolean enabled = true;

		@Config.LangKey("verticalendportals.config.endPortals.useAllTypesJson")
		@Config.Comment("Whether to read from the all_types JSON rather than the different JSONs " +
				"for the specific frame types.")
		public boolean useAllTypesJson = true;
	}

	public static class Misc {
		@Config.RequiresWorldRestart
		@Config.LangKey("verticalendportals.config.misc.vepreloadCommand")
		@Config.Comment("Enables the /vepreload command.")
		public boolean vepreloadCommand = true;
	}

	public static class NetherPortals {
		@Config.RequiresMcRestart
		@Config.LangKey("verticalendportals.config.netherPortals.enabled")
		@Config.Comment("Enables lateral Nether portals and a variety of Nether portal tweaks.")
		public boolean enabled = true;

		@Config.LangKey("verticalendportals.config.netherPortals.useAllTypesJson")
		@Config.Comment("Whether to read from the all_types JSON rather than the different JSONs " +
				"for the specific frame types.")
		public boolean useAllTypesJson = true;
	}

	@Config.Ignore
	public static final String NAME = VerticalEndPortals.MOD_ID + "/" + VerticalEndPortals.MOD_ID;

	@Config.LangKey("verticalendportals.config.client")
	@Config.Comment("Options related to features that only work client-side.")
	public static Client client = new Client();

	@Config.LangKey("verticalendportals.config.endPortals")
	@Config.Comment("Options related to End portals.")
	public static EndPortals endPortals = new EndPortals();

	@Config.LangKey("verticalendportals.config.misc")
	@Config.Comment("Options that don't fit into any other categories.")
	public static Misc misc = new Misc();

	@Config.LangKey("verticalendportals.config.netherPortals")
	@Config.Comment("Options related to Nether portals.")
	public static NetherPortals netherPortals = new NetherPortals();

	private static final Method GET_CONFIGURATION = RPUtils.findMethod(
			ConfigManager.class, "getConfiguration", "getConfiguration", String.class, String.class
	);

	private static final Field CONFIGS = RPUtils.findField(ConfigManager.class, "CONFIGS");

	private static final Map<String, FrameSize> frameSizes = new HashMap<>();

	public static void reload() {
		ConfigManager.sync(VerticalEndPortals.MOD_ID, Config.Type.INSTANCE);

		try {
			modifyConfig();
			ConfigManager.sync(VerticalEndPortals.MOD_ID, Config.Type.INSTANCE);
			modifyConfig();
		} catch(Exception ex) {
			RPUtils.crashReport("Error while modifying config", ex);
		}

		frameSizes.clear();
	}

	public static void reloadFromDisk() {
		try {
			final File file = new File(Loader.instance().getConfigDir(), NAME + ".cfg");
			((Map) CONFIGS.get(null)).remove(file.getAbsolutePath());
			reload();
		} catch(Exception ex) {
			RPUtils.crashReport("Error while modifying config", ex);
		}
	}

	@SubscribeEvent
	public static void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent event) {
		if(event.getModID().equals(VerticalEndPortals.MOD_ID)) {
			reload();
		}
	}

	public static Path getConfig(String name) {
		final Path path = Paths.get("config", VerticalEndPortals.MOD_ID, name);
		final Path parent = path.getParent();

		try {
			if(parent != null) {
				if(parent.toFile().exists() && parent.toFile().isFile()) {
					Files.delete(parent);
				}

				Files.createDirectories(parent);
			}
		} catch(IOException ex) {
			RPUtils.crashReport("Failed to create parent: " + path, ex);
		}

		return path;
	}

	public static String read(Path path) {
		try {
			return StringUtils.join(Files.readAllLines(path), System.lineSeparator());
		} catch(IOException ex) {
			RPUtils.crashReport("Failed to read file: " + path, ex);
		}

		return null;
	}

	public static <T> T readJson(String jsonName, Class<T> clazz) {
		final Path path = getConfig(jsonName + ".json");

		if(!Files.exists(path)) {
			return null;
		}

		String raw = read(path);

		if(raw != null) {
			try {
				final Jankson jankson = Jankson.builder().build();
				raw = jankson.load(raw).toJson();
				return new Gson().fromJson(raw, clazz);
			} catch(SyntaxError ex) {
				RPUtils.crashReport("Failed to read JSON: " + path, ex);
			}
		}

		return null;
	}

	public static void writeJson(String jsonName, Object object) {
		final Path path = getConfig(jsonName + ".json");

		final String raw = new GsonBuilder().
				setPrettyPrinting().
				disableHtmlEscaping().
				create().
				toJson(object).replaceAll(" {2}", "\t");

		try {
			Files.write(path, (raw + System.lineSeparator()).getBytes(StandardCharsets.UTF_8));
		} catch(IOException ex) {
			RPUtils.crashReport("Failed to write to: " + path, ex);
		}
	}

	public static FrameSize getFrameSize(String name) {
		FrameSize size = frameSizes.get(name);

		if(size == null) {
			size = readJson(name, FrameSize.class);

			if(size == null) {
				size = new FrameSize();
			}
		}

		size.ensureCorrect();
		writeJson(name, size);
		frameSizes.put(name, size);

		return size;
	}

	private static void modifyConfig() throws IllegalAccessException, InvocationTargetException {
		final Configuration config = (Configuration) GET_CONFIGURATION.invoke(
				null, VerticalEndPortals.MOD_ID, NAME
		);

		final Map<Property, String> comments = new HashMap<>();

		//Remove old elements
		for(String name : config.getCategoryNames()) {
			final ConfigCategory category = config.getCategory(name);

			category.getValues().forEach((key, property) -> {
				final String comment = property.getComment();

				if(comment == null || comment.isEmpty()) {
					category.remove(key);
					return;
				}

				//Add default value to comment
				comments.put(property, comment);
				property.setComment(comment + "\nDefault: " + property.getDefault());
			});

			if(category.getValues().isEmpty() || category.getComment() == null) {
				config.removeCategory(category);
			}
		}

		config.save();

		//Remove default values from comments so they don't show up in the configuration GUI
		for(String name : config.getCategoryNames()) {
			config.getCategory(name).getValues().forEach(
					(key, property) -> property.setComment(comments.get(property))
			);
		}
	}
}
