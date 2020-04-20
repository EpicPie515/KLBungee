package lol.kangaroo.bungee.commands.player;

import lol.kangaroo.bungee.commands.CommandExecutor;
import lol.kangaroo.bungee.player.PlayerManager;
import lol.kangaroo.bungee.util.Message;
import lol.kangaroo.common.permissions.Rank;
import lol.kangaroo.common.player.BasePlayer;
import lol.kangaroo.common.player.CachedPlayer;
import lol.kangaroo.common.player.PlayerVariable;
import lol.kangaroo.common.util.MSG;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public class PingCommand extends CommandExecutor {

	public PingCommand(PlayerManager pm, ProxyServer proxy) {
		super(pm, proxy, "ping", Rank.PLAYER.getPerm());
	}

	@Override
	public void execute(ProxiedPlayer sender, BasePlayer bp, String label, String[] args) {
		if(args.length == 0) {
			Message.sendMessage(bp, MSG.PREFIX_PLAYER, MSG.COMMAND_PING_SELF, sender.getPing());
			return;
		}
		CachedPlayer cp = pm.getCachedPlayer(pm.getFromCurrent(args[0]));
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
		Message.sendMessage(bp, MSG.PREFIX_PLAYER, MSG.COMMAND_PING_OTHER, prefix + nickname, pp.getPing());
	}

	@Override
	public void executeConsole(String label, String[] args) {
		if(args.length == 0) {
			Message.sendConsole(MSG.PREFIX_ERROR, MSG.COMMAND_PING_USAGE);
			return;
		}
		CachedPlayer cp = pm.getCachedPlayer(pm.getFromCurrent(args[0]));
		if(cp == null) {
			Message.sendConsole(MSG.PREFIX_ERROR, MSG.PLAYER_NOTFOUND);
			return;
		}
		ProxiedPlayer pp = proxy.getPlayer(cp.getUniqueId());
		if(pp == null) {
			Message.sendConsole( MSG.PREFIX_ERROR, MSG.PLAYER_OFFLINE);
			return;
		}
		String prefix = pm.getRankManager().getPrefix(cp, true);
		String nickname = (String) cp.getVariable(PlayerVariable.NICKNAME);
		Message.sendConsole(MSG.PREFIX_PLAYER, MSG.COMMAND_PING_OTHER, prefix + nickname, pp.getPing());
	}
	
}
