package lol.kangaroo.bungee.player;

import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lol.kangaroo.bungee.config.ConfigManager;
import lol.kangaroo.bungee.util.Message;
import lol.kangaroo.bungee.util.ThreadManager;
import lol.kangaroo.common.database.Setting;
import lol.kangaroo.common.player.BasePlayer;
import lol.kangaroo.common.player.CachedPlayer;
import lol.kangaroo.common.player.PlayerUpdateCache;
import lol.kangaroo.common.player.PlayerVariable;
import lol.kangaroo.common.util.MSG;
import net.md_5.bungee.api.ChatColor;

public enum PlayerLevel {

	BEGINNER("Beginner", 0, 10, ChatColor.DARK_GRAY, 1),
	NOVICE3("Novice III", 10, 25, ChatColor.GRAY, 2),
	NOVICE2("Novice II", 25, 40, ChatColor.GRAY, 3),
	NOVICE1("Novice I", 40, 55, ChatColor.GRAY, 4),
	AMATEUR3("Amateur III", 55, 70, ChatColor.WHITE, 5),
	AMATEUR2("Amateur II", 70, 85, ChatColor.WHITE, 6),
	AMATEUR1("Amateur I", 85, 100, ChatColor.WHITE, 7),
	COMPETENT3("Competent III", 100, 120, ChatColor.YELLOW, 8),
	COMPETENT2("Competent II", 120, 140, ChatColor.YELLOW, 9),
	COMPETENT1("Competent I", 140, 160, ChatColor.YELLOW, 10),
	PRO3("Pro III", 160, 190, ChatColor.LIGHT_PURPLE, 11),
	PRO2("Pro II", 190, 220, ChatColor.LIGHT_PURPLE, 12),
	PRO1("Pro I", 220, 250, ChatColor.GREEN, 13),
	EXPERT3("Expert III", 250, 275, ChatColor.AQUA, 14),
	EXPERT2("Expert II", 275, 300, ChatColor.AQUA, 15),
	EXPERT1("Expert I", 300, 325, ChatColor.DARK_GREEN, 16),
	VETERAN3("Veteran III", 325, 350, ChatColor.BLUE, 17),
	VETERAN2("Veteran II", 350, 375, ChatColor.BLUE, 18),
	VETERAN1("Veteran I", 375, 400, ChatColor.DARK_AQUA, 19),
	PRINCE2("Prince II", 400, 425, ChatColor.DARK_BLUE, 20),
	PRINCE1("Prince I", 425, 450, ChatColor.GOLD, 21),
	KING1("King", 450, 475, ChatColor.RED, 22),
	KANGAROO1("Kangaroo", 475, 500, ChatColor.DARK_PURPLE, 24),
	SUPREME("Supreme", 500, 1000, ChatColor.DARK_RED, 25),
	SUPREMEULTIMATE("Supreme Ultimate", 1000, 2000, ChatColor.BLACK, 26);

	String name;
	int min;
	int max;
	ChatColor color;
	int order;
	PlayerLevel(String name, int min, int max, ChatColor color, int order) {
		this.name = name;
		this.min = min;
		this.max = max;
		this.color = color;
		this.order = order;
	}

	public String getName() {
		return name;
	}

	@Override
	public String toString() {
		return name;
	}

	public int getMinLevel() {
		return min;
	}

	public int getMaxLevel() {
		return max;
	}

	public ChatColor getColor() {
		return color;
	}
	
	public int getOrder() {
		return order;
	}
	
	private static PlayerManager pm;
	private static ConfigManager conf;
	
	public static void init(PlayerManager plm, ConfigManager config) {
		pm = plm;
		conf = config;
		SYMBOL = conf.getConfig("settings").getString("xp-symbol");
	}
	
	public static String SYMBOL;
	
	public static PlayerLevel getByName(String name) {
		for(PlayerLevel l : values()) {
			if(l.getName().equals(name)) return l;
		}
		return null;
	}

	public static PlayerLevel getLevel(int level) {
		for(PlayerLevel l : values()) {
			if(level >= l.getMinLevel() && level < l.getMaxLevel()) return l;
		}
		return BEGINNER;
	}

	/**
	 * Amount of Experience needed to rank up TO this level
	 */
	public static long getRequiredExperience(int level) {
		return level*level;
	}
	
	/**
	 * @deprecated Could possibly use database requests, should be run asynchronously.
	 */
	public static long getExperience(UUID uuid) {
		CachedPlayer cp = pm.getCachedPlayer(uuid);
		return (long) cp.getVariable(PlayerVariable.EXPERIENCE);
	}
	
