package lol.kangaroo.bungee.listeners;

import lol.kangaroo.bungee.KLBungeePlugin;
import lol.kangaroo.bungee.player.PlayerManager;
import lol.kangaroo.common.player.CachedPlayer;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;

public class PermissionListener implements Listener {
	
	private PlayerManager pm;
	private ProxyServer proxy;
	private KLBungeePlugin pl;
	
	public PermissionListener(PlayerManager pm, KLBungeePlugin pl) {
		this.pm = pm;
		this.pl = pl;
		this.proxy = ProxyServer.getInstance();
	}
	
	@EventHandler(priority=EventPriority.HIGH)
	public void onPostLogin(PostLoginEvent e) {
		ProxiedPlayer c = e.getPlayer();
		proxy.getScheduler().runAsync(pl, () -> {
			CachedPlayer cp = pm.getCachedPlayer(c.getUniqueId());
			if(cp != null)
				pm.setJoinedPlayerPermissions(cp);
		});
	}
	
}
