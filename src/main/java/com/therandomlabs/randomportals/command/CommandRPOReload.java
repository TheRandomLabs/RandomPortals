package com.therandomlabs.randomportals.command;

import com.therandomlabs.randomportals.RPOConfig;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.fml.relauncher.Side;

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
		return isClient ? "commands.rporeloadclient.usage" : "/rporeload";
	}

	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args)
			throws CommandException {
		RPOConfig.reloadFromDisk();

		if(server != null && server.isDedicatedServer()) {
			notifyCommandListener(sender, this, "RandomPortals configuration reloaded!");
		} else {
			sender.sendMessage(new TextComponentTranslation("commands.rporeloadclient.success"));
		}
	}

	@Override
	public int getRequiredPermissionLevel() {
		return isClient ? 0 : 4;
	}
}
