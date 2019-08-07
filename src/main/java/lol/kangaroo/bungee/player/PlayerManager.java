package lol.kangaroo.bungee.player;

import java.net.InetAddress;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;

import lol.kangaroo.bungee.KLBungeePlugin;
import lol.kangaroo.bungee.permissions.RankManager;
import lol.kangaroo.bungee.player.punish.PunishManager;
import lol.kangaroo.bungee.util.Message;
import lol.kangaroo.common.database.DatabaseManager;
import lol.kangaroo.common.permissions.PermissionManager;
import lol.kangaroo.common.permissions.Rank;
import lol.kangaroo.common.player.BasePlayer;
import lol.kangaroo.common.player.CachedPlayer;
import lol.kangaroo.common.player.PlayerHistory;
import lol.kangaroo.common.player.PlayerHistory.HistoryUpdateCache;
import lol.kangaroo.common.player.PlayerUpdateCache;
import lol.kangaroo.common.player.PlayerVariable;
import lol.kangaroo.common.player.punish.Ban;
import lol.kangaroo.common.player.punish.Blacklist;
import lol.kangaroo.common.player.punish.Mute;
import lol.kangaroo.common.player.punish.Punishment;
import lol.kangaroo.common.util.DoubleObject;
import lol.kangaroo.common.util.MSG;
import lol.kangaroo.common.util.ObjectMutable;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.PendingConnection;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public class PlayerManager {
	
	private DatabaseManager db;
	
	private ProxyServer proxy;
	private KLBungeePlugin pl;
	
	private PlayerVariableManager pvm;
	
	private PlayerCacheManager pcm;
	
	private PunishManager pum;
	private PermissionManager prm;
	
	public Set<UUID> unbannedJoining = new HashSet<>();
	
	public PlayerManager(DatabaseManager db, ProxyServer proxy, KLBungeePlugin pl, PlayerVariableManager pvm, PlayerCacheManager pcm, PunishManager pum, PermissionManager prm) {
		this.db = db;
		this.pl = pl;
		this.proxy = proxy;
		this.pvm = pvm;
		this.pcm = pcm;
		this.pum = pum;
		this.prm = prm;
	}
	
	/**
	 * Pass-through getter to lessen the amount of arguments on functions that already have PlayerManager
	 * @return
	 */
	public RankManager getRankManager() {
		return pl.getRankManager();
	}
	
	/**
	 * Pass-through getter to lessen the amount of arguments on functions that already have PlayerManager
	 * @return
	 */
	public PunishManager getPunishManager() {
		return pum;
	}
	
	public PlayerCacheManager getPlayerCacheManager() {
		return pcm;
	}
	
	public PermissionManager getPermissionManager() {
		return prm;
	}
	
	/**
	 * Verifies that the UUID is of a player that has joined the server before.
	 * @param uuid UUID to check.
	 * @return whether the player exists.
	 */
	public boolean playerExists(UUID uuid) {
		if(uuid == null) return false;
		
		ObjectMutable<Boolean> b = new ObjectMutable<>(false);
		
		db.query("SELECT `UUID` FROM `users` WHERE `UUID`=?", rs -> {
			try {
				if(rs.next()) b.set(true);
			} catch (SQLException e) {
				e.printStackTrace();
			} 
		}, uuid);
		return b.get();
	}
	
	/**
	 * Gets a DatabasePlayer object which is always directly returning database values, non-cached.
	 * When possible, use {@link #getCachedPlayer(UUID)} instead.
	 * 
	 * Returns null if the given UUID has never joined the server.
	 * 
	 * @param uuid The UUID of the player.
	 * @return a new DatabasePlayer object
	 */
	public DatabasePlayer getDatabasePlayer(UUID uuid) {
		if(uuid == null) return null;
		
		ObjectMutable<Boolean> b = new ObjectMutable<>(false);
		
		db.query("SELECT `UUID` FROM `users` WHERE `UUID`=?", rs -> {
			try {
				if(rs == null) {
					// stacktrace it
					throw new RuntimeException("WTF HOW IS RESULTSET NULL");
				}
				if(rs.next()) b.set(true);
			} catch (SQLException e) {
				e.printStackTrace();
			} 
		}, uuid);
		if(b.get())
			return new DatabasePlayer(uuid, pvm, pum);
		else
			return null;
	}
	
	/**
	 * Gets a CachedPlayer object holding all variables, cached. Only updates based on the set interval.
	 * For an instantly updated, database-linked version use {@link #getDatabasePlayer(UUID)} instead.
	 * 
	 * If there is no cached player for the given UUID, and the player has joined before, one will be created.
	 * 
	 * This will return null if the player has never joined. Use {@link #playerExists(UUID)} to check beforehand.
	 * 
	 * @param uuid The UUID of the player.
	 * @return the desired CachedPlayer object, from the playerCache.
	 */
	public CachedPlayer getCachedPlayer(UUID uuid) {
		if(uuid == null) return null;
		
		if(pcm.isInPlayerCache(uuid)) {
			return pcm.getUUIDCache().get(uuid);
		}
		
		if(!playerExists(uuid)) return null;
		
		// Else, we have to create a new CachedPlayer
		// First by getting a DatabasePlayer, then using that to get the variables.
		
		DatabasePlayer dp = getDatabasePlayer(uuid);
		
		CachedPlayer cp = new CachedPlayer(uuid, dp.getAllVariablesMap(), dp.getPunishments(), dp.getActivePunishments(), pvm, pum);
		pcm.addToPlayerCache(cp);
		
		return cp;
	}
	
	/**
	 * Creates a new player in the database, based on a new player joining.
	 * @param uuid the UUID of the new player.
	 */
	public CachedPlayer createNewPlayer(PendingConnection con) {
		UUID uuid = con.getUniqueId();
		if(playerExists(uuid))
			return getCachedPlayer(uuid);
		
		db.update("INSERT INTO `users` (`UUID`) VALUES (?)", uuid);
		
		Map<PlayerVariable, Object> newVariables = new HashMap<>();
		
		System.out.println(con.getName());
		newVariables.put(PlayerVariable.USERNAME, con.getName());
		newVariables.put(PlayerVariable.IP, con.getAddress().getAddress());
		newVariables.put(PlayerVariable.RANK, Rank.PLAYER);
		newVariables.put(PlayerVariable.PREFIX_C1, ChatColor.DARK_GRAY);
		newVariables.put(PlayerVariable.PREFIX_C2, ChatColor.DARK_GRAY);
		newVariables.put(PlayerVariable.FAKERANK_DATA, "");
		newVariables.put(PlayerVariable.LEVEL, 1);
		newVariables.put(PlayerVariable.EXPERIENCE, 0L);
		newVariables.put(PlayerVariable.NETWORK_BALANCE, 0L);
		newVariables.put(PlayerVariable.FIRSTJOIN, new Timestamp(System.currentTimeMillis()));
		newVariables.put(PlayerVariable.LASTQUIT, new Timestamp(System.currentTimeMillis()));
		newVariables.put(PlayerVariable.LASTJOIN, new Timestamp(System.currentTimeMillis()));
		newVariables.put(PlayerVariable.ADMIN_ALERT, false);
		newVariables.put(PlayerVariable.LANGUAGE, "en_US");
		newVariables.put(PlayerVariable.NICKNAME, con.getName());
		newVariables.put(PlayerVariable.VOTE_LAST, new Timestamp(0));
		newVariables.put(PlayerVariable.VOTE_STREAK, 0);
		newVariables.put(PlayerVariable.RANK_EXPIRETIME, new Timestamp(0));
		newVariables.put(PlayerVariable.RANK_EXPIRETO, Rank.PLAYER);
		
		DatabasePlayer dp = getDatabasePlayer(uuid);
		dp.setAllVariablesMap(newVariables);
		
		PlayerHistory hist = PlayerHistory.getPlayerHistory(dp);
		HistoryUpdateCache u = hist.createUpdateCache();
		u.addName(con.getName(), System.currentTimeMillis());
		u.addIp(con.getAddress().getAddress(), System.currentTimeMillis());
		u.addNickname(con.getName(), System.currentTimeMillis());
		u.pushUpdates();
		
		CachedPlayer cp = getCachedPlayer(uuid);
		return cp;
	}
	
	public Set<BasePlayer> getOnlineStaff() {
		Set<BasePlayer> staff = new HashSet<>();
		for(ProxiedPlayer pp : proxy.getPlayers()) {
			CachedPlayer cp = getCachedPlayer(pp.getUniqueId());
			if(((Rank)cp.getVariable(PlayerVariable.RANK)).isStaff())
				staff.add(cp);
		}
		return staff;
	}
	
	public Set<BasePlayer> getNotifiableStaff() {
		Set<BasePlayer> staff = new HashSet<>();
		for(ProxiedPlayer pp : proxy.getPlayers()) {
			CachedPlayer cp = getCachedPlayer(pp.getUniqueId());
			if(((Rank)cp.getVariable(PlayerVariable.RANK)).isStaff() && ((Boolean)cp.getVariable(PlayerVariable.ADMIN_ALERT)))
				staff.add(cp);
		}
		return staff;
	}
	
	/**
	 * Gets a UUID of the player only if a player has that as their current exact username.
	 */
	public UUID getFromCurrentExact(String currentExactName) {
		ObjectMutable<UUID> u = new ObjectMutable<UUID>(null);
		
		db.query(
				"SELECT `UUID` FROM `cur_data` WHERE `NAME`=?",
				rs -> {
					try {
						if(rs.next())
							u.set(UUID.fromString(rs.getString(1)));
					} catch (SQLException e) {
						e.printStackTrace();
					}
				}, currentExactName);
		
		return u.get();
	}
	
	/**
	 * Gets a UUID of the player only if a player has that as their current username.
	 */
	public UUID getFromCurrent(String currentName) {
		ObjectMutable<UUID> u = new ObjectMutable<UUID>(null);
		
		db.query(
				"SELECT `UUID` FROM `cur_data` WHERE `NAME` LIKE ?",
				rs -> {
					try {
						if(rs.next())
							u.set(UUID.fromString(rs.getString(1)));
					} catch (SQLException e) {
						e.printStackTrace();
					}
				}, "%" + currentName + "%");
		
		return u.get();
	}
	
	/**
	 * Gets a UUID of the player that has had that username.
	 */
	public UUID getFromPastExact(String exactName) {
		ObjectMutable<UUID> u = new ObjectMutable<UUID>(null);
		
		db.query(
				"SELECT `UUID` FROM `prev_names` WHERE `NAME`=? ORDER BY `INITIAL` DESC",
				rs -> {
					try {
						if(rs.next())
							u.set(UUID.fromString(rs.getString(1)));
					} catch (SQLException e) {
						e.printStackTrace();
					}
				}, exactName);
		
		return u.get();
	}
	
	/**
	 * Gets a UUID of the player that has had that username.
	 */
	public UUID getFromPast(String name) {
		ObjectMutable<UUID> u = new ObjectMutable<UUID>(null);
		
		db.query(
				"SELECT `UUID` FROM `prev_names` WHERE `NAME` LIKE ? ORDER BY `INITIAL` DESC",
				rs -> {
					try {
						if(rs.next())
							u.set(UUID.fromString(rs.getString(1)));
					} catch (SQLException e) {
						e.printStackTrace();
					}
				}, "%" + name + "%");
		
		return u.get();
	}
	
	/**
	 * Gets a UUID of the player only if a player has that as their current exact nickname.
	 */
	public UUID getFromCurrentExactNick(String currentExactNick) {
		ObjectMutable<UUID> u = new ObjectMutable<UUID>(null);
		
		db.query(
				"SELECT `UUID` FROM `player_data` WHERE `TYPE`='nickname' AND `VALUE`=?",
				rs -> {
					try {
						if(rs.next())
							u.set(UUID.fromString(rs.getString(1)));
					} catch (SQLException e) {
						e.printStackTrace();
					}
				}, currentExactNick);
		
		return u.get();
	}
	
	/**
	 * Gets a UUID of the player only if a player has that as their current nickname.
	 */
	public UUID getFromCurrentNick(String currentNick) {
		ObjectMutable<UUID> u = new ObjectMutable<UUID>(null);
		
		db.query(
				"SELECT `UUID` FROM `player_data` WHERE `TYPE`='nickname' AND `VALUE` LIKE ?",
				rs -> {
					try {
						if(rs.next())
							u.set(UUID.fromString(rs.getString(1)));
					} catch (SQLException e) {
						e.printStackTrace();
					}
				}, "%" + currentNick + "%");
		
		return u.get();
	}
	
	/**
	 * Gets a UUID of the player only if a player has had that exact nickname.
	 */
	public UUID getFromPastExactNick(String exactNick) {
		ObjectMutable<UUID> u = new ObjectMutable<UUID>(null);
		
		db.query(
				"SELECT `UUID` FROM `prev_nicknames` WHERE `NICKNAME`=? ORDER BY `INITIAL` DESC",
				rs -> {
					try {
						if(rs.next())
							u.set(UUID.fromString(rs.getString(1)));
					} catch (SQLException e) {
						e.printStackTrace();
					}
				}, exactNick);
		
		return u.get();
	}
	
	/**
	 * Gets a UUID of the player only if a player has had that nickname.
	 */
	public UUID getFromPastNick(String nick) {
		ObjectMutable<UUID> u = new ObjectMutable<UUID>(null);
		
		db.query(
				"SELECT `UUID` FROM `prev_nicknames` WHERE `NICKNAME` LIKE ? ORDER BY `INITIAL` DESC",
				rs -> {
					try {
						if(rs.next())
							u.set(UUID.fromString(rs.getString(1)));
					} catch (SQLException e) {
						e.printStackTrace();
					}
				}, "%" + nick + "%");
		
		return u.get();
	}
	
	/**
	 * Combines {@link #getFromPast(String)} and {@link #getFromPastNick(String)}.
	 * Includes Past and Current (Because all current names are also stored in the name history table).
	 */
	public UUID getFromAny(String name) {
		ObjectMutable<UUID> nu = new ObjectMutable<UUID>(null);
		ObjectMutable<UUID> u = new ObjectMutable<UUID>(null);
		
		Map<DoubleObject<String, Object[]>, Consumer<ResultSet>> queries = new HashMap<>();
		
		queries.put(new DoubleObject<>("SELECT `UUID` FROM `prev_names` WHERE `NAME` LIKE ? ORDER BY `INITIAL` DESC", new Object[] {"%" + name + "%"}), rs -> {
			try {
				if(rs.next())
					u.set(UUID.fromString(rs.getString(1)));
			} catch (SQLException e) {
				e.printStackTrace();
			}
		});
		queries.put(new DoubleObject<>("SELECT `UUID` FROM `prev_nicknames` WHERE `NICKNAME` LIKE ? ORDER BY `INITIAL` DESC", new Object[] {"%" + name + "%"}), rs -> {
			try {
				if(rs.next())
					nu.set(UUID.fromString(rs.getString(1)));
			} catch (SQLException e) {
				e.printStackTrace();
			}
		});
		db.multiQuery(queries);
		
		if(u.get() != null)
			return u.get();
		return nu.get();
		
	}
	
	/**
	 * Bans the player, kicking them if online.
	 * Set duration to -1 for permanent.
	 * 
	 * return false if player is already banned, true otherwise.
	 * 
	 * Broadcast will not be sent if silent is true, or if the player is offline.
	 */
	public boolean banPlayer(BasePlayer p, long duration, String reason, BasePlayer author, boolean silent) {
		for(Punishment pun : p.getActivePunishments())
			if(pun instanceof Ban && ((Ban)pun).isActive()) return false;
		
		ProxiedPlayer pp = proxy.getPlayer(p.getUniqueId());
		int server = 0;
		// No prefixes to reduce chat clutter
		// NICKNAME should only be used for at-the-moment things, such as chat
		// but not for this because someone could use it to detect the real name of the nicked person by checking it when they arent nicked then again when they are.
		String authorName = MSG.CONSOLE.getMessage(Locale.getDefault());
		if(author != null)
			authorName = pl.getRankManager().getRank(author, false).getColor() + (String) author.getVariable(PlayerVariable.USERNAME);
		String targetName = pl.getRankManager().getRank(p, true).getColor() + (String) p.getVariable(PlayerVariable.NICKNAME);
		String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("d MMM uuuu"));
		String durStr = MSG.TIMEFORMAT_PERMANENT.getMessage(p);
		String timeLeftStr = MSG.BANNED_TIMEPERMANENT.getMessage(p);
		if(duration != -1) {
			Duration dur = Duration.ofMillis(duration);
			long days = dur.get(ChronoUnit.DAYS); dur.minusDays(days);
			long hours = dur.get(ChronoUnit.HOURS); dur.minusHours(hours);
			long minutes = dur.get(ChronoUnit.MINUTES);
			durStr = (days > 0 ? days + MSG.TIMEFORMAT_DAYS.getMessage(p) + ", " : "")
					+ (hours > 0 ? hours + MSG.TIMEFORMAT_HOURS.getMessage(p) + ", " : "")
					+ minutes + MSG.TIMEFORMAT_MINUTES.getMessage(p);
			Duration tlDur = Duration.ofMillis(duration);
			long tldays = tlDur.get(ChronoUnit.DAYS); tlDur.minusDays(days);
			long tlhours = tlDur.get(ChronoUnit.HOURS); tlDur.minusHours(hours);
			long tlminutes = tlDur.get(ChronoUnit.MINUTES);
			String timeLeft = (tldays > 0 ? tldays + MSG.TIMEFORMAT_DAYS.getMessage(p) + ", " : "")
					+ (tlhours > 0 ? tlhours + MSG.TIMEFORMAT_HOURS.getMessage(p) + ", " : "")
					+ tlminutes + MSG.TIMEFORMAT_MINUTES.getMessage(p);
			timeLeftStr = MSG.BANNED_TIMEREMAINING.getMessage(p, timeLeft);
		}
		if(pp != null) {
			server = pl.getServerID(pp.getServer().getInfo().getName());
			String bm = MSG.KICKMESSAGE_BAN.getMessage(p, MSG.BANSCREEN_LINE.getMessage(p), MSG.PUNISHMESSAGE_HAVEBEEN.getMessage(p), authorName, date, durStr, reason, timeLeftStr, MSG.APPEAL_URL.getMessage(p), MSG.BANSCREEN_LINE.getMessage(p));
			TextComponent banMessage = new TextComponent(TextComponent.fromLegacyText(bm));
			pp.disconnect(banMessage);
			
			if(!silent) {
				if(duration != 1)
					Message.broadcast(MSG.PUBLIC_TEMPBANALERT, targetName, durStr, authorName, reason);
				else
					Message.broadcast(MSG.PUBLIC_BANALERT, targetName, authorName, reason);
			}
		}

		Set<BasePlayer> staff = new HashSet<>();
		for(ProxiedPlayer ap : proxy.getPlayers()) {
			CachedPlayer cp = getCachedPlayer(ap.getUniqueId());
			if(cp != null && ((Rank) cp.getVariable(PlayerVariable.RANK)).isStaff())
				staff.add(cp);
		}
		String srvName = pl.getServerName(server);
		if(server == 0) srvName = MSG.ADMIN_OFFLINE.getMessage(Locale.getDefault());
		if(duration != 1) {
			Message.broadcast(staff, MSG.ADMIN_TEMPBANALERT, srvName, targetName, durStr, authorName, reason, silent ? MSG.ADMIN_SILENT.getMessage(Locale.getDefault()) : "");
			Message.sendConsole(MSG.ADMIN_TEMPBANALERT, srvName, targetName, durStr, authorName, reason, silent ? MSG.ADMIN_SILENT.getMessage(Locale.getDefault()) : "");
		} else {
			Message.broadcast(staff, MSG.ADMIN_BANALERT, srvName, targetName, authorName, reason, silent ? MSG.ADMIN_SILENT.getMessage(Locale.getDefault()) : "");
			Message.sendConsole(MSG.ADMIN_BANALERT, srvName, targetName, authorName, reason, silent ? MSG.ADMIN_SILENT.getMessage(Locale.getDefault()) : "");
		}
		
		pum.executeBan(p.getUniqueId(), duration, author != null ? author.getUniqueId() : new UUID(0, 0), reason, server);
		return true;
	}
	
	/**
	 * Mutes the player, notifying them if online.
	 * Set duration to -1 for permanent.
	 * 
	 * returns false if player is already muted, otherwise returns true.
	 * 
	 * Broadcast will not be sent if silent is true, or if the player is offline.
	 */
	public boolean mutePlayer(BasePlayer p, long duration, String reason, BasePlayer author, boolean silent) {
		for(Punishment pun : p.getActivePunishments())
			if(pun instanceof Mute && ((Mute)pun).isActive()) return false;
		
		ProxiedPlayer pp = proxy.getPlayer(p.getUniqueId());
		int server = 0;
		// No prefixes to reduce chat clutter
		// NICKNAME should only be used for at-the-moment things, such as chat
		// but not for this because someone could use it to detect the real name of the nicked person by checking it when they arent nicked then again when they are.
		String authorName = MSG.CONSOLE.getMessage(Locale.getDefault());
		if(author != null)
			authorName = pl.getRankManager().getRank(author, false).getColor() + (String) author.getVariable(PlayerVariable.USERNAME);
		String targetName = pl.getRankManager().getRank(p, true).getColor() + (String) p.getVariable(PlayerVariable.NICKNAME);
		String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("d MMM uuuu"));
		String durStr = MSG.TIMEFORMAT_PERMANENT.getMessage(p);
		String timeLeftStr = MSG.TIMEFORMAT_PERMANENT.getMessage(p);
		if(duration != -1) {
			Duration dur = Duration.ofMillis(duration);
			long days = dur.get(ChronoUnit.DAYS); dur.minusDays(days);
			long hours = dur.get(ChronoUnit.HOURS); dur.minusHours(hours);
			long minutes = dur.get(ChronoUnit.MINUTES);
			durStr = (days > 0 ? days + MSG.TIMEFORMAT_DAYS.getMessage(p) + ", " : "")
					+ (hours > 0 ? hours + MSG.TIMEFORMAT_HOURS.getMessage(p) + ", " : "")
					+ minutes + MSG.TIMEFORMAT_MINUTES.getMessage(p);
			Duration tlDur = Duration.ofMillis(duration);
			long tldays = tlDur.get(ChronoUnit.DAYS); tlDur.minusDays(days);
			long tlhours = tlDur.get(ChronoUnit.HOURS); tlDur.minusHours(hours);
			long tlminutes = tlDur.get(ChronoUnit.MINUTES);
			timeLeftStr = (tldays > 0 ? tldays + MSG.TIMEFORMAT_DAYS.getMessage(p) + ", " : "")
					+ (tlhours > 0 ? tlhours + MSG.TIMEFORMAT_HOURS.getMessage(p) + ", " : "")
					+ tlminutes + MSG.TIMEFORMAT_MINUTES.getMessage(p);
		}
		if(pp != null) {
			server = pl.getServerID(pp.getServer().getInfo().getName());
			if(duration != 1) {
				if(!silent)
					Message.broadcast(MSG.PUBLIC_TEMPMUTEALERT, targetName, durStr, authorName, reason);
				
				Message.sendMessage(p, MSG.MUTEMESSAGE_TEMPORARY, MSG.PUNISHMESSAGE_HAVEBEEN.getMessage(p), durStr, authorName, reason, date, timeLeftStr, MSG.APPEAL_URL.getMessage(p));
			} else {
				if(!silent)
					Message.broadcast(MSG.PUBLIC_MUTEALERT, targetName, authorName, reason);
				
				Message.sendMessage(p, MSG.MUTEMESSAGE_PERMANENT, MSG.PUNISHMESSAGE_HAVEBEEN.getMessage(p), authorName, reason, date, MSG.APPEAL_URL.getMessage(p));
			}
		}

		Set<BasePlayer> staff = new HashSet<>();
		for(ProxiedPlayer ap : proxy.getPlayers()) {
			CachedPlayer cp = getCachedPlayer(ap.getUniqueId());
			if(cp != null && ((Rank) cp.getVariable(PlayerVariable.RANK)).isStaff())
				staff.add(cp);
		}
		String srvName = pl.getServerName(server);
		if(server == 0) srvName = MSG.ADMIN_OFFLINE.getMessage(Locale.getDefault());
		if(duration != 1) {
			Message.broadcast(staff, MSG.ADMIN_TEMPMUTEALERT, srvName, targetName, durStr, authorName, reason, silent ? MSG.ADMIN_SILENT.getMessage(Locale.getDefault()) : "");
			Message.sendConsole(MSG.ADMIN_TEMPMUTEALERT, srvName, targetName, durStr, authorName, reason, silent ? MSG.ADMIN_SILENT.getMessage(Locale.getDefault()) : "");
		} else {
			Message.broadcast(staff, MSG.ADMIN_MUTEALERT, srvName, targetName, authorName, reason, silent ? MSG.ADMIN_SILENT.getMessage(Locale.getDefault()) : "");
			Message.sendConsole(MSG.ADMIN_MUTEALERT, srvName, targetName, authorName, reason, silent ? MSG.ADMIN_SILENT.getMessage(Locale.getDefault()) : "");
		}
		
		pum.executeMute(p.getUniqueId(), duration, author != null ? author.getUniqueId() : new UUID(0, 0), reason, server);
		return true;
	}
	
	/**
	 * Blacklists the player, kicking them if online.
	 * 
	 * Broadcast will not be sent if silent is true, or if the player is offline.
	 * 
	 * returns false if already blacklisted, otherwise returns true.
	 * 
	 * TODO add a script to firewall the player.
	 */
	public boolean blacklistPlayer(BasePlayer p, String reason, BasePlayer author, boolean silent) {
		for(Punishment pun : p.getActivePunishments())
			if(pun instanceof Blacklist && ((Blacklist)pun).isActive()) return false;
		
		ProxiedPlayer pp = proxy.getPlayer(p.getUniqueId());
		int server = 0;
		// No prefixes to reduce chat clutter
		// NICKNAME should only be used for at-the-moment things, such as chat
		// but not for this because someone could use it to detect the real name of the nicked person by checking it when they arent nicked then again when they are.
		String authorName = MSG.CONSOLE.getMessage(Locale.getDefault());
		if(author != null)
			authorName = pl.getRankManager().getRank(author, false).getColor() + (String) author.getVariable(PlayerVariable.USERNAME);
		String targetName = pl.getRankManager().getRank(p, true).getColor() + (String) p.getVariable(PlayerVariable.NICKNAME);
		String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("d MMM uuuu"));
		if(pp != null) {
			server = pl.getServerID(pp.getServer().getInfo().getName());
			String bm = MSG.KICKMESSAGE_BLACKLIST.getMessage(p, MSG.BANSCREEN_LINE.getMessage(p), MSG.PUNISHMESSAGE_HAVEBEEN.getMessage(p), authorName, date, reason, MSG.BANSCREEN_LINE.getMessage(p));
			TextComponent blacklistMessage = new TextComponent(TextComponent.fromLegacyText(bm));
			pp.disconnect(blacklistMessage);
			
			if(!silent)
				Message.broadcast(MSG.PUBLIC_BLACKLISTALERT, MSG.BLACKLIST_LINE.getMessage(Locale.getDefault()), targetName, authorName, reason, MSG.BLACKLIST_LINE.getMessage(Locale.getDefault()));
		}

		Set<BasePlayer> staff = new HashSet<>();
		for(ProxiedPlayer ap : proxy.getPlayers()) {
			CachedPlayer cp = getCachedPlayer(ap.getUniqueId());
			if(cp != null && ((Rank) cp.getVariable(PlayerVariable.RANK)).isStaff())
				staff.add(cp);
		}
		Message.broadcast(staff, MSG.ADMIN_BLACKLISTALERT, targetName, authorName, reason, silent ? MSG.ADMIN_SILENT.getMessage(Locale.getDefault()) : "");
		Message.sendConsole(MSG.ADMIN_BLACKLISTALERT, targetName, authorName, reason, silent ? MSG.ADMIN_SILENT.getMessage(Locale.getDefault()) : "");
		
		pum.executeBlacklist(p.getUniqueId(), author != null ? author.getUniqueId() : new UUID(0, 0), reason, server);
		return true;
	}
	
	/**
	 * Kicks the player if online.
	 * 
	 * Broadcast will not be sent if silent is true.
	 */
	public void kickPlayer(BasePlayer p, String reason, BasePlayer author, boolean silent) {
		ProxiedPlayer pp = proxy.getPlayer(p.getUniqueId());
		if(pp == null) return;
		int server = pl.getServerID(pp.getServer().getInfo().getName());
		// No prefixes to reduce chat clutter
		// NICKNAME should only be used for at-the-moment things, such as chat
		// but not for this because someone could use it to detect the real name of the nicked person by checking it when they arent nicked then again when they are.
		String authorName = MSG.CONSOLE.getMessage(Locale.getDefault());
		if(author != null)
			authorName = pl.getRankManager().getRank(author, false).getColor() + (String) author.getVariable(PlayerVariable.USERNAME);
		String targetName = pl.getRankManager().getRank(p, true).getColor() + (String) p.getVariable(PlayerVariable.NICKNAME);
		String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("d MMM uuuu"));
		String km = MSG.KICKMESSAGE_KICK.getMessage(MSG.BANSCREEN_LINE.getMessage(p), p, authorName, date, reason, MSG.SUPPORT_EMAIL.getMessage(p), MSG.BANSCREEN_LINE.getMessage(p));
		TextComponent kickMessage = new TextComponent(TextComponent.fromLegacyText(km));
		pp.disconnect(kickMessage);
		
		if(!silent)
			Message.broadcast(MSG.PUBLIC_KICKALERT, targetName, authorName, reason);

		Set<BasePlayer> staff = new HashSet<>();
		for(ProxiedPlayer ap : proxy.getPlayers()) {
			CachedPlayer cp = getCachedPlayer(ap.getUniqueId());
			if(cp != null && ((Rank) cp.getVariable(PlayerVariable.RANK)).isStaff())
				staff.add(cp);
		}
		String srvName = pl.getServerName(server);
		Message.broadcast(staff, MSG.ADMIN_KICKALERT, srvName, targetName, authorName, reason, silent ? MSG.ADMIN_SILENT.getMessage(Locale.getDefault()) : "");
		Message.sendConsole(MSG.ADMIN_KICKALERT, srvName, targetName, authorName, reason, silent ? MSG.ADMIN_SILENT.getMessage(Locale.getDefault()) : "");
		
		pum.executeKick(p.getUniqueId(), author != null ? author.getUniqueId() : new UUID(0, 0), reason, server);
	}
	
	/**
	 * Unbans the player.
	 * 
	 * returns false if player is not banned, otherwise returns true.
	 * 
	 * Broadcast will not be sent if silent is true.
	 */
	public boolean unbanPlayer(BasePlayer p, String reason, BasePlayer unbanAuthor, boolean silent) {
		// No prefixes to reduce chat clutter
		// NICKNAME should only be used for at-the-moment things, such as chat
		// but not for this because someone could use it to detect the real name of the nicked person by checking it when they arent nicked then again when they are.
		String authorName = MSG.CONSOLE.getMessage(Locale.getDefault());
		if(unbanAuthor != null)
			authorName = pl.getRankManager().getRank(unbanAuthor, false).getColor() + (String) unbanAuthor.getVariable(PlayerVariable.USERNAME);
		String targetName = pl.getRankManager().getRank(p, true).getColor() + (String) p.getVariable(PlayerVariable.USERNAME);
		
		Ban ban = null;
		for(Punishment pun : p.getActivePunishments())
			if(pun instanceof Ban) ban = (Ban) pun;
		if(ban == null || !ban.isActive()) return false;
		
		if(!silent)
			Message.broadcast(MSG.PUBLIC_UNBANALERT, targetName, authorName);

		Set<BasePlayer> staff = new HashSet<>();
		for(ProxiedPlayer ap : proxy.getPlayers()) {
			CachedPlayer cp = getCachedPlayer(ap.getUniqueId());
			if(cp != null && ((Rank) cp.getVariable(PlayerVariable.RANK)).isStaff())
				staff.add(cp);
		}
		Message.broadcast(staff, MSG.ADMIN_UNBANALERT, targetName, authorName, reason, silent ? MSG.ADMIN_SILENT.getMessage(Locale.getDefault()) : "");
		Message.sendConsole(MSG.ADMIN_UNBANALERT, targetName, authorName, reason, silent ? MSG.ADMIN_SILENT.getMessage(Locale.getDefault()) : "");
		
		pum.executeUnBan(ban, reason, unbanAuthor != null ? unbanAuthor.getUniqueId() : new UUID(0, 0));
		return true;
	}
	
	/**
	 * Unmutes the player.
	 * 
	 * returns false if player is not muted, otherwise returns true.
	 * 
	 * Broadcast will not be sent if silent is true.
	 */
	public boolean unmutePlayer(BasePlayer p, String reason, BasePlayer unmuteAuthor, boolean silent) {
		// No prefixes to reduce chat clutter
		// NICKNAME should only be used for at-the-moment things, such as chat
		// but not for this because someone could use it to detect the real name of the nicked person by checking it when they arent nicked then again when they are.
		String authorName = MSG.CONSOLE.getMessage(Locale.getDefault());
		if(unmuteAuthor != null)
			authorName = pl.getRankManager().getRank(unmuteAuthor, false).getColor() + (String) unmuteAuthor.getVariable(PlayerVariable.USERNAME);
		String targetName = pl.getRankManager().getRank(p, true).getColor() + (String) p.getVariable(PlayerVariable.USERNAME);
		
		Mute mute = null;
		for(Punishment pun : p.getActivePunishments())
			if(pun instanceof Mute) mute = (Mute) pun;
		if(mute == null || !mute.isActive()) return false;
		
		if(!silent)
			Message.broadcast(MSG.PUBLIC_UNMUTEALERT, targetName, authorName);

		Set<BasePlayer> staff = new HashSet<>();
		for(ProxiedPlayer ap : proxy.getPlayers()) {
			CachedPlayer cp = getCachedPlayer(ap.getUniqueId());
			if(cp != null && ((Rank) cp.getVariable(PlayerVariable.RANK)).isStaff())
				staff.add(cp);
		}
		Message.broadcast(staff, MSG.ADMIN_UNMUTEALERT, targetName, authorName, reason, silent ? MSG.ADMIN_SILENT.getMessage(Locale.getDefault()) : "");
		Message.sendConsole(MSG.ADMIN_UNMUTEALERT, targetName, authorName, reason, silent ? MSG.ADMIN_SILENT.getMessage(Locale.getDefault()) : "");
		
		pum.executeUnMute(mute, reason, unmuteAuthor != null ? unmuteAuthor.getUniqueId() : new UUID(0, 0));
		return true;
	}
	
	/**
	 * Unblacklists the player.
	 * 
	 * returns false if player is not blacklisted, otherwise returns true.
	 * 
	 * Broadcast will not be sent if silent is true.
	 * 
	 * TODO add a script to unfirewall the player.
	 */
	public boolean unblacklistPlayer(BasePlayer p, String reason, BasePlayer unblacklistAuthor, boolean silent) {
		// NICKNAME should only be used for at-the-moment things, such as chat
		// but not for this because someone could use it to detect the real name of the nicked person by checking it when they arent nicked then again when they are.
		String authorName = MSG.CONSOLE.getMessage(Locale.getDefault());
		if(unblacklistAuthor != null)
			authorName = pl.getRankManager().getRank(unblacklistAuthor, false).getColor() + (String) unblacklistAuthor.getVariable(PlayerVariable.USERNAME);
		String targetName = pl.getRankManager().getPrefix(p) + (String) p.getVariable(PlayerVariable.NICKNAME);
		
		Blacklist blacklist = null;
		for(Punishment pun : p.getActivePunishments())
			if(pun instanceof Blacklist) blacklist = (Blacklist) pun;
		if(blacklist == null || !blacklist.isActive()) return false;
		
		if(!silent)
			Message.broadcast(MSG.PUBLIC_UNBLACKLISTALERT, targetName, authorName);

		Set<BasePlayer> staff = new HashSet<>();
		for(ProxiedPlayer ap : proxy.getPlayers()) {
			CachedPlayer cp = getCachedPlayer(ap.getUniqueId());
			if(cp != null && ((Rank) cp.getVariable(PlayerVariable.RANK)).isStaff())
				staff.add(cp);
		}
		Message.broadcast(staff, MSG.ADMIN_UNBLACKLISTALERT, targetName, authorName, reason, silent ? MSG.ADMIN_SILENT.getMessage(Locale.getDefault()) : "");
		Message.sendConsole(MSG.ADMIN_UNBLACKLISTALERT, targetName, authorName, reason, silent ? MSG.ADMIN_SILENT.getMessage(Locale.getDefault()) : "");

		pum.executeUnBlacklist(blacklist, reason, unblacklistAuthor != null ? unblacklistAuthor.getUniqueId() : new UUID(0, 0));
		return true;
	}
	
	public Set<UUID> getUUIDsAssociatedWithIP(InetAddress ip) {
		Set<UUID> uuids = new HashSet<>();
		db.query("SELECT DISTINCT `UUID` FROM `prev_ips` WHERE `IP`=? ORDER BY `INITIAL` DESC", rs -> {
			try {
				while(rs.next())
					uuids.add(UUID.fromString(rs.getString(1)));
			} catch (SQLException e) {
				e.printStackTrace();
			} 
		}, ip);
		return uuids;
	}
	
	/**
	 * Set ALL permissions that should be set for that player.
	 */
	public void setJoinedPlayerPermissions(BasePlayer bp) {
		Map<String, Boolean> perms = prm.getAllPermissions(bp);
		ProxiedPlayer pp = proxy.getPlayer(bp.getUniqueId());
		if(pp != null)
			for(Entry<String, Boolean> perm : perms.entrySet())
				pp.setPermission(perm.getKey(), perm.getValue());
	}
	
	/**
	 * Check for expired permissions for online player, and remove them.
	 */
	public void removeExpiredPermissions(BasePlayer bp) {
		Set<String> exp = prm.getExpiredPlayerPermissions(bp);
		ProxiedPlayer pp = proxy.getPlayer(bp.getUniqueId());
		prm.removePlayerPermissions(bp, exp);
		if(pp != null) {
			for(String perm : exp) {
				pp.setPermission(perm, false);
				Message.sendMessage(bp, MSG.PLAYER_REMOVEDPERM, perm.toUpperCase());
			}
		}
	}
	
	/**
	 * Check for expired rank for online player.
	 * If expired, will be demoted to their DEMOTETO rank.
	 * @return true if expired+demoted, false if their rank is still valid.
	 */
	public boolean demoteIfRankExpired(CachedPlayer bp) {
		Rank cur = getRankManager().getRank(bp, false);
		Instant exp = getRankManager().getRankExpiry(bp);
		if(exp == null || exp.isAfter(Instant.now())) return false;
		Rank demote = getRankManager().getRankExprireTo(bp);
		PlayerUpdateCache u = bp.createUpdateCache();
		bp.setVariableInUpdate(u, PlayerVariable.RANK, demote);
		bp.setVariableInUpdate(u, PlayerVariable.RANK_EXPIRETIME, new Timestamp(0));
		u.pushUpdates();
		Message.sendMessage(bp, MSG.PLAYER_REMOVEDRANK, cur.getColor() + cur.getName(), MSG.PLAYER_REMOVEDEXPIRED.getMessage(bp));
		return true;
	}
	
}