	public static long getExperience(BasePlayer bp) {
		return (long) bp.getVariable(PlayerVariable.EXPERIENCE);
	}
	
	private static final Pattern reasonPattern = Pattern.compile("^(?:([CL]).*: ?)(.*)$");
	
	/**
	 * @deprecated Uses database requests, should be run asynchronously.
	 */
	public static void addExperience(UUID uuid, long amount, boolean useMultiplier, boolean sendMessage) {
		CachedPlayer cp = pm.getCachedPlayer(uuid);
		addExperience(cp, amount, useMultiplier, sendMessage);
	}
	
	public static void addExperience(CachedPlayer cp, long amount, boolean useMultiplier, boolean sendMessage) {
		String multiplierMessage = "";
		if(useMultiplier) {
			float multiplier = (float) Setting.getSetting(Setting.XPMULTIPLIER);
			amount *= multiplier;
			if(multiplier != 1f) {
				String mreason = (String) Setting.getSetting(Setting.XPMREASON);
				Matcher m = reasonPattern.matcher(mreason);
				if(m.matches()) {
					if(m.group(1).equals("C")) multiplierMessage = MSG.color("&7(&b" + m.group(2) + "&7)");
					else if(m.group(1).equals("L")) multiplierMessage = new MSG(m.group(2)).getMessage(cp);
				}
			}
		}
		int level = getPlayerLevel(cp, false);
		long max = getRequiredExperience(level+1);
		long cur = getExperience(cp);
		PlayerUpdateCache u = cp.createUpdateCache();
		if(cur + amount >= max) {
			amount = (cur + amount) - max;
			cp.setVariableInUpdate(u, PlayerVariable.LEVEL, level + 1);
			cp.setVariableInUpdate(u, PlayerVariable.EXPERIENCE, amount);
			Message.sendMessage(cp, MSG.EXP_ADDED, amount, multiplierMessage);
			PlayerLevel l = getLevel(level + 1);
			Message.sendMessage(cp, MSG.EXP_LEVELUP, l.getColor() + l.getName(), level+1, SYMBOL);
		} else {
			cp.setVariableInUpdate(u, PlayerVariable.EXPERIENCE, cur + amount);
			Message.sendMessage(cp, MSG.EXP_ADDED, amount, multiplierMessage);
		}
		u.pushUpdates();
	}

	/**
	 * @deprecated Use {@link #getLevel(BasePlayer, boolean)}, this method may lag in sync.
	 * Returns the player's level, -1 if they don't exist.
	 */
	public static int getPlayerLevel(UUID uuid, boolean nickLevel) {
		// TODO support nicknames
		CachedPlayer cp = pm.getCachedPlayer(uuid);
		if(cp == null) return -1;
		return (int) cp.getVariable(PlayerVariable.LEVEL);
	}

	/**
	 * Returns the player's level.
	 */
	public static int getPlayerLevel(BasePlayer cp, boolean nickLevel) {
		// TODO support nicknames
		return (int) cp.getVariable(PlayerVariable.LEVEL);
	}
	
	/**
	 * Asynchronously sets the level. Use {@link #setLevelSync(UUID, int)} for sync.
	 */
	public static void setLevel(UUID uuid, int level) {
		ThreadManager.async(() -> {
			CachedPlayer cp = pm.getCachedPlayer(uuid);
			if(cp == null) return;
			PlayerUpdateCache u = cp.createUpdateCache();
			cp.setVariableInUpdate(u, PlayerVariable.LEVEL, level);
			u.pushUpdates();
		});
	}
	
	/**
	 * Synchronously sets the level. Not recommended due to database lag.
	 */
	public static void setLevelSync(UUID uuid, int level) {
		CachedPlayer cp = pm.getCachedPlayer(uuid);
		if(cp == null) return;
		PlayerUpdateCache u = cp.createUpdateCache();
		cp.setVariableInUpdate(u, PlayerVariable.LEVEL, level);
		u.pushUpdates();
	}
	
	/**
	 * Asynchronously sets the level.
	 */
	public static void setLevel(CachedPlayer cp, int level) {
		ThreadManager.async(() -> {
			PlayerUpdateCache u = cp.createUpdateCache();
			cp.setVariableInUpdate(u, PlayerVariable.LEVEL, level);
			u.pushUpdates();
		});
	}
	
	/**
	 * Synchronously sets the level. Not recommended due to database lag.
	 */
	public static void setLevelSync(CachedPlayer cp, int level) {
		PlayerUpdateCache u = cp.createUpdateCache();
		cp.setVariableInUpdate(u, PlayerVariable.LEVEL, level);
		u.pushUpdates();
	}

}