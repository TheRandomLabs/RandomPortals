package com.therandomlabs.verticalendportals.block;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import com.google.common.collect.ImmutableList;
import com.therandomlabs.verticalendportals.VerticalEndPortals;
import com.therandomlabs.verticalendportals.config.VEPConfig;
import com.therandomlabs.verticalendportals.tileentity.TileEntityUpsideDownEndPortal;
import com.therandomlabs.verticalendportals.tileentity.TileEntityVerticalEndPortal;
import net.minecraft.block.Block;
import net.minecraft.block.BlockEndPortal;
import net.minecraft.block.BlockFire;
import net.minecraft.block.BlockPortal;
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

@GameRegistry.ObjectHolder(VerticalEndPortals.MOD_ID)
@Mod.EventBusSubscriber(modid = VerticalEndPortals.MOD_ID)
public final class VEPBlocks {
	@Mod.EventBusSubscriber(value = Side.CLIENT, modid = VerticalEndPortals.MOD_ID)
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

	@GameRegistry.ObjectHolder("minecraft:fire")
	public static final BlockFire fire = null;

	public static final BlockVerticalEndPortalFrame vertical_end_portal_frame = null;
	public static final BlockUpsideDownEndPortalFrame upside_down_end_portal_frame = null;

	@GameRegistry.ObjectHolder("minecraft:end_portal")
	public static final BlockEndPortal lateral_end_portal = null;

	public static final BlockVerticalEndPortal vertical_end_portal = null;
	public static final BlockUpsideDownEndPortal upside_down_end_portal = null;

	@GameRegistry.ObjectHolder("minecraft:portal")
	public static final BlockPortal vertical_nether_portal = null;

	public static final BlockLateralNetherPortal lateral_nether_portal = null;

	private static ImmutableList<Block> blocksWithItems;

	@SubscribeEvent
	public static void registerBlocks(RegistryEvent.Register<Block> event) {
		Blocks.END_PORTAL_FRAME.setTranslationKey("endPortalFrameLateral");

		final List<Block> blocksWithItems = new ArrayList<>();

		final IForgeRegistry<Block> registry = event.getRegistry();

		if(VEPConfig.endPortals.enabled) {
			Collections.addAll(
					blocksWithItems,
					new BlockVerticalEndPortalFrame(),
					new BlockUpsideDownEndPortalFrame(),
					new BlockLateralEndPortal(),
					new BlockVerticalEndPortal(),
					new BlockUpsideDownEndPortal()
			);
		}

		if(VEPConfig.netherPortals.enabled) {
			registry.register(new BlockVEPFire());

			Collections.addAll(
					blocksWithItems,
					new BlockNetherPortal(),
					new BlockLateralNetherPortal()
			);
		}

		for(Block block : blocksWithItems) {
			registry.register(block);
		}

		VEPBlocks.blocksWithItems = ImmutableList.copyOf(blocksWithItems);

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
				VerticalEndPortals.MOD_ID, key
		));
	}

	public static ImmutableList<Block> getBlocksWithItems() {
		return blocksWithItems;
	}
}
