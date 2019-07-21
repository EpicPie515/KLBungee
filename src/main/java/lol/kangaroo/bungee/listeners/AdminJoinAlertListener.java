package lol.kangaroo.bungee.listeners;

import lol.kangaroo.bungee.KLBungeePlugin;
import lol.kangaroo.bungee.player.PlayerManager;
import lol.kangaroo.bungee.util.Message;
import lol.kangaroo.common.permissions.Rank;
import lol.kangaroo.common.player.CachedPlayer;
import lol.kangaroo.common.player.PlayerVariable;
import lol.kangaroo.common.util.MSG;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class AdminJoinAlertListener implements Listener {
	
	private PlayerManager pm;
	private KLBungeePlugin pl;
	
	public AdminJoinAlertListener(PlayerManager pm, KLBungeePlugin pl) {
		this.pl = pl;
		this.pm = pm;
	}
	
	@EventHandler
	public void onPostLogin(PostLoginEvent e) {
		ProxiedPlayer p = e.getPlayer();
		pl.getProxy().getScheduler().runAsync(pl, () -> {
			CachedPlayer op = pm.getCachedPlayer(p.getUniqueId());
			for(ProxiedPlayer pp : pl.getProxy().getPlayers()) {
				CachedPlayer cp = pm.getCachedPlayer(pp.getUniqueId());
				Rank rank = (Rank) cp.getVariable(PlayerVariable.RANK);
				if(rank.getLevel() > Rank.SRMOD.getLevel())
					if((Boolean) cp.getVariable(PlayerVariable.ADMIN_ALERT))
						Message.sendMessage(cp, MSG.ADMIN_JOINALERT, op.getVariable(PlayerVariable.USERNAME));
			}
		});
	}
	
	@EventHandler
	public void onDisconnect(PlayerDisconnectEvent e) {
		ProxiedPlayer p = e.getPlayer();
		pl.getProxy().getScheduler().runAsync(pl, () -> {
			CachedPlayer op = pm.getCachedPlayer(p.getUniqueId());
			for(ProxiedPlayer pp : pl.getProxy().getPlayers()) {
				CachedPlayer cp = pm.getCachedPlayer(pp.getUniqueId());
				Rank rank = (Rank) cp.getVariable(PlayerVariable.RANK);
				if(rank.getLevel() > Rank.SRMOD.getLevel())
					if((Boolean) cp.getVariable(PlayerVariable.ADMIN_ALERT))
						Message.sendMessage(cp, MSG.ADMIN_LEAVEALERT, op.getVariable(PlayerVariable.USERNAME));
			}
		});
	}
	
}
