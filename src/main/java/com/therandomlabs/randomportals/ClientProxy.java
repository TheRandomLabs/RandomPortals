package com.therandomlabs.randomportals;

import com.therandomlabs.randompatches.client.TileEntityEndPortalRenderer;
import com.therandomlabs.randomportals.client.creativetab.CreativeTabPortals;
import com.therandomlabs.randomportals.command.CommandRPOReload;
import com.therandomlabs.randomportals.config.RPOConfig;
import com.therandomlabs.randomportals.tileentity.TileEntityUpsideDownEndPortal;
import com.therandomlabs.randomportals.tileentity.TileEntityVerticalEndPortal;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.relauncher.Side;

public final class ClientProxy extends CommonProxy {
	@Override
	public void preInit() {
		super.preInit();

		if(RPOConfig.Client.rporeloadclientCommand) {
			ClientCommandHandler.instance.registerCommand(new CommandRPOReload(Side.CLIENT));
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
