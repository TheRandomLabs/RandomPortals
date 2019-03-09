package com.therandomlabs.randomportals.config;

import com.therandomlabs.randomlib.config.ConfigManager;
import com.therandomlabs.randomportals.RandomPortals;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.fml.client.config.GuiConfig;

public class RPOGuiConfig extends GuiConfig {
	public RPOGuiConfig(GuiScreen parentScreen) {
		super(
				parentScreen,
				ConfigManager.getConfigElements(RPOConfig.class),
				RandomPortals.MOD_ID,
				RandomPortals.MOD_ID,
				false,
				false,
				ConfigManager.getPathString(RPOConfig.class)
		);
	}
}
