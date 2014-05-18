package me.sablednah.MinerBuddyServer;

import java.io.IOException;

import me.sablednah.MinerBuddyServer.MinerBuddyServer;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Commands implements CommandExecutor {
	public MinerBuddyServer plugin;

	public Commands(MinerBuddyServer i) {
		this.plugin=i;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if(command.getName().equalsIgnoreCase("minerbuddyserver") || command.getName().equalsIgnoreCase("mbs")){

			if (args.length > 0 && args[0].toLowerCase().equals("reload")) {
				Boolean doReload = false;
				
				if (sender instanceof Player) {
					if (sender.hasPermission("minerbuddyserver.command.reload")) {
						doReload = true;
					} else {
						sender.sendMessage("You do not have permission to reload.");
						return true;
					}
				} else {
					doReload = true;
				}

				if (doReload) {
					plugin.reloadConfig();
					plugin.loadConfiguration();

					plugin.stopServer=true;
					try {
						plugin.getConnectionsocket().close();
						plugin.getServersocket().close();
					} catch (IOException e) {}
					plugin.serverTaskID.cancel();
					
					plugin.stopServer=false;
					plugin.serverTaskID = plugin.getServer().getScheduler().runTaskAsynchronously(plugin, new RESTserver(plugin));
					
					return true;
				}
			}
		}

		return false; 
	}
}

