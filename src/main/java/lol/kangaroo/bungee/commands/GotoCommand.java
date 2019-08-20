package lol.kangaroo.bungee.commands;

import lol.kangaroo.bungee.KLBungeePlugin;
import lol.kangaroo.bungee.player.PlayerManager;
import lol.kangaroo.bungee.servers.ServerManager;
import lol.kangaroo.bungee.util.Message;
import lol.kangaroo.common.permissions.Rank;
import lol.kangaroo.common.player.BasePlayer;
import lol.kangaroo.common.player.CachedPlayer;
import lol.kangaroo.common.player.PlayerVariable;
import lol.kangaroo.common.util.MSG;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public class GotoCommand extends CommandExecutor {
	
	public GotoCommand(PlayerManager pm, ProxyServer proxy) {
		super(pm, proxy, "goto", Rank.JRMOD.getPerm());
	}

	@Override
	public void execute(ProxiedPlayer sender, BasePlayer bp, String label, String[] args) {
		if(args.length == 0) {
			Message.sendMessage(bp, MSG.PREFIX_ERROR, MSG.COMMAND_GOTO_USAGE);
			return;
		}
		CachedPlayer cp = pm.getCachedPlayer(pm.getFromCurrentNick(args[0]));
		if(cp == null) {
			Message.sendMessage(bp, MSG.PREFIX_ERROR, MSG.PLAYER_NOTFOUND);
			return;
		}
		String prefix = pm.getRankManager().getPrefix(cp);
		String nickname = (String) cp.getVariable(PlayerVariable.NICKNAME);
		Message.sendMessage(bp, MSG.PREFIX_ADMIN, MSG.COMMAND_GOTO_FINDING, prefix + nickname);
		ProxiedPlayer pp = proxy.getPlayer(cp.getUniqueId());
		if(pp == null) {
			Message.sendMessage(bp, MSG.PREFIX_ERROR, MSG.PLAYER_OFFLINE);
			return;
		}
		ServerInfo srv = pp.getServer().getInfo();
		ServerManager sm = KLBungeePlugin.instance.getServerManager();
		Message.sendMessage(bp, MSG.PREFIX_ADMIN, MSG.COMMAND_GOTO_FOUND, prefix + nickname, sm.formatServerName(sm.getServerID(srv.getName())));
		if(srv.getName().equals(sender.getServer().getInfo().getName())) {
			Message.sendMessage(bp, MSG.PREFIX_ERROR, MSG.PLAYER_SERVER_CONNECTED_ALREADY, sm.formatServerName(sm.getServerID(srv.getName())));
			return;
		}
		Message.sendMessage(bp, MSG.PREFIX_ADMIN, MSG.PLAYER_SERVER_CONNECTING, srv.getName());
		sender.connect(srv);
	}

	@Override
	public void executeConsole(String label, String[] args) {
		Message.sendConsole(MSG.PREFIX_ERROR, MSG.MUST_BE_PLAYER);
	}
	
}
