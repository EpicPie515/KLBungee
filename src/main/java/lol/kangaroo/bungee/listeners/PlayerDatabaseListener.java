package lol.kangaroo.bungee.listeners;

import java.net.InetAddress;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.concurrent.TimeUnit;

import lol.kangaroo.bungee.KLBungeePlugin;
import lol.kangaroo.bungee.player.PlayerManager;
import lol.kangaroo.bungee.util.Message;
import lol.kangaroo.common.permissions.Rank;
import lol.kangaroo.common.player.CachedPlayer;
import lol.kangaroo.common.player.PlayerHistory;
import lol.kangaroo.common.player.PlayerHistory.HistoryUpdateCache;
import lol.kangaroo.common.player.PlayerUpdateCache;
import lol.kangaroo.common.player.PlayerVariable;
import lol.kangaroo.common.util.MSG;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.PendingConnection;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.LoginEvent;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;

public class PlayerDatabaseListener implements Listener {
	
	private PlayerManager pm;
	private ProxyServer proxy;
	private KLBungeePlugin pl;
	
	public PlayerDatabaseListener(PlayerManager pm, KLBungeePlugin pl) {
		this.pm = pm;
		this.pl = pl;
		this.proxy = ProxyServer.getInstance();
	}
	
	@EventHandler(priority=EventPriority.HIGHEST)
	public void onLogin(LoginEvent e) {
		PendingConnection c = e.getConnection();
		e.registerIntent(pl);
		proxy.getScheduler().runAsync(pl, () -> {
			if(!pm.playerExists(c.getUniqueId()))
				pm.createNewPlayer(c);
			e.completeIntent(pl);
		});
	}
	
	@EventHandler(priority=EventPriority.HIGHEST)
	public void onPostLogin(PostLoginEvent e) {
		ProxiedPlayer c = e.getPlayer();
		proxy.getScheduler().runAsync(pl, () -> {
			CachedPlayer cp;
			cp = pm.getCachedPlayer(c.getUniqueId());
			
			Timestamp ts = (Timestamp) cp.getVariable(PlayerVariable.LASTJOIN);
			String username = (String) cp.getVariable(PlayerVariable.USERNAME);
			String nickname = (String) cp.getVariable(PlayerVariable.NICKNAME);
			InetAddress lastIp = (InetAddress) cp.getVariable(PlayerVariable.IP);
			Rank r = (Rank) cp.getVariable(PlayerVariable.RANK);
			PlayerUpdateCache u = cp.createUpdateCache();
			PlayerHistory hist = PlayerHistory.getPlayerHistory(cp);
			HistoryUpdateCache hu = hist.createUpdateCache();
			cp.setVariableInUpdate(u, PlayerVariable.LASTJOIN, new Timestamp(System.currentTimeMillis()));
			if(username != c.getName()) {
				Message.broadcast(pm.getNotifiableStaff(), MSG.ADMIN_NAMECHANGEALERT, pl.getRankManager().getPrefix(cp) + c.getName(), pl.getRankManager().getPrefix(cp) + username);
				cp.setVariableInUpdate(u, PlayerVariable.USERNAME, c.getName());
				hu.addName(c.getName(), System.currentTimeMillis());
				if(username.equals(nickname)) {
					cp.setVariableInUpdate(u, PlayerVariable.NICKNAME, c.getName());
					hu.addNickname(c.getName(), System.currentTimeMillis());
				}
			}
			if(!lastIp.equals(c.getAddress().getAddress())) {
				cp.setVariableInUpdate(u, PlayerVariable.IP, c.getAddress().getAddress());
				hu.addIp(c.getAddress().getAddress(), System.currentTimeMillis());
			}
			LocalDateTime lastJoin = LocalDateTime.ofInstant(Instant.ofEpochMilli(ts.getTime()), ZoneId.of("UTC"));
			LocalDateTime thisJoin = LocalDateTime.now();
			if(lastJoin.getDayOfMonth() != thisJoin.getDayOfMonth()
					|| lastJoin.getMonthValue() != thisJoin.getMonthValue()
					|| lastJoin.getYear() != thisJoin.getYear()) {
				cp.setVariableInUpdate(u, PlayerVariable.NETWORK_BALANCE, (150 * r.getLevel()) + ((long) cp.getVariable(PlayerVariable.NETWORK_BALANCE)));
				KLBungeePlugin.instance.getProxy().getScheduler().schedule(KLBungeePlugin.instance, () -> {
					Message.sendMessage(cp, MSG.DAILY_BONUS, (150 * r.getLevel()));
				}, 1000, TimeUnit.MILLISECONDS);
			}
			u.pushUpdates();
			hu.pushUpdates();
		});
	}
	
	@EventHandler
	public void onLogin(PlayerDisconnectEvent e) {
		CachedPlayer cp = pm.getCachedPlayer(e.getPlayer().getUniqueId());
		proxy.getScheduler().runAsync(pl, () -> {
			PlayerUpdateCache u = cp.createUpdateCache();
			cp.setVariableInUpdate(u, PlayerVariable.LASTQUIT, new Timestamp(System.currentTimeMillis()));
			u.pushUpdates();
		});
	}
	
}
