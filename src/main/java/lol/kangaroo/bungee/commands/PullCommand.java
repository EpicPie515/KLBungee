package lol.kangaroo.bungee.commands;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import lol.kangaroo.bungee.KLBungeePlugin;
import lol.kangaroo.bungee.player.PlayerManager;
import lol.kangaroo.bungee.util.Message;
import lol.kangaroo.common.permissions.Rank;
import lol.kangaroo.common.player.BasePlayer;
import lol.kangaroo.common.player.CachedPlayer;
import lol.kangaroo.common.player.PlayerVariable;
import lol.kangaroo.common.util.MSG;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public class PullCommand extends CommandExecutor {
	
	private KLBungeePlugin pl;

	public PullCommand(PlayerManager pm, ProxyServer proxy, KLBungeePlugin pl) {
		super(pm, proxy, "pull", Rank.SRMOD.getPerm(), "bring", "pullall", "spull", "sbring", "spullall");
		this.pl = pl;
	}
	
	private Map<UUID, Long> confirming = new HashMap<>();

	@Override
	public void execute(ProxiedPlayer sender, BasePlayer bp, String label, String[] args) {
		ServerInfo srv = sender.getServer().getInfo();
		String srvName = pl.getServerManager().formatServerName(pl.getServerManager().getServerID(srv.getName()));
		String senderName = pl.getRankManager().getPrefix(bp, true) + bp.getVariable(PlayerVariable.NICKNAME);
		if(args.length == 0) {
			if(label.equalsIgnoreCase("pullall") || label.equalsIgnoreCase("spullall")) {
				int pcount = 0;
				for(ProxiedPlayer op : proxy.getPlayers()) {
					// Exclude the current server in player count. (No need to be pulled, already there)
					if(op.getServer().getInfo().getName().equals(srv.getName()))
						continue;
					
					pcount++;
				}
				if(confirming.containsKey(bp.getUniqueId()) && System.currentTimeMillis() < (confirming.get(bp.getUniqueId()) + 30000)) {
					Message.sendMessage(bp, MSG.PREFIX_ADMIN, MSG.COMMAND_PULL_ALL, pcount, srvName);
					confirming.remove(bp.getUniqueId());
					for(ProxiedPlayer pp : proxy.getPlayers()) {
						// Already on the target server, no need to be pulled.
						if(pp.getServer().getInfo().getName().equals(srv.getName()))
							continue;
						
						if(label.equalsIgnoreCase("spullall"))
							Message.sendMessage(pp, MSG.PREFIX_PLAYER, MSG.COMMAND_PULL_PULLED_SILENT, srvName);
						else
							Message.sendMessage(pp, MSG.PREFIX_PLAYER, MSG.COMMAND_PULL_PULLED, srvName, senderName);
						pp.connect(srv);
					}
				} else {
					Message.sendMessage(bp, MSG.PREFIX_ADMIN, MSG.COMMAND_PULL_ALL_CONFIRM, pcount, srvName, label);
					confirming.put(bp.getUniqueId(), System.currentTimeMillis());
				}
			} else {
				Message.sendMessage(bp, MSG.PREFIX_ERROR, MSG.COMMAND_PULL_USAGE);
			}
			return;
		}
		CachedPlayer cp = pm.getCachedPlayer(pm.getFromCurrentNick(args[0]));
		if(cp == null) {
			Message.sendMessage(bp, MSG.PREFIX_ERROR, MSG.PLAYER_NOTFOUND);
			return;
		}
		ProxiedPlayer pp = proxy.getPlayer(cp.getUniqueId());
		if(pp == null) {
			Message.sendMessage(bp, MSG.PREFIX_ERROR, MSG.PLAYER_OFFLINE);
			return;
		}
		String prefix = pm.getRankManager().getPrefix(cp, true);
		String nickname = (String) cp.getVariable(PlayerVariable.NICKNAME);
		if(pp.getServer().getInfo().getName().equals(srv.getName())) {
			Message.sendMessage(bp, MSG.COMMAND_PULL_ALREADYTHERE, prefix + nickname, srvName);
			return;
		}
		Message.sendMessage(bp, MSG.PREFIX_ADMIN, MSG.COMMAND_PULL_PULL, prefix + nickname, srvName);
		if(label.toLowerCase().startsWith("s"))
			Message.sendMessage(cp, MSG.PREFIX_PLAYER, MSG.COMMAND_PULL_PULLED_SILENT, srvName);
		else
			Message.sendMessage(cp, MSG.PREFIX_PLAYER, MSG.COMMAND_PULL_PULLED, srvName, senderName);
		pp.connect(srv);
	}

	@Override
	public void executeConsole(String label, String[] args) {
		Message.sendConsole(MSG.PREFIX_ERROR, MSG.MUST_BE_PLAYER);
	}
	
}
