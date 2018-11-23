package com.therandomlabs.verticalendportals.item;

import java.lang.reflect.Field;
import com.therandomlabs.verticalendportals.VEPConfig;
import com.therandomlabs.verticalendportals.VerticalEndPortals;
import com.therandomlabs.verticalendportals.block.VEPBlocks;
import net.minecraft.block.Block;
import net.minecraft.client.gui.inventory.GuiContainerCreative;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.lang3.ArrayUtils;

//A lot of this is there just so creative tab configuration options can be toggled in-game
//Worth it? Maybe
@Mod.EventBusSubscriber(value = Side.CLIENT, modid = VerticalEndPortals.MOD_ID)
public final class CreativeTabPortals extends CreativeTabs {
	public static final CreativeTabs INSTANCE = new CreativeTabPortals();

	public static final Field TAB_PAGE =
			ReflectionHelper.findField(GuiContainerCreative.class, "tabPage");

	private static CreativeTabs originalLateralEndPortalFrameTab;
	private static boolean firstInit;

	private CreativeTabPortals() {
		super("portals");
	}

	@SideOnly(Side.CLIENT)
	@Override
	public ItemStack createIcon() {
		return new ItemStack(Blocks.END_PORTAL_FRAME);
	}

	@SubscribeEvent
	public static void onConfigChanged(ConfigChangedEvent.PostConfigChangedEvent event) {
		if(event.getModID().equals(VerticalEndPortals.MOD_ID)) {
			init();
		}
	}

	public static void init() {
		register();

		if(VEPConfig.client.portalsCreativeTab) {
			originalLateralEndPortalFrameTab = Blocks.END_PORTAL_FRAME.getCreativeTab();
			firstInit = true;

			Blocks.END_PORTAL_FRAME.setCreativeTab(INSTANCE);

			for(Block block : VEPBlocks.getBlocks()) {
				block.setCreativeTab(INSTANCE);
			}
		} else if(firstInit) {
			Blocks.END_PORTAL_FRAME.setCreativeTab(originalLateralEndPortalFrameTab);

			for(Block block : VEPBlocks.getBlocks()) {
				block.setCreativeTab(DECORATIONS);
			}
		}
	}

	private static void register() {
		if(VEPConfig.client.portalsCreativeTab) {
			if(!ArrayUtils.contains(CreativeTabs.CREATIVE_TAB_ARRAY, INSTANCE)) {
				CreativeTabs.CREATIVE_TAB_ARRAY =
						ArrayUtils.add(CreativeTabs.CREATIVE_TAB_ARRAY, INSTANCE);
				INSTANCE.index = CreativeTabs.CREATIVE_TAB_ARRAY.length - 1;
			}

			return;
		}

		if(!firstInit) {
			return;
		}

		final int index = ArrayUtils.indexOf(CreativeTabs.CREATIVE_TAB_ARRAY, INSTANCE);

		if(index != ArrayUtils.INDEX_NOT_FOUND) {
			CreativeTabs.CREATIVE_TAB_ARRAY =
					ArrayUtils.remove(CreativeTabs.CREATIVE_TAB_ARRAY, index);
			GuiContainerCreative.selectedTabIndex = CreativeTabs.BUILDING_BLOCKS.index;

			try {
				TAB_PAGE.set(null, 0);
			} catch(Exception ex) {
				VerticalEndPortals.crashReport("Error while disabling creative tab", ex);
			}
		}
	}
}
