package lol.kangaroo.bungee.permissions;

import java.util.concurrent.TimeUnit;

import lol.kangaroo.bungee.KLBungeePlugin;
import lol.kangaroo.common.player.CachedPlayer;

public class RankPermissionExpiry implements Runnable {
	
	private KLBungeePlugin plugin;
	
	public RankPermissionExpiry(KLBungeePlugin pl) {
		plugin = pl;
	}
	
	public void startLoopTask() {
		plugin.getProxy().getScheduler().schedule(plugin, this, 30, 60, TimeUnit.SECONDS);
	}

	@Override
	public void run() {
		for(CachedPlayer cp : plugin.getPlayerManager().getPlayerCacheManager().getPlayerCache()) {
			plugin.getPlayerManager().updateGrants(cp);
			plugin.getPlayerManager().removeExpiredPermissions(cp);
		}
	}
	
}
