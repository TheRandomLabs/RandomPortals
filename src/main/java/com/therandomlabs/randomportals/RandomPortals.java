package com.therandomlabs.randomportals;

import com.therandomlabs.randomportals.command.CommandRPOReload;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.relauncher.Side;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod.EventBusSubscriber(modid = RandomPortals.MOD_ID)
@Mod(
		modid = RandomPortals.MOD_ID, version = RandomPortals.VERSION,
		acceptedMinecraftVersions = RandomPortals.ACCEPTED_MINECRAFT_VERSIONS,
		dependencies = RandomPortals.DEPENDENCIES, updateJSON = RandomPortals.UPDATE_JSON,
		certificateFingerprint = RandomPortals.CERTIFICATE_FINGERPRINT
)
public final class RandomPortals {
	public static final String MOD_ID = "randomportals";
	public static final String VERSION = "@VERSION@";
	public static final String ACCEPTED_MINECRAFT_VERSIONS = "[1.12.2,1.13)";
	public static final String DEPENDENCIES = "required-after:randompatches@[1.12.2-1.9.1.2,)";
	public static final String UPDATE_JSON =
			"https://raw.githubusercontent.com/TheRandomLabs/RandomPortals/misc/versions.json";
	public static final String CERTIFICATE_FINGERPRINT = "@FINGERPRINT@";

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
		if(RPOConfig.misc.rporeloadCommand) {
			event.registerServerCommand(new CommandRPOReload(Side.SERVER));
		}
	}

	//Remove when 1.12.2-1.0.0.0 is released:

	@SubscribeEvent
	public static void onMissingBlockMappings(RegistryEvent.MissingMappings<Block> event) {
		for(RegistryEvent.MissingMappings.Mapping<Block> mapping : event.getAllMappings()) {
			if(mapping.key.getNamespace().equals("verticalendportals")) {
				mapping.remap(ForgeRegistries.BLOCKS.getValue(new ResourceLocation(
						MOD_ID, mapping.key.getPath()
				)));
			}
		}
	}

	@SubscribeEvent
	public static void onMissingItemMappings(RegistryEvent.MissingMappings<Item> event) {
		for(RegistryEvent.MissingMappings.Mapping<Item> mapping : event.getAllMappings()) {
			if(mapping.key.getNamespace().equals("verticalendportals")) {
				mapping.remap(ForgeRegistries.ITEMS.getValue(new ResourceLocation(
						MOD_ID, mapping.key.getPath()
				)));
			}
		}
	}
}
