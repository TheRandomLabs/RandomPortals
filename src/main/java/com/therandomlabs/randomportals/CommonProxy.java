package com.therandomlabs.randomportals;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import com.therandomlabs.randomlib.TRLUtils;
import com.therandomlabs.randomlib.config.ConfigManager;
import com.therandomlabs.randomportals.advancements.RPOCriteriaTriggers;
import com.therandomlabs.randomportals.api.config.FrameSizes;
import com.therandomlabs.randomportals.api.config.PortalTypes;
import com.therandomlabs.randomportals.config.RPOConfig;
import com.therandomlabs.randomportals.frame.endportal.EndPortalFrames;
import com.therandomlabs.randomportals.handler.EndPortalActivationHandler;
import com.therandomlabs.randomportals.handler.FrameHeadVillagerHandler;
import com.therandomlabs.randomportals.handler.NetherPortalActivationHandler;
import com.therandomlabs.randomportals.handler.NetherPortalFrameBreakHandler;
import com.therandomlabs.randomportals.handler.NetherPortalTeleportHandler;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraftforge.common.MinecraftForge;

public class CommonProxy {
	public void preInit() {
		ConfigManager.register(RPOConfig.class);
	}

	public void init() {
		RPOConfig.reload();

		if(RandomPortals.MOVINGWORLD_INSTALLED) {
			try {
				handleMovingWorld();
			} catch(ClassNotFoundException | NoSuchFieldException | NoSuchMethodException |
					IllegalAccessException | InvocationTargetException ex) {
				RandomPortals.LOGGER.error("Failed to fix MovingWorld compatibility", ex);
			}
		}

		if(RPOConfig.EndPortals.enabled) {
			MinecraftForge.EVENT_BUS.register(EndPortalActivationHandler.class);
			MinecraftForge.EVENT_BUS.register(FrameHeadVillagerHandler.class);
		}

		if(RPOConfig.NetherPortals.enabled) {
			MinecraftForge.EVENT_BUS.register(NetherPortalTeleportHandler.class);
			MinecraftForge.EVENT_BUS.register(NetherPortalFrameBreakHandler.class);
			MinecraftForge.EVENT_BUS.register(NetherPortalActivationHandler.class);
		}

		EndPortalFrames.registerSizes();
		FrameSizes.reload();

		try {
			PortalTypes.reload();
		} catch(IOException ex) {
			TRLUtils.crashReport("Error while reloading Nether portal types", ex);
		}

		RPOCriteriaTriggers.register();
	}

	private void handleMovingWorld() throws ClassNotFoundException, NoSuchFieldException,
			NoSuchMethodException, IllegalAccessException, InvocationTargetException {
		//https://github.com/elytra/MovingWorld/blob/1c547d75d9e681473cbc04a58dc6b803d5ef19fa/
		//src/main/java/com/elytradev/movingworld/common/config/priority/
		//AssemblePriorityConfig.java
		//MovingWorld loads Blocks.PORTAL and Blocks.END_PORTAL in preInit before
		//RandomPortals can register its replacements
		//It then uses the vanilla portal and End portal in init, causing an NPE
		final Class<?> movingWorldMod = Class.forName("com.elytradev.movingworld.MovingWorldMod");
		final Object movingWorldInstance = movingWorldMod.getDeclaredField("INSTANCE").get(null);

		final Object localConfigInstance =
				movingWorldMod.getDeclaredMethod("getLocalConfig").invoke(movingWorldInstance);
		final Class<?> mainConfig = localConfigInstance.getClass();

		final Object sharedConfigInstance =
				mainConfig.getDeclaredMethod("getShared").invoke(localConfigInstance);
		final Class<?> sharedConfig = sharedConfigInstance.getClass();

		final Object config =
				sharedConfig.getDeclaredField("assemblePriorityConfig").get(sharedConfigInstance);
		final Class<?> clazz = config.getClass();

		final Field defaultHighPriorityAssemblyBlocks =
				clazz.getDeclaredField("defaultHighPriorityAssemblyBlocks");
		final Field defaultLowPriorityDisassemblyBlocks =
				clazz.getDeclaredField("defaultLowPriorityDisassemblyBlocks");

		defaultHighPriorityAssemblyBlocks.setAccessible(true);
		defaultLowPriorityDisassemblyBlocks.setAccessible(true);

		final Block[] blocks1 = (Block[]) defaultHighPriorityAssemblyBlocks.get(config);
		final Block[] blocks2 = (Block[]) defaultLowPriorityDisassemblyBlocks.get(config);

		replace(blocks1);
		replace(blocks2);
	}

	private void replace(Block[] blocks) {
		for(int i = 0; i < blocks.length; i++) {
			final String registryName = blocks[i].getRegistryName().toString();

			if(registryName.equals("minecraft:portal")) {
				blocks[i] = Blocks.PORTAL;
			} else if(registryName.equals("minecraft:end_portal")) {
				blocks[i] = Blocks.END_PORTAL;
			}
		}
	}
}
