package lol.kangaroo.bungee.commands.player;

import lol.kangaroo.bungee.KLBungeePlugin;
import lol.kangaroo.bungee.commands.CommandExecutor;
import lol.kangaroo.bungee.player.PlayerManager;
import lol.kangaroo.bungee.servers.ServerType;
import lol.kangaroo.bungee.util.Message;
import lol.kangaroo.common.permissions.Rank;
import lol.kangaroo.common.player.BasePlayer;
import lol.kangaroo.common.util.MSG;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public class HubCommand extends CommandExecutor {

	private KLBungeePlugin pl;
	
	public HubCommand(PlayerManager pm, ProxyServer proxy, KLBungeePlugin pl) {
		super(pm, proxy, "hub", Rank.PLAYER.getPerm(), "lobby", "hub1", "lobby1");
		this.pl = pl;
	}

	@Override
	public void execute(ProxiedPlayer sender, BasePlayer bp, String label, String[] args) {
		int srv = pl.getServerManager().getServerID(sender.getServer().getInfo().getName());
		String srvName = pl.getServerManager().formatServerName(srv);
		if(args.length == 0 && !label.endsWith("1")) {
			connectAutoHub(sender, bp, srv, srvName);
			return;
		}
		int hubNumber = 0;
		if(label.endsWith("1")) hubNumber = 1;
		else if(args.length > 0) {
			try {
				hubNumber = Integer.parseInt(args[0]);
			} catch(NumberFormatException e) {
				connectAutoHub(sender, bp, srv, srvName);
				return;
			}
		} else {
			connectAutoHub(sender, bp, srv, srvName);
			return;
		}
		if(hubNumber == 0) {
			connectAutoHub(sender, bp, srv, srvName);
			return;
		}
		String attHubName = String.format("%s%02d", ServerType.HUB.getNamePrefix(), hubNumber);
		int hub = 0;
		try {
			hub = pl.getServerManager().getServerID(attHubName);
		} catch(IllegalArgumentException e) {
			connectAutoHub(sender, bp, srv, srvName);
			return;
		}
		if(hub == 0) {
			connectAutoHub(sender, bp, srv, srvName);
			return;
		}
		if(!pl.getServerManager().isServerOnline(hub)) {
			connectAutoHubFallback(sender, bp, srv, srvName, hub);
			return;
		}
		if(pl.getServerManager().isServerFull(hub)) {
			connectAutoHubFallback(sender, bp, srv, srvName, hub);
			return;
		}
		String hubName = pl.getServerManager().formatServerName(hub);
		Message.sendMessage(bp, MSG.PREFIX_PLAYER, MSG.PLAYER_SERVER_CONNECTING, hubName);
		sender.connect(pl.getServerManager().getServerInfo(hub));
	}
	
	private void connectAutoHub(ProxiedPlayer sender, BasePlayer bp, int srv, String srvName) {
		if(pl.getServerManager().isHub(srv)) {
			Message.sendMessage(bp, MSG.PREFIX_ERROR, MSG.PLAYER_SERVER_CONNECTED_ALREADY, srvName);
			return;
		}
		int hub = pl.getServerManager().findAvailableHub();
		String hubName = pl.getServerManager().formatServerName(hub);
		Message.sendMessage(bp, MSG.PREFIX_PLAYER, MSG.PLAYER_SERVER_CONNECTING, hubName);
		sender.connect(pl.getServerManager().getServerInfo(hub));
	}
	
	private void connectAutoHubFallback(ProxiedPlayer sender, BasePlayer bp, int srv, String srvName, int intended) {
		String intName = pl.getServerManager().formatServerName(intended);
		if(pl.getServerManager().isHub(srv)) {
			Message.sendMessage(bp, MSG.PREFIX_ERROR, MSG.COMMAND_HUB_UNAVAILABLE_STAYING, intName, srvName);
			return;
		}
		int hub = pl.getServerManager().findAvailableHub();
		String hubName = pl.getServerManager().formatServerName(hub);
		Message.sendMessage(bp, MSG.PREFIX_PLAYER, MSG.COMMAND_HUB_UNAVAILABLE_CONNECTING, intName, hubName);
		sender.connect(pl.getServerManager().getServerInfo(hub));
	}

	@Override
	public void executeConsole(String label, String[] args) {
		Message.sendConsole(MSG.PREFIX_ERROR, MSG.MUST_BE_PLAYER);
	}
	
}
