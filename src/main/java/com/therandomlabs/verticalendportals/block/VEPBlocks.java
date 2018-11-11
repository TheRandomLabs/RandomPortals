package com.therandomlabs.verticalendportals.block;

import java.util.Arrays;
import java.util.List;
import com.therandomlabs.verticalendportals.VerticalEndPortals;
import com.therandomlabs.verticalendportals.tileentity.TileEntityVerticalEndPortal;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
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
	public static final BlockVerticalEndPortalFrame vertical_end_portal_frame = null;
	public static final BlockVerticalEndPortal vertical_end_portal = null;

	@Mod.EventBusSubscriber(value = Side.CLIENT, modid = VerticalEndPortals.MOD_ID)
	public static class ModelRegistrar {
		@SubscribeEvent
		public static void registerModels(ModelRegistryEvent event) {
			ModelLoader.setCustomModelResourceLocation(
					Item.getItemFromBlock(vertical_end_portal_frame),
					0,
					new ModelResourceLocation(
							vertical_end_portal_frame.getRegistryName(),
							"inventory"
					)
			);

			ModelLoader.setCustomModelResourceLocation(
					Item.getItemFromBlock(vertical_end_portal),
					0,
					new ModelResourceLocation(
							vertical_end_portal.getRegistryName(),
							"inventory"
					)
			);

			ModelLoader.setCustomModelResourceLocation(
					Item.getItemFromBlock(Blocks.END_PORTAL),
					0,
					new ModelResourceLocation(
							Blocks.END_PORTAL.getRegistryName(),
							"inventory"
					)
			);
		}
	}

	@SubscribeEvent
	public static void registerBlocks(RegistryEvent.Register<Block> event) {
		final IForgeRegistry<Block> registry = event.getRegistry();

		registry.register(new BlockVerticalEndPortalFrame());
		registry.register(new BlockVerticalEndPortal());

		GameRegistry.registerTileEntity(TileEntityVerticalEndPortal.class, new ResourceLocation(
				VerticalEndPortals.MOD_ID,
				"vertical_end_portal"
		));

		Blocks.END_PORTAL.setCreativeTab(CreativeTabs.DECORATIONS);
		Blocks.END_PORTAL.setTranslationKey("endPortalVertical");
	}

	@SubscribeEvent
	public static void registerItems(RegistryEvent.Register<Item> event) {
		final List<Block> blocks = Arrays.asList(
				vertical_end_portal_frame,
				vertical_end_portal,
				Blocks.END_PORTAL
		);

		blocks.stream().
				map(block -> new ItemBlock(block).setRegistryName(block.getRegistryName())).
				forEach(event.getRegistry()::register);
	}
}
