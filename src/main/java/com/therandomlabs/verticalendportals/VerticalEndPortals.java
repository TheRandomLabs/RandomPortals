package com.therandomlabs.verticalendportals;

import com.therandomlabs.verticalendportals.command.CommandVEPReload;
import net.minecraft.crash.CrashReport;
import net.minecraft.launchwrapper.Launch;
import net.minecraft.util.ReportedException;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLConstructionEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.relauncher.Side;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(
		modid = VerticalEndPortals.MOD_ID, version = VerticalEndPortals.VERSION,
		acceptedMinecraftVersions = VerticalEndPortals.ACCEPTED_MINECRAFT_VERSIONS,
		updateJSON = VerticalEndPortals.UPDATE_JSON,
		certificateFingerprint = VerticalEndPortals.CERTIFICATE_FINGERPRINT
)
public final class VerticalEndPortals {
	public static final String MOD_ID = "verticalendportals";
	public static final String VERSION = "@VERSION@";
	public static final String ACCEPTED_MINECRAFT_VERSIONS = "[1.12.2,1.13)";
	public static final String MINIMUM_RANDOMPATCHES_VERSION = "1.12.2-1.8.0.0";
	public static final String RANDOMPATCHES_VERSION_RANGE =
			"[" + MINIMUM_RANDOMPATCHES_VERSION + ",)";
	public static final String UPDATE_JSON = "https://raw.githubusercontent.com/TheRandomLabs/" +
			"Vertical-End-Portals/misc/versions.json";
	public static final String CERTIFICATE_FINGERPRINT = "@FINGERPRINT@";

	public static final Logger LOGGER = LogManager.getLogger(MOD_ID);

	public static final boolean IS_DEOBFUSCATED =
			(boolean) Launch.blackboard.get("fml.deobfuscatedEnvironment");

	@SidedProxy(
			clientSide = "com.therandomlabs.verticalendportals.ClientProxy",
			serverSide = "com.therandomlabs.verticalendportals.CommonProxy"
	)
	public static CommonProxy proxy;

	@Mod.EventHandler
	public static void construct(FMLConstructionEvent event) {
		proxy.construct();
	}

	@Mod.EventHandler
	public static void preInit(FMLPreInitializationEvent event) {
		proxy.preInit();
	}

	@Mod.EventHandler
	public static void serverStarting(FMLServerStartingEvent event) {
		if(VEPConfig.misc.vepreloadCommand) {
			event.registerServerCommand(new CommandVEPReload(Side.SERVER));
		}
	}

	public static void crashReport(String message, Throwable throwable) {
		throw new ReportedException(new CrashReport(message, throwable));
	}
}
