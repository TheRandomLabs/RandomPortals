package com.therandomlabs.verticalendportals.command;

import com.therandomlabs.verticalendportals.config.VEPConfig;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.fml.relauncher.Side;

public class CommandVEPReload extends CommandBase {
	private final boolean isClient;

	public CommandVEPReload(Side side) {
		isClient = side.isClient();
	}

	@Override
	public String getName() {
		return isClient ? "vepreloadclient" : "vepreload";
	}

	@Override
	public String getUsage(ICommandSender sender) {
		return isClient ? "commands.vepreloadclient.usage" : "/vepreload";
	}

	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args)
			throws CommandException {
		VEPConfig.reloadFromDisk();

		if(server.isDedicatedServer()) {
			notifyCommandListener(sender, this, "Vertical End Portals configuration reloaded!");
		} else {
			sender.sendMessage(new TextComponentTranslation("commands.vepreloadclient.success"));
		}
	}

	@Override
	public int getRequiredPermissionLevel() {
		return isClient ? 0 : 4;
	}
}
