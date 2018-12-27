package com.therandomlabs.randomportals;

import com.therandomlabs.randompatches.client.TileEntityEndPortalRenderer;
import com.therandomlabs.randomportals.client.RPOPortalRenderer;
import com.therandomlabs.randomportals.command.CommandRPOReload;
import com.therandomlabs.randomportals.client.creativetab.CreativeTabPortals;
import com.therandomlabs.randomportals.tileentity.TileEntityUpsideDownEndPortal;
import com.therandomlabs.randomportals.tileentity.TileEntityVerticalEndPortal;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.relauncher.Side;

public final class ClientProxy extends CommonProxy {
	@Override
	public void preInit() {
		super.preInit();

		if(RPOConfig.client.rporeloadclientCommand) {
			ClientCommandHandler.instance.registerCommand(new CommandRPOReload(Side.CLIENT));
		}

		if(RPOConfig.endPortals.enabled) {
			ClientRegistry.bindTileEntitySpecialRenderer(
					TileEntityVerticalEndPortal.class, new TileEntityEndPortalRenderer()
			);

			ClientRegistry.bindTileEntitySpecialRenderer(
					TileEntityUpsideDownEndPortal.class, new TileEntityEndPortalRenderer(true)
			);
		}

		if(RPOConfig.netherPortals.enabled) {
			RPOPortalRenderer.register();
		}
	}

	@Override
	public void init() {
		super.init();
		CreativeTabPortals.init();
	}
}
