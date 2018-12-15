package com.therandomlabs.randomportals;

import java.io.IOException;
import java.lang.reflect.Field;
import com.elytradev.movingworld.MovingWorldMod;
import com.elytradev.movingworld.common.config.priority.AssemblePriorityConfig;
import com.therandomlabs.randompatches.util.RPUtils;
import com.therandomlabs.randomportals.api.config.FrameSizes;
import com.therandomlabs.randomportals.api.config.NetherPortalTypes;
import com.therandomlabs.randomportals.frame.NetherPortalFrames;
import com.therandomlabs.randomportals.frame.endportal.EndPortalFrames;
import com.therandomlabs.randomportals.handler.EndPortalActivationHandler;
import com.therandomlabs.randomportals.handler.FrameHeadVillagerSpawnHandler;
import com.therandomlabs.randomportals.handler.NetherPortalFrameBreakHandler;
import com.therandomlabs.randomportals.handler.NetherPortalTeleportHandler;
import com.therandomlabs.randomportals.world.RPOTeleporter;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraftforge.common.MinecraftForge;

public class CommonProxy {
	public void preInit() {
		RPOConfig.reload();
		RPOTeleporter.register();
	}

	public void init() {
		if(RandomPortals.MOVINGWORLD_INSTALLED) {
			try {
				handleMovingWorld();
			} catch(NoSuchFieldException | IllegalAccessException ex) {
				RandomPortals.LOGGER.error("Failed to fix MovingWorld compatibility", ex);
			}
		}

		if(RPOConfig.endPortals.enabled) {
			MinecraftForge.EVENT_BUS.register(EndPortalActivationHandler.class);
			MinecraftForge.EVENT_BUS.register(FrameHeadVillagerSpawnHandler.class);
		}

		if(RPOConfig.netherPortals.enabled) {
			MinecraftForge.EVENT_BUS.register(NetherPortalTeleportHandler.class);
			MinecraftForge.EVENT_BUS.register(NetherPortalFrameBreakHandler.class);
		}

		EndPortalFrames.registerSizes();
		NetherPortalFrames.registerSizes();
		FrameSizes.reload();

		try {
			NetherPortalTypes.reload();
		} catch(IOException ex) {
			RPUtils.crashReport("Error while reloading Nether portal types", ex);
		}
	}

	private void handleMovingWorld() throws NoSuchFieldException, IllegalAccessException {
		//https://github.com/elytra/MovingWorld/blob/1c547d75d9e681473cbc04a58dc6b803d5ef19fa/
		//src/main/java/com/elytradev/movingworld/common/config/priority/
		//AssemblePriorityConfig.java
		//MovingWorld loads Blocks.PORTAL and Blocks.END_PORTAL in preInit before
		//RandomPortals can register its replacements
		//It then uses the vanilla portal and End portal in init, causing an NPE
		final AssemblePriorityConfig config =
				MovingWorldMod.INSTANCE.getLocalConfig().getShared().assemblePriorityConfig;
		final Class<?> clazz = AssemblePriorityConfig.class;

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
