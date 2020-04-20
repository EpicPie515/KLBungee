package lol.kangaroo.bungee.commands.player;

import java.util.regex.Matcher;

import lol.kangaroo.bungee.KLBungeePlugin;
import lol.kangaroo.bungee.commands.CommandExecutor;
import lol.kangaroo.bungee.player.PlayerManager;
import lol.kangaroo.bungee.servers.ServerManager;
import lol.kangaroo.bungee.util.Message;
import lol.kangaroo.bungee.util.PluginMessage;
import lol.kangaroo.common.permissions.Rank;
import lol.kangaroo.common.player.BasePlayer;
import lol.kangaroo.common.util.MSG;
import lol.kangaroo.common.util.MessageWrapper;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public class ServerCommand extends CommandExecutor {

	private KLBungeePlugin pl;
	
	public ServerCommand(PlayerManager pm, ProxyServer proxy, KLBungeePlugin pl) {
		super(pm, proxy, "server", Rank.PLAYER.getPerm(), "connect", "whereami");
		this.pl = pl;
	}

	@Override
	public void execute(ProxiedPlayer sender, BasePlayer bp, String label, String[] args) {
		if(args.length == 0 || label.equalsIgnoreCase("whereami")) {
			int srv = pl.getServerManager().getServerID(sender.getServer().getInfo().getName());
			if(pl.getServerManager().isHub(srv) && (label.equalsIgnoreCase("server") || label.equalsIgnoreCase("connect"))) {
				PluginMessage.sendToSpigot(sender, "CommandGUI", new MessageWrapper("Server"));
			} else {
				Message.sendMessage(bp, MSG.PREFIX_ERROR, MSG.COMMAND_SERVER_CURRENT, pl.getServerManager().formatServerName(srv), pl.getServerManager().getServerName(srv));
			}
			return;
		}
		String a = args[0];
		Matcher aMatch = ServerManager.SERVER_NAME_PATTERN.matcher(a);
		int srv = 0;
		if(aMatch.matches()) {
			srv = pl.getServerManager().getServerID(a);
		} else {
			try {
				srv = Integer.parseInt(a);
			} catch(NumberFormatException e) {
				srv = 0;
			}
		}
		if(srv != 0) {
			if(sender.getServer().getInfo().getName().equals(pl.getServerManager().getServerName(srv))) {
				Message.sendMessage(bp, MSG.PREFIX_ERROR, MSG.PLAYER_SERVER_CONNECTED_ALREADY, pl.getServerManager().getServerName(srv));
				return;
			}
			Message.sendMessage(bp, MSG.PREFIX_PLAYER, MSG.PLAYER_SERVER_CONNECTING, pl.getServerManager().getServerName(srv));
			sender.connect(pl.getServerManager().getServerInfo(srv));
		} else {
			Message.sendMessage(bp, MSG.PREFIX_ERROR, MSG.COMMAND_SERVER_UNKNOWN);
		}
	}

	@Override
	public void executeConsole(String label, String[] args) {
		Message.sendConsole(MSG.PREFIX_ERROR, MSG.MUST_BE_PLAYER);
	}
	
}
