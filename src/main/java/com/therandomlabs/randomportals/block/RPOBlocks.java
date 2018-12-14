package com.therandomlabs.randomportals.block;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import com.google.common.collect.ImmutableList;
import com.therandomlabs.randomportals.RPOConfig;
import com.therandomlabs.randomportals.RandomPortals;
import com.therandomlabs.randomportals.tileentity.TileEntityUpsideDownEndPortal;
import com.therandomlabs.randomportals.tileentity.TileEntityVerticalEndPortal;
import net.minecraft.block.Block;
import net.minecraft.block.BlockFire;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.registries.IForgeRegistry;

@GameRegistry.ObjectHolder(RandomPortals.MOD_ID)
@Mod.EventBusSubscriber(modid = RandomPortals.MOD_ID)
public final class RPOBlocks {
	@Mod.EventBusSubscriber(value = Side.CLIENT, modid = RandomPortals.MOD_ID)
	public static class ModelRegistrar {
		@SubscribeEvent
		public static void registerModels(ModelRegistryEvent event) {
			blocksWithItems.forEach(block -> ModelLoader.setCustomModelResourceLocation(
					Item.getItemFromBlock(block), 0, new ModelResourceLocation(
							block.getRegistryName(), "inventory"
					)
			));
		}
	}

	public static final BlockVerticalEndPortalFrame vertical_end_portal_frame = null;
	public static final BlockUpsideDownEndPortalFrame upside_down_end_portal_frame = null;

	public static final BlockVerticalEndPortal vertical_end_portal = null;
	public static final BlockUpsideDownEndPortal upside_down_end_portal = null;

	public static final BlockLateralNetherPortal lateral_nether_portal = null;

	private static ImmutableList<Block> blocksWithItems;

	@SubscribeEvent
	public static void registerBlocks(RegistryEvent.Register<Block> event) {
		Blocks.END_PORTAL_FRAME.setTranslationKey("endPortalFrameLateral");

		final List<Block> blocksWithItems = new ArrayList<>();
		final IForgeRegistry<Block> registry = event.getRegistry();

		if(RPOConfig.endPortals.enabled) {
			Collections.addAll(
					blocksWithItems,
					new BlockVerticalEndPortalFrame(),
					new BlockUpsideDownEndPortalFrame(),
					new BlockLateralEndPortal(),
					new BlockVerticalEndPortal(),
					new BlockUpsideDownEndPortal()
			);
		}

		if(RPOConfig.netherPortals.enabled) {
			registry.register(new BlockRPOFire());

			Collections.addAll(
					blocksWithItems,
					new BlockNetherPortal(),
					new BlockLateralNetherPortal()
			);
		}

		for(Block block : blocksWithItems) {
			registry.register(block);
		}

		RPOBlocks.blocksWithItems = ImmutableList.copyOf(blocksWithItems);
		registerTileEntities();
	}

	@SubscribeEvent
	public static void registerItems(RegistryEvent.Register<Item> event) {
		BlockFire.init();

		blocksWithItems.stream().
				map(block -> new ItemBlock(block).setRegistryName(block.getRegistryName())).
				forEach(event.getRegistry()::register);
	}

	public static void registerTileEntities() {
		registerTileEntity(TileEntityVerticalEndPortal.class, "vertical_end_portal");
		registerTileEntity(TileEntityUpsideDownEndPortal.class, "upside_down_end_portal");
	}

	public static void registerTileEntity(Class<? extends TileEntity> clazz, String key) {
		GameRegistry.registerTileEntity(clazz, new ResourceLocation(
				RandomPortals.MOD_ID, key
		));
	}

	public static ImmutableList<Block> getBlocksWithItems() {
		return blocksWithItems;
	}
}
