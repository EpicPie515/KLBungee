package lol.kangaroo.bungee.listeners;

import lol.kangaroo.bungee.player.PlayerManager;
import lol.kangaroo.common.player.CachedPlayer;
import net.md_5.bungee.api.plugin.Listener;

public class PermissionListener implements Listener {
	
	private PlayerManager pm;
	
	public PermissionListener(PlayerManager pm) {
		this.pm = pm;
	}
	
	/**
	 * Called by @PlayerDatabaseListener because the cache doesn't like being accessed 3 times on join.
	 */
	public void onPostLogin(CachedPlayer cp) {
		if(cp == null) return;
		pm.setJoinedPlayerPermissions(cp);
	}
	
}
