package com.therandomlabs.verticalendportals;

import com.therandomlabs.randompatches.RandomPatches;
import com.therandomlabs.randompatches.TileEntityEndPortalRenderer;
import com.therandomlabs.verticalendportals.command.CommandVEPReload;
import com.therandomlabs.verticalendportals.item.CreativeTabPortals;
import com.therandomlabs.verticalendportals.tileentity.TileEntityUpsideDownEndPortal;
import com.therandomlabs.verticalendportals.tileentity.TileEntityVerticalEndPortal;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.versioning.ArtifactVersion;
import net.minecraftforge.fml.common.versioning.DefaultArtifactVersion;
import net.minecraftforge.fml.common.versioning.VersionParser;
import net.minecraftforge.fml.common.versioning.VersionRange;
import net.minecraftforge.fml.relauncher.Side;

public final class ClientProxy extends CommonProxy {
	@Override
	public void construct() {
		super.construct();

		boolean randomPatchesInstalled = false;

		if(Loader.isModLoaded("randompatches")) {
			final VersionRange range =
					VersionParser.parseRange(VerticalEndPortals.RANDOMPATCHES_VERSION_RANGE);
			final ArtifactVersion version =
					new DefaultArtifactVersion(RandomPatches.MOD_ID, RandomPatches.VERSION);

			randomPatchesInstalled = range.containsVersion(version);
		}

		if(!randomPatchesInstalled) {
			VerticalEndPortals.crashReport(
					"RandomPatches " + VerticalEndPortals.MINIMUM_RANDOMPATCHES_VERSION +
							" or higher must be installed on the client",
					new IllegalStateException()
			);
		}
	}

	@Override
	public void preInit() {
		super.preInit();

		if(VEPConfig.client.vepreloadclientCommand) {
			ClientCommandHandler.instance.registerCommand(new CommandVEPReload(Side.CLIENT));
		}

		ClientRegistry.bindTileEntitySpecialRenderer(
				TileEntityVerticalEndPortal.class, new TileEntityEndPortalRenderer()
		);

		ClientRegistry.bindTileEntitySpecialRenderer(
				TileEntityUpsideDownEndPortal.class, new TileEntityEndPortalRenderer(true)
		);
	}

	@Override
	public void init() {
		super.init();

		CreativeTabPortals.init();
	}
}
