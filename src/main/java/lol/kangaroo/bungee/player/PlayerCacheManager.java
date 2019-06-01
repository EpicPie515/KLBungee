package lol.kangaroo.bungee.player;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import lol.kangaroo.bungee.KLBungeePlugin;
import lol.kangaroo.common.player.CachedPlayer;
import lol.kangaroo.common.player.PlayerVariable;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public class PlayerCacheManager {
	
	private KLBungeePlugin pl;
	
	private PlayerManager pm;
	
	private long pullUpdateInterval;
	private long flushInterval;
	
	private Set<CachedPlayer> playerCache = new HashSet<>();
	
	private Map<UUID, CachedPlayer> uuidCache = new HashMap<>();
	
	/**
	 * 
	 * @param pullUpdateInterval Interval in milliseconds which the cache updates from the database.
	 * @param flushInterval Interval in milliseconds which the cache will flush out offline players (non-DB)
	 */
	public PlayerCacheManager(KLBungeePlugin pl, long pullUpdateInterval, long flushInterval) {
		this.pullUpdateInterval = pullUpdateInterval;
		this.flushInterval = flushInterval;
		this.pl = pl;
	}
	
	public void scheduleUpdateTasks(PlayerManager pm) {
		this.pm = pm;
		pl.getProxy().getScheduler().schedule(pl, new PullCacheUpdateRunnable(), 100, pullUpdateInterval, TimeUnit.MILLISECONDS);
		pl.getProxy().getScheduler().schedule(pl, new FlushCacheRunnable(), 100, flushInterval, TimeUnit.MILLISECONDS);
		pl.getProxy().getScheduler().schedule(pl, () -> {
			if(millisUntilPull <= 0) millisUntilPull = pullUpdateInterval;
			else millisUntilPull -= 1000;
			if(millisUntilFlush <= 0) millisUntilFlush = flushInterval;
			else millisUntilFlush -= 1000;
		}, 100, 1, TimeUnit.SECONDS);
	}
	
	private long millisUntilPull;
	private long millisUntilFlush;
	
	private class PullCacheUpdateRunnable implements Runnable {
		
		@Override
		public void run() {
			for(CachedPlayer cp : playerCache) {
				// Cached variables to use later.
				Map<PlayerVariable, Object> map = cp.getAllVariablesMap();
				// Gets a DatabasePlayer for comparison.
				DatabasePlayer dbpl = pm.getDatabasePlayer(cp.getUniqueId());
				// Getting all DB Variables in one connection.
				Map<PlayerVariable, Object> dbMap = dbpl.getAllVariablesMap();
				
				for(PlayerVariable pv : map.keySet()) {
					if(!map.get(pv).equals(dbMap.get(pv))) {
						map.replace(pv, dbMap.get(pv));
					}
				}
				
				cp.setAllVariablesMap(map);
			}
		}
	}
	
	private class FlushCacheRunnable implements Runnable {
		
		@Override
		public void run() {
			Set<CachedPlayer> tr = new HashSet<>();
			for(CachedPlayer cp : playerCache) {
				ProxiedPlayer pp = pl.getProxy().getPlayer(cp.getUniqueId());
				if(pp == null) {
					tr.add(cp);
				}
			}
			for(CachedPlayer cp : tr) removeFromPlayerCache(cp);
			System.gc();
		}
	}
	
	public Set<CachedPlayer> getPlayerCache() {
		return playerCache;
	}
	
	public Map<UUID, CachedPlayer> getUUIDCache() {
		return uuidCache;
	}
	
	public boolean isInPlayerCache(CachedPlayer cp) {
		return playerCache.contains(cp);
	}
	
	public boolean isInPlayerCache(UUID uuid) {
		return uuidCache.containsKey(uuid) && playerCache.contains(uuidCache.get(uuid));
	}
	
	public void addToPlayerCache(CachedPlayer cp) {
		playerCache.add(cp);
		uuidCache.put(cp.getUniqueId(), cp);
	}
	
	public void removeFromPlayerCache(CachedPlayer cp) {
		playerCache.remove(cp);
		uuidCache.remove(cp.getUniqueId());
	}
	
	/**
	 * Removes @param oldPlayer, adds @param newPlayer.
	 * 
	 * Does not maintain same index, but its a @Set anyway so any index shouldn't be used.
	 */
	public void replaceInPlayerCache(CachedPlayer oldPlayer, CachedPlayer newPlayer) {
		removeFromPlayerCache(oldPlayer);
		addToPlayerCache(newPlayer);
	}
	
}
