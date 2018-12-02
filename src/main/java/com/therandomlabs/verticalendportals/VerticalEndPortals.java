package com.therandomlabs.verticalendportals;

import com.therandomlabs.verticalendportals.command.CommandVEPReload;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.relauncher.Side;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(
		modid = VerticalEndPortals.MOD_ID, version = VerticalEndPortals.VERSION,
		acceptedMinecraftVersions = VerticalEndPortals.ACCEPTED_MINECRAFT_VERSIONS,
		dependencies = VerticalEndPortals.DEPENDENCIES, updateJSON = VerticalEndPortals.UPDATE_JSON,
		certificateFingerprint = VerticalEndPortals.CERTIFICATE_FINGERPRINT
)
public final class VerticalEndPortals {
	public static final String MOD_ID = "verticalendportals";
	public static final String VERSION = "@VERSION@";
	public static final String ACCEPTED_MINECRAFT_VERSIONS = "[1.12.2,1.13)";
	public static final String DEPENDENCIES = "required-after:randompatches@[1.12.2-1.9.0.1,)";
	public static final String UPDATE_JSON = "https://raw.githubusercontent.com/TheRandomLabs/" +
			"Vertical-End-Portals/misc/versions.json";
	public static final String CERTIFICATE_FINGERPRINT = "@FINGERPRINT@";

	public static final Logger LOGGER = LogManager.getLogger(MOD_ID);

	@SidedProxy(
			clientSide = "com.therandomlabs.verticalendportals.ClientProxy",
			serverSide = "com.therandomlabs.verticalendportals.CommonProxy"
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
		if(VEPConfig.misc.vepreloadCommand) {
			event.registerServerCommand(new CommandVEPReload(Side.SERVER));
		}
	}
}
