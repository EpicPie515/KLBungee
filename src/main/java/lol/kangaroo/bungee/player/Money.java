package lol.kangaroo.bungee.player;

import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lol.kangaroo.bungee.util.Message;
import lol.kangaroo.bungee.util.ThreadManager;
import lol.kangaroo.common.database.Setting;
import lol.kangaroo.common.player.BasePlayer;
import lol.kangaroo.common.player.CachedPlayer;
import lol.kangaroo.common.player.PlayerUpdateCache;
import lol.kangaroo.common.player.PlayerVariable;
import lol.kangaroo.common.util.MSG;

public class Money {
	private static PlayerManager pm;
	
	public static void init(PlayerManager plm) {
		pm = plm;
	}
	
	/** 
	 * Sets the player's balance asynchronously. Use {@link #setBalanceSync(UUID, long)} for a synchronous method.
	 */
	public static void setBalance(UUID uuid, long balance) {
		ThreadManager.async(() -> {
			CachedPlayer cp = pm.getCachedPlayer(uuid);
			PlayerUpdateCache c = cp.createUpdateCache();
			cp.setVariableInUpdate(c, PlayerVariable.NETWORK_BALANCE, balance);
			c.pushUpdates();
		});
	}
	
	/** 
	 * Sets the player's balance synchronously. This may cause lag and shouldn't be used. Use {@link #setBalance(UUID, long)} for an asynchronous method.
	 */
	public static void setBalanceSync(UUID uuid, long balance) {
		CachedPlayer cp = pm.getCachedPlayer(uuid);
		PlayerUpdateCache c = cp.createUpdateCache();
		cp.setVariableInUpdate(c, PlayerVariable.NETWORK_BALANCE, balance);
		c.pushUpdates();
	}
	
	/** 
	 * Sets the player's balance asynchronously. Use {@link #setBalanceSync(CachedPlayer, long)} for a synchronous method.
	 */
	public static void setBalance(CachedPlayer cp, long balance) {
		ThreadManager.async(() -> {
			PlayerUpdateCache c = cp.createUpdateCache();
			cp.setVariableInUpdate(c, PlayerVariable.NETWORK_BALANCE, balance);
			c.pushUpdates();
		});
	}
	
	/** 
	 * Sets the player's balance synchronously. This may cause lag and shouldn't be used. Use {@link #setBalance(CachedPlayer, long)} for an asynchronous method.
	 */
	public static void setBalanceSync(CachedPlayer cp, long balance) {
		PlayerUpdateCache c = cp.createUpdateCache();
		cp.setVariableInUpdate(c, PlayerVariable.NETWORK_BALANCE, balance);
		c.pushUpdates();
	}
	
	/** 
	 * Sets the player's balance asynchronously. Use {@link #setBalanceSync(DatabasePlayer, long)} for a synchronous method.
	 */
	public static void setBalance(DatabasePlayer cp, long balance) {
		ThreadManager.async(() -> {
			cp.setVariable(PlayerVariable.NETWORK_BALANCE, balance);
		});
	}
	
	/** 
	 * Sets the player's balance synchronously. This may cause lag and shouldn't be used. Using DatabasePlayer at all is also a bad idea. Use {@link #setBalance(DatabasePlayer, long)} for an asynchronous method.
	 */
	public static void setBalanceSync(DatabasePlayer cp, long balance) {
		cp.setVariable(PlayerVariable.NETWORK_BALANCE, balance);
	}
	
	/**
	 * @deprecated Use {@link #getBalance(BasePlayer)}, this may cause lag if the player is not in the cache.
	 * Gets the player's balance. 
	 * 
	 */
	public static long getBalance(UUID uuid) {
		CachedPlayer cp = pm.getCachedPlayer(uuid);
		return (long) cp.getVariable(PlayerVariable.NETWORK_BALANCE);
	}
	
	/**
	 * Gets the player's balance.
	 */
	public static long getBalance(BasePlayer bp) {
		return (long) bp.getVariable(PlayerVariable.NETWORK_BALANCE);
	}
	
	/**
	 * Adds to the player's balance asynchronously. To add synchronously use {@link #addToBalanceSync(UUID, long)}
	 */
	public static void addToBalance(UUID uuid, long addTo) {
		ThreadManager.async(() -> {
			CachedPlayer cp = pm.getCachedPlayer(uuid);
			long cur = (long) cp.getVariable(PlayerVariable.NETWORK_BALANCE);
			PlayerUpdateCache c = cp.createUpdateCache();
			cp.setVariableInUpdate(c, PlayerVariable.NETWORK_BALANCE, cur + addTo);
			c.pushUpdates();
		});
	}
	
	/**
	 * @deprecated Use {@link #addToBalanceSync(CachedPlayer, long)} if synchronous is absolutely necessary.
	 * Adds to the player's balance synchronously. This will lag.
	 */
	public static void addToBalanceSync(UUID uuid, long addTo) {
		CachedPlayer cp = pm.getCachedPlayer(uuid);
		long cur = (long) cp.getVariable(PlayerVariable.NETWORK_BALANCE);
		PlayerUpdateCache c = cp.createUpdateCache();
		cp.setVariableInUpdate(c, PlayerVariable.NETWORK_BALANCE, cur + addTo);
		c.pushUpdates();
	}
	
	/**
	 * Adds to the player's balance asynchronously. To add synchronously use {@link #addToBalanceSync(UUID, long)}
	 */
	public static void addToBalance(CachedPlayer cp, long addTo) {
		ThreadManager.async(() -> {
			long cur = (long) cp.getVariable(PlayerVariable.NETWORK_BALANCE);
			PlayerUpdateCache c = cp.createUpdateCache();
			cp.setVariableInUpdate(c, PlayerVariable.NETWORK_BALANCE, cur + addTo);
			c.pushUpdates();
		});
	}
	
	private static final Pattern reasonPattern = Pattern.compile("^(?:([CL]).*: ?)(.*)$");
	
	public static void addToBalanceAndMessage(CachedPlayer cp, boolean useMultiplier, long addTo) {
		String multiplierMessage = "";
		if(useMultiplier) {
			float multiplier = (float) Setting.getSetting(Setting.COINMULTIPLIER);
			addTo *= multiplier;
			if(multiplier != 1f) {
				String mreason = (String) Setting.getSetting(Setting.COINMREASON);
				Matcher m = reasonPattern.matcher(mreason);
				if(m.matches()) {
					if(m.group(1).equals("C")) multiplierMessage = MSG.color("&7(&b" + m.group(2) + "&7)");
					else if(m.group(1).equals("L")) multiplierMessage = MSG.color("&7(&b" + new MSG(m.group(2)).getMessage(cp) + "&7)");
				}
			}
		}
		addToBalance(cp, addTo);
		Message.sendMessage(cp, MSG.MONEY_ADDED, addTo, multiplierMessage);
	}
	
	/**
	 * Adds to the player's balance synchronously. This will cause lag and is not recommended
	 */
	public static void addToBalanceSync(CachedPlayer cp, long addTo) {
		long cur = (long) cp.getVariable(PlayerVariable.NETWORK_BALANCE);
		PlayerUpdateCache c = cp.createUpdateCache();
		cp.setVariableInUpdate(c, PlayerVariable.NETWORK_BALANCE, cur + addTo);
		c.pushUpdates();
	}
	
	public static boolean canAfford(UUID uuid, long balance) {
		return getBalance(uuid) >= balance;
	}

	public static boolean canAfford(BasePlayer bp, long balance) {
		return getBalance(bp) >= balance;
	}

}
