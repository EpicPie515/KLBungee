package lol.kangaroo.bungee.commands.admin;

import lol.kangaroo.bungee.commands.AdminCommand;
import lol.kangaroo.bungee.commands.Subcommand;
import lol.kangaroo.bungee.player.PlayerManager;
import lol.kangaroo.bungee.util.Message;
import lol.kangaroo.bungee.util.ThreadManager;
import lol.kangaroo.common.permissions.Rank;
import lol.kangaroo.common.player.BasePlayer;
import lol.kangaroo.common.player.CachedPlayer;
import lol.kangaroo.common.player.DatabasePlayer;
import lol.kangaroo.common.player.PlayerUpdateCache;
import lol.kangaroo.common.player.PlayerVariable;
import lol.kangaroo.common.util.MSG;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public class AlertsSub extends Subcommand {
	
	public AlertsSub(PlayerManager pm, ProxyServer proxy, AdminCommand parent) {
		super(pm, proxy, parent, "alerts", Rank.SRMOD.getPerm(), "alert", "togglealerts");
	}

	@Override
	public void execute(ProxiedPlayer sender, BasePlayer bp, String label, String[] args) {
		ThreadManager.async(() -> {
			boolean cur = (Boolean) bp.getVariable(PlayerVariable.ADMIN_ALERT);
			boolean upd = !cur;
			if(upd)
				Message.sendMessage(bp, MSG.PREFIX_ADMIN, MSG.ADMIN_ALERTSON);
			else
				Message.sendMessage(bp, MSG.PREFIX_ADMIN, MSG.ADMIN_ALERTSOFF);
			if(bp instanceof CachedPlayer) {
					CachedPlayer cp = ((CachedPlayer)bp);
					PlayerUpdateCache u = cp.createUpdateCache();
					cp.setVariableInUpdate(u, PlayerVariable.ADMIN_ALERT, upd);
					u.pushUpdates();
			} else if(bp instanceof DatabasePlayer) {
					DatabasePlayer dp = ((DatabasePlayer)bp);
					dp.setVariable(PlayerVariable.ADMIN_ALERT, upd);
			}
		});
	}

	@Override
	public void executeConsole(String label, String[] args) {
		ThreadManager.async(() -> {
			CachedPlayer target = pm.getCachedPlayer(pm.getFromCurrent(args[0]));
			if(target == null) {
				Message.sendConsole(MSG.PREFIX_ERROR, MSG.PLAYER_NOTFOUND);
				return;
			}
			boolean cur = (Boolean) target.getVariable(PlayerVariable.ADMIN_ALERT);
			boolean upd = !cur;
			String pref = pm.getRankManager().getPrefix(target, false);
			if(upd)
				Message.sendConsole(MSG.PREFIX_ADMIN, MSG.ADMIN_ALERTSON_OTHER, pref + target.getVariable(PlayerVariable.USERNAME));
			else
				Message.sendConsole(MSG.PREFIX_ADMIN, MSG.ADMIN_ALERTSOFF_OTHER, pref + target.getVariable(PlayerVariable.USERNAME));
			PlayerUpdateCache c = target.createUpdateCache();
			target.setVariableInUpdate(c, PlayerVariable.ADMIN_ALERT, upd);
			c.pushUpdates();
		});
	}
	
	
}
