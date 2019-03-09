package com.therandomlabs.randomportals.command;

import com.therandomlabs.randomlib.config.ConfigManager;
import com.therandomlabs.randomportals.config.RPOConfig;
import com.therandomlabs.randomportals.api.config.PortalTypes;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.fml.relauncher.Side;
import org.apache.commons.lang3.StringUtils;

public class CommandRPOReload extends CommandBase {
	private final boolean isClient;

	public CommandRPOReload(Side side) {
		isClient = side.isClient();
	}

	@Override
	public String getName() {
		return isClient ? "rporeloadclient" : "rporeload";
	}

	@Override
	public String getUsage(ICommandSender sender) {
		return "commands." + getName() + ".usage";
	}

	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args)
			throws CommandException {
		ConfigManager.reloadFromDisk(RPOConfig.class);
		RPOConfig.reload();

		final String loadedPortalTypes = StringUtils.join(PortalTypes.getGroups().keySet(), ", ");

		if(server != null && server.isDedicatedServer()) {
			notifyCommandListener(sender, this, "commands.rporeload.success");
			notifyCommandListener(
					sender, this, "commands.rporeload.loadedPortalTypes", loadedPortalTypes
			);
		} else {
			sender.sendMessage(new TextComponentTranslation("commands.rporeloadclient.success"));
			sender.sendMessage(new TextComponentTranslation(
					"commands.rporeload.loadedPortalTypes", loadedPortalTypes
			));
		}
	}

	@Override
	public int getRequiredPermissionLevel() {
		return isClient ? 0 : 4;
	}
}
