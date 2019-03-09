package com.therandomlabs.randomportals.config;

import com.therandomlabs.randomlib.config.TRLGuiConfigFactory;
import net.minecraftforge.fml.client.config.GuiConfig;

public class RPOGuiConfigFactory extends TRLGuiConfigFactory {
	@Override
	public Class<? extends GuiConfig> mainConfigGuiClass() {
		return RPOGuiConfig.class;
	}
}
