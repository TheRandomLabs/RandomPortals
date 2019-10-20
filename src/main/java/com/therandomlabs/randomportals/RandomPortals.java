package com.therandomlabs.randomportals;

import com.therandomlabs.randomlib.config.CommandConfigReload;
import com.therandomlabs.randomportals.api.config.PortalTypes;
import com.therandomlabs.randomportals.config.RPOConfig;
import net.minecraft.command.CommandBase;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod.EventBusSubscriber(modid = RandomPortals.MOD_ID)
@Mod(
		modid = RandomPortals.MOD_ID, name = RandomPortals.NAME,
		version = RandomPortals.VERSION,
		acceptedMinecraftVersions = RandomPortals.ACCEPTED_MINECRAFT_VERSIONS,
		dependencies = RandomPortals.DEPENDENCIES, guiFactory = RandomPortals.GUI_FACTORY,
		updateJSON = RandomPortals.UPDATE_JSON,
		certificateFingerprint = RandomPortals.CERTIFICATE_FINGERPRINT
)
public final class RandomPortals {
	public static final String MOD_ID = "randomportals";
	public static final String NAME = "RandomPortals";
	public static final String VERSION = "@VERSION@";
	public static final String ACCEPTED_MINECRAFT_VERSIONS = "[1.12.2,1.13)";
	public static final String DEPENDENCIES =
			"required-after:randompatches@[1.12.2-1.15.0.0,);before:movingworld";
	public static final String GUI_FACTORY =
			"com.therandomlabs.randomportals.config.RPOGuiConfigFactory";
	public static final String UPDATE_JSON =
			"https://raw.githubusercontent.com/TheRandomLabs/RandomPortals/misc/versions.json";
	public static final String CERTIFICATE_FINGERPRINT = "@FINGERPRINT@";

	public static final boolean CUBIC_CHUNKS_INSTALLED = Loader.isModLoaded("cubicchunks");
	public static final boolean MOVINGWORLD_INSTALLED = Loader.isModLoaded("movingworld");

	public static final Logger LOGGER = LogManager.getLogger(MOD_ID);

	@SidedProxy(
			clientSide = "com.therandomlabs.randomportals.ClientProxy",
			serverSide = "com.therandomlabs.randomportals.CommonProxy"
	)
	public static CommonProxy proxy;

	@Mod.EventHandler
	public static void preInit(FMLPreInitializationEvent event) {
		proxy.preInit();
	}

	@Mod.EventHandler
	public static void init(FMLInitializationEvent event) {
		proxy.init();
	}

	@Mod.EventHandler
	public static void serverStarting(FMLServerStartingEvent event) {
		if (RPOConfig.Misc.rporeloadCommand) {
			event.registerServerCommand(CommandConfigReload.server(
					"rporeload",
					"rporeloadclient",
					RPOConfig.class,
					null,
					(phase, command, sender) -> {
						if (phase == CommandConfigReload.ReloadPhase.POST) {
							CommandBase.notifyCommandListener(
									sender, command, "commands.rporeload.loadedPortalTypes",
									StringUtils.join(PortalTypes.getGroups().keySet(), ", ")
							);
						}
					}
			));
		}
	}
}
