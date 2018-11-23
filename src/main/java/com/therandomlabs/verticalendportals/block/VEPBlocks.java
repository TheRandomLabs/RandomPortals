package com.therandomlabs.verticalendportals.block;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import com.google.common.collect.ImmutableList;
import com.therandomlabs.verticalendportals.VerticalEndPortals;
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

@GameRegistry.ObjectHolder(VerticalEndPortals.MOD_ID)
@Mod.EventBusSubscriber(modid = VerticalEndPortals.MOD_ID)
public final class VEPBlocks {
	@Mod.EventBusSubscriber(value = Side.CLIENT, modid = VerticalEndPortals.MOD_ID)
	public static class ModelRegistrar {
		@SubscribeEvent
		public static void registerModels(ModelRegistryEvent event) {
			blocks.forEach(block -> ModelLoader.setCustomModelResourceLocation(
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

	private static final ArrayList<Block> blocks = new ArrayList<>();

	@SubscribeEvent
	public static void registerBlocks(RegistryEvent.Register<Block> event) {
		Blocks.END_PORTAL_FRAME.setTranslationKey("endPortalFrameLateral");

		event.getRegistry().registerAll(
				new BlockVEPFire(),
				new BlockVerticalEndPortalFrame(),
				new BlockUpsideDownEndPortalFrame(),
				new BlockLateralEndPortal(),
				new BlockVerticalEndPortal(),
				new BlockUpsideDownEndPortal(),
				new BlockNetherPortal(),
				new BlockLateralNetherPortal()
		);

		registerTileEntities();
	}

	@SubscribeEvent
	public static void registerItems(RegistryEvent.Register<Item> event) {
		BlockFire.init();

		try {
			for(Field field : VEPBlocks.class.getDeclaredFields()) {
				if(!"blocks".equals(field.getName()) && !"fire".equals(field.getName())) {
					blocks.add((Block) field.get(null));
				}
			}
		} catch(IllegalAccessException ex) {
			VerticalEndPortals.crashReport("Could not register blocks", ex);
		}

		blocks.stream().
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

	@SuppressWarnings("unchecked")
	public static List<Block> getBlocks() {
		return (List<Block>) blocks.clone();
	}
}
