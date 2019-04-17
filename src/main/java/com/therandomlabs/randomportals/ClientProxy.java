package com.therandomlabs.randomportals;

import com.therandomlabs.randomlib.config.CommandConfigReload;
import com.therandomlabs.randompatches.client.TileEntityEndPortalRenderer;
import com.therandomlabs.randomportals.api.config.PortalTypes;
import com.therandomlabs.randomportals.client.creativetab.CreativeTabPortals;
import com.therandomlabs.randomportals.config.RPOConfig;
import com.therandomlabs.randomportals.tileentity.TileEntityUpsideDownEndPortal;
import com.therandomlabs.randomportals.tileentity.TileEntityVerticalEndPortal;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.relauncher.Side;
import org.apache.commons.lang3.StringUtils;

public final class ClientProxy extends CommonProxy {
	@Override
	public void preInit() {
		super.preInit();

		if(RPOConfig.Client.rporeloadclientCommand) {
			ClientCommandHandler.instance.registerCommand(new CommandConfigReload(
					"rporeloadclient", RPOConfig.class,
					(phase, command, sender) -> {
						if(phase == CommandConfigReload.ReloadPhase.POST) {
							sender.sendMessage(new TextComponentTranslation(
									"commands.rporeload.loadedPortalTypes",
									StringUtils.join(PortalTypes.getGroups().keySet(), ", ")
							));
						}
					}, Side.CLIENT
			));
		}

		if(RPOConfig.EndPortals.enabled) {
			ClientRegistry.bindTileEntitySpecialRenderer(
					TileEntityVerticalEndPortal.class, new TileEntityEndPortalRenderer()
			);

			ClientRegistry.bindTileEntitySpecialRenderer(
					TileEntityUpsideDownEndPortal.class, new TileEntityEndPortalRenderer(true)
			);
		}
	}

	@Override
	public void init() {
		super.init();
		CreativeTabPortals.init();
	}
}
