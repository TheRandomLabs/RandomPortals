package com.therandomlabs.verticalendportals;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigCategory;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

@Mod.EventBusSubscriber(modid = VerticalEndPortals.MOD_ID)
@Config(modid = VerticalEndPortals.MOD_ID, name = VerticalEndPortals.MOD_ID, category = "")
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

	public static class Misc {
		@Config.RequiresWorldRestart
		@Config.LangKey("verticalendportals.config.misc.vepreloadCommand")
		@Config.Comment("Enables the /vepreload command.")
		public boolean vepreloadCommand = true;
	}

	@Config.LangKey("verticalendportals.config.client")
	@Config.Comment("Options related to features that only work client-side.")
	public static Client client = new Client();

	@Config.LangKey("verticalendportals.config.misc")
	@Config.Comment("Options that don't fit into any other categories.")
	public static Misc misc = new Misc();

	private static final Method GET_CONFIGURATION = ReflectionHelper.findMethod(
			ConfigManager.class, "getConfiguration", "getConfiguration", String.class, String.class
	);

	private static final Field CONFIGS = ReflectionHelper.findField(ConfigManager.class, "CONFIGS");

	public static void reload() {
		ConfigManager.sync(VerticalEndPortals.MOD_ID, Config.Type.INSTANCE);

		try {
			modifyConfig();
			ConfigManager.sync(VerticalEndPortals.MOD_ID, Config.Type.INSTANCE);
			modifyConfig();
		} catch(Exception ex) {
			VerticalEndPortals.crashReport("Error while modifying config", ex);
		}
	}

	public static void reloadFromDisk() {
		try {
			final File file = new File(
					Loader.instance().getConfigDir(),
					VerticalEndPortals.MOD_ID + ".cfg"
			);
			((Map) CONFIGS.get(null)).remove(file.getAbsolutePath());
			reload();
		} catch(Exception ex) {
			VerticalEndPortals.crashReport("Error while modifying config", ex);
		}
	}

	@SubscribeEvent
	public static void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent event) {
		if(event.getModID().equals(VerticalEndPortals.MOD_ID)) {
			reload();
		}
	}

	private static void modifyConfig() throws IllegalAccessException, InvocationTargetException {
		final Configuration config = (Configuration) GET_CONFIGURATION.invoke(
				null, VerticalEndPortals.MOD_ID, VerticalEndPortals.MOD_ID
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
