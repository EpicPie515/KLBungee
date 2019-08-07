package lol.kangaroo.bungee.player.punish;

import java.net.InetAddress;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;

import lol.kangaroo.common.database.DatabaseManager;
import lol.kangaroo.common.player.punish.Ban;
import lol.kangaroo.common.player.punish.Blacklist;
import lol.kangaroo.common.player.punish.IPunishManager;
import lol.kangaroo.common.player.punish.Kick;
import lol.kangaroo.common.player.punish.Mute;
import lol.kangaroo.common.player.punish.Punishment;
import lol.kangaroo.common.util.DoubleObject;
import lol.kangaroo.common.util.ObjectMutable;

public class PunishManager implements IPunishManager {
	
	public static final UUID ZERO_UUID = new UUID(0, 0);
	
	private DatabaseManager db;
	
	public PunishManager(DatabaseManager db) {
		this.db = db;
	}
	
	/** 
	 * Instead of keeping the whole punishment in cache (RAM intensive at some scale) we only keep whether they have any active mute/ban
	 * This allows to cancel the event based on cache, then get the rest of the punishment in async from the db. 
	 */
	private Set<UUID> mutedCache = new HashSet<>();
	private Set<UUID> bannedCache = new HashSet<>();
	
	/** method for all punishment caches. Automatically determines the type of punishment and adds the target appropriately.
	 *  WILL NOT REMOVE IF INACTIVE, could cause conflict with multiple active mutes (Which should never happen but still possible)
	 *  @throws IllegalArgumentException if Punishment is not an instance of Mute or Ban
	 */
	private void addToCache(UUID uuid, Punishment pun) {
		if(pun instanceof Mute) {
			if(pun.isActive())
				mutedCache.add(uuid);
		} else if(pun instanceof Ban) {
			if(pun.isActive())
				bannedCache.add(uuid);
		} else {
			throw new IllegalArgumentException("Punishment is not instance of Mute or Ban");
		}
	}
	
	private void removeFromBanCache(UUID uuid) {
		bannedCache.remove(uuid);
	}
	
	private void removeFromMuteCache(UUID uuid) {
		mutedCache.remove(uuid);
	}
	
	public boolean isInBanCache(UUID uuid) {
		return bannedCache.contains(uuid);
	}
	
	public boolean isInMuteCache(UUID uuid) {
		return mutedCache.contains(uuid);
	}
	
	/**
	 * returns a COPY of the ban cache.
	 * @return
	 */
	public Set<UUID> getBanCache() {
		return new HashSet<UUID>(bannedCache);
	}
	
	/**
	 * returns a COPY of the mute cache.
	 * @return
	 */
	public Set<UUID> getMuteCache() {
		return new HashSet<UUID>(mutedCache);
	}
	
	/**
	 * Called by PlayerCacheManager when the player cache is flushed.
	 */
	public void flushPunishmentCache() {
		Set<UUID> bc = new HashSet<>();
		Set<UUID> mc = new HashSet<>();
		Map<DoubleObject<String, Object[]>, Consumer<ResultSet>> queries = new HashMap<>();
		queries.put(new DoubleObject<>("SELECT `UUID` FROM `log_bans` WHERE `ACTIVE`=1", new Object[0]), rs -> {
			try {
				while(rs.next()) {
					bc.add(UUID.fromString(rs.getString(1)));
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		});
		queries.put(new DoubleObject<>("SELECT `UUID` FROM `log_blacklist` WHERE `ACTIVE`=1", new Object[0]), rs -> {
			try {
				while(rs.next()) {
					bc.add(UUID.fromString(rs.getString(1)));
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		});
		queries.put(new DoubleObject<>("SELECT `UUID` FROM `log_mute` WHERE `ACTIVE`=1", new Object[0]), rs -> {
			try {
				while(rs.next()) {
					mc.add(UUID.fromString(rs.getString(1)));
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		});
		db.multiQuery(queries);
		bannedCache = new HashSet<UUID>(bc);
		mutedCache = new HashSet<UUID>(mc);
	}
	
	@Override
	public Set<Punishment> getPunishments(UUID uuid) {
		Set<Punishment> punishments = new HashSet<>();
		Map<DoubleObject<String, Object[]>, Consumer<ResultSet>> queries = new HashMap<>();
		queries.put(
				new DoubleObject<>("SELECT `TIMESTAMP`, `DURATION`, "
				+ "`REASON`, `AUTHOR`, `UNREASON`, `UNTIMESTAMP`, `UNAUTHOR`, `SERVER`, `ACTIVE` "
				+ "FROM `log_ban` WHERE `UUID`=? ORDER BY `TIMESTAMP` DESC", new Object[] { uuid }),
				rs -> {
					try {
						while(rs.next()) {
							long timestamp = rs.getTimestamp(1).getTime();
							long duration = rs.getLong(2);
							String reason = rs.getString(3);
							UUID author = UUID.fromString(rs.getString(4));
							String unReason = rs.getString(5);
							Timestamp unTimestampObj = rs.getTimestamp(6);
							long unTimestamp = 0;
							if(unTimestampObj != null) unTimestamp = unTimestampObj.getTime();
							String unAuthorStr = rs.getString(7);
							UUID unAuthor = null;
							if(unAuthorStr != null) unAuthor = UUID.fromString(unAuthorStr);
							int server = rs.getInt(8);
							boolean active = rs.getBoolean(9);
							
							Ban ban = new Ban(uuid, timestamp, duration, 
									reason, author, unReason, unTimestamp, unAuthor, server, active);
							punishments.add(ban);
						}
					} catch(SQLException e) {
						e.printStackTrace();
					}
				});
		queries.put(
				new DoubleObject<>("SELECT `TIMESTAMP`, "
				+ "`REASON`, `AUTHOR`, `UNREASON`, `UNTIMESTAMP`, `UNAUTHOR`, `SERVER`, `ACTIVE` "
				+ "FROM `log_blacklist` WHERE `UUID`=?", new Object[] { uuid }),
				rs -> {
					try {
						while(rs.next()) {
							long timestamp = rs.getTimestamp(1).getTime();
							String reason = rs.getString(2);
							UUID author = UUID.fromString(rs.getString(3));
							String unReason = rs.getString(4);
							Timestamp unTimestampObj = rs.getTimestamp(5);
							long unTimestamp = 0;
							if(unTimestampObj != null) unTimestamp = unTimestampObj.getTime();
							String unAuthorStr = rs.getString(6);
							UUID unAuthor = null;
							if(unAuthorStr != null) unAuthor = UUID.fromString(unAuthorStr);
							int server = rs.getInt(7);
							boolean active = rs.getBoolean(8);
							
							Blacklist blacklist = new Blacklist(uuid, timestamp, 
									reason, author, unReason, unTimestamp, unAuthor, server, active);
							punishments.add(blacklist);
						}
					} catch(SQLException e) {
						e.printStackTrace();
					}
				});
		queries.put(
				new DoubleObject<>("SELECT `TIMESTAMP`, `DURATION`, "
				+ "`REASON`, `AUTHOR`, `UNREASON`, `UNTIMESTAMP`, `UNAUTHOR`, `SERVER`, `ACTIVE` "
				+ "FROM `log_mute` WHERE `UUID`=?", new Object[] { uuid }),
				rs -> {
					try {
						while(rs.next()) {
							long timestamp = rs.getTimestamp(1).getTime();
							long duration = rs.getLong(2);
							String reason = rs.getString(3);
							UUID author = UUID.fromString(rs.getString(4));
							String unReason = rs.getString(5);
							Timestamp unTimestampObj = rs.getTimestamp(6);
							long unTimestamp = 0;
							if(unTimestampObj != null) unTimestamp = unTimestampObj.getTime();
							String unAuthorStr = rs.getString(7);
							UUID unAuthor = null;
							if(unAuthorStr != null) unAuthor = UUID.fromString(unAuthorStr);
							int server = rs.getInt(8);
							boolean active = rs.getBoolean(9);
							
							Mute mute = new Mute(uuid, timestamp, duration, 
									reason, author, unReason, unTimestamp, unAuthor, server, active);
							punishments.add(mute);
						}
					} catch(SQLException e) {
						e.printStackTrace();
					}
				});
		queries.put(
				new DoubleObject<>("SELECT `TIMESTAMP`, `REASON`, `AUTHOR`, `SERVER` "
				+ "FROM `log_mute` WHERE `UUID`=?", new Object[] { uuid }),
				rs -> {
					try {
						while(rs.next()) {
							long timestamp = rs.getTimestamp(1).getTime();
							String reason = rs.getString(2);
							UUID author = UUID.fromString(rs.getString(3));
							int server = rs.getInt(4);
							
							Kick kick = new Kick(uuid, timestamp, reason, author, server);
							punishments.add(kick);
						}
					} catch(SQLException e) {
						e.printStackTrace();
					}
				});
		
		db.multiQuery(queries);
		
		return punishments;
	}
	
	@Override
	public Set<Punishment> getActivePunishments(UUID uuid) {
		Set<Punishment> punishments = new HashSet<>();
		Map<DoubleObject<String, Object[]>, Consumer<ResultSet>> queries = new HashMap<>();
		queries.put(
				new DoubleObject<>("SELECT `TIMESTAMP`, `DURATION`, "
				+ "`REASON`, `AUTHOR`, `UNREASON`, `UNTIMESTAMP`, `UNAUTHOR`, `SERVER` "
				+ "FROM `log_ban` WHERE `UUID`=? AND `ACTIVE`=TRUE", new Object[] { uuid }),
				rs -> {
					try {
						while(rs.next()) {
							long timestamp = rs.getTimestamp(1).getTime();
							long duration = rs.getLong(2);
							String reason = rs.getString(3);
							UUID author = UUID.fromString(rs.getString(4));
							String unReason = rs.getString(5);
							Timestamp unTimestampObj = rs.getTimestamp(6);
							long unTimestamp = 0;
							if(unTimestampObj != null) unTimestamp = unTimestampObj.getTime();
							String unAuthorStr = rs.getString(7);
							UUID unAuthor = null;
							if(unAuthorStr != null) unAuthor = UUID.fromString(unAuthorStr);
							int server = rs.getInt(8);
							
							Ban ban = new Ban(uuid, timestamp, duration, 
									reason, author, unReason, unTimestamp, unAuthor, server, true);
							punishments.add(ban);
						}
					} catch(SQLException e) {
						e.printStackTrace();
					}
				});
		queries.put(
				new DoubleObject<>("SELECT `TIMESTAMP`, "
				+ "`REASON`, `AUTHOR`, `UNREASON`, `UNTIMESTAMP`, `UNAUTHOR`, `SERVER` "
				+ "FROM `log_blacklist` WHERE `UUID`=? AND `ACTIVE`=TRUE", new Object[] { uuid }),
				rs -> {
					try {
						while(rs.next()) {
							long timestamp = rs.getTimestamp(1).getTime();
							String reason = rs.getString(2);
							UUID author = UUID.fromString(rs.getString(3));
							String unReason = rs.getString(4);
							Timestamp unTimestampObj = rs.getTimestamp(5);
							long unTimestamp = 0;
							if(unTimestampObj != null) unTimestamp = unTimestampObj.getTime();
							String unAuthorStr = rs.getString(6);
							UUID unAuthor = null;
							if(unAuthorStr != null) unAuthor = UUID.fromString(unAuthorStr);
							int server = rs.getInt(7);
							
							Blacklist blacklist = new Blacklist(uuid, timestamp, 
									reason, author, unReason, unTimestamp, unAuthor, server, true);
							punishments.add(blacklist);
						}
					} catch(SQLException e) {
						e.printStackTrace();
					}
				});
		queries.put(
				new DoubleObject<>("SELECT `TIMESTAMP`, `DURATION`, "
				+ "`REASON`, `AUTHOR`, `UNREASON`, `UNTIMESTAMP`, `UNAUTHOR`, `SERVER` "
				+ "FROM `log_mute` WHERE `UUID`=? AND `ACTIVE`=TRUE", new Object[] { uuid }),
				rs -> {
					try {
						while(rs.next()) {
							long timestamp = rs.getTimestamp(1).getTime();
							long duration = rs.getLong(2);
							String reason = rs.getString(3);
							UUID author = UUID.fromString(rs.getString(4));
							String unReason = rs.getString(5);
							Timestamp unTimestampObj = rs.getTimestamp(6);
							long unTimestamp = 0;
							if(unTimestampObj != null) unTimestamp = unTimestampObj.getTime();
							String unAuthorStr = rs.getString(7);
							UUID unAuthor = null;
							if(unAuthorStr != null) unAuthor = UUID.fromString(unAuthorStr);
							int server = rs.getInt(8);
							
							Mute mute = new Mute(uuid, timestamp, duration, 
									reason, author, unReason, unTimestamp, unAuthor, server, true);
							punishments.add(mute);
						}
					} catch(SQLException e) {
						e.printStackTrace();
					}
				});
		
		db.multiQuery(queries);
		
		return punishments;
	}

	@Override
	public Set<Blacklist> getActiveBlacklists(InetAddress ip) {
		Set<Blacklist> blacklists = new HashSet<>();
		db.query(
				"SELECT `UUID`, `TIMESTAMP`, "
				+ "`REASON`, `AUTHOR`, `UNREASON`, `UNTIMESTAMP`, `UNAUTHOR`, `SERVER` "
				+ "FROM `log_blacklist` WHERE `UUID`=ANY(SELECT DISTINCT `UUID` FROM `prev_ips` WHERE `IP`=?) AND `ACTIVE`=TRUE",
				rs -> {
					try {
						while(rs.next()) {
							UUID uuid = UUID.fromString(rs.getString(1));
							long timestamp = rs.getTimestamp(2).getTime();
							String reason = rs.getString(3);
							UUID author = UUID.fromString(rs.getString(4));
							String unReason = rs.getString(5);
							Timestamp unTimestampObj = rs.getTimestamp(6);
							long unTimestamp = 0;
							if(unTimestampObj != null) unTimestamp = unTimestampObj.getTime();
							String unAuthorStr = rs.getString(7);
							UUID unAuthor = null;
							if(unAuthorStr != null) unAuthor = UUID.fromString(unAuthorStr);
							int server = rs.getInt(8);
							
							blacklists.add(new Blacklist(uuid, timestamp, 
									reason, author, unReason, unTimestamp, unAuthor, server, true));
						}
					} catch(SQLException e) {
						e.printStackTrace();
					}
				},
				ip);
		return blacklists;
	}
	

	@Override
	public boolean isPunished(UUID uuid) {
		ObjectMutable<Boolean> punished = new ObjectMutable<>(false);
		Map<DoubleObject<String, Object[]>, Consumer<ResultSet>> queries = new HashMap<>();
		queries.put(
				new DoubleObject<>("SELECT `ACTIVE` FROM `log_ban` WHERE `UUID`=? AND `ACTIVE`=TRUE", 
				new Object[] { uuid }),
				rs -> {
					try {
						if(rs.next())
							punished.set(true);
					} catch(SQLException e) {
						e.printStackTrace();
					}
				});
		queries.put(
				new DoubleObject<>("SELECT `ACTIVE` FROM `log_blacklist` WHERE `UUID`=? AND `ACTIVE`=TRUE", 
				new Object[] { uuid }),
				rs -> {
					try {
						if(rs.next())
							punished.set(true);
					} catch(SQLException e) {
						e.printStackTrace();
					}
				});
		queries.put(
				new DoubleObject<>("SELECT `ACTIVE` FROM `log_mute` WHERE `UUID`=? AND `ACTIVE`=TRUE", 
				new Object[] { uuid }),
				rs -> {
					try {
						if(rs.next())
							punished.set(true);
					} catch(SQLException e) {
						e.printStackTrace();
					}
				});
		db.multiQuery(queries);
		
		return punished.get();
	}

	@Override
	public boolean isBanned(UUID uuid) {
		ObjectMutable<Boolean> banned = new ObjectMutable<>(false);
		db.query(
				"SELECT `ACTIVE` FROM `log_ban` WHERE `UUID`=? AND `ACTIVE`=TRUE", 
				rs -> {
					try {
						if(rs.next())
							banned.set(true);
					} catch(SQLException e) {
						e.printStackTrace();
					}
				},
				uuid);
		return banned.get();
	}

	@Override
	public boolean isBlacklisted(UUID uuid) {
		ObjectMutable<Boolean> blacklisted = new ObjectMutable<>(false);
		db.query(
				"SELECT `ACTIVE` FROM `log_blacklist` WHERE `UUID`=? AND `ACTIVE`=TRUE", 
				rs -> {
					try {
						if(rs.next())
							blacklisted.set(true);
					} catch(SQLException e) {
						e.printStackTrace();
					}
				},
				uuid);
		return blacklisted.get();
	}

	@Override
	public boolean isBlacklisted(InetAddress ip) {
		ObjectMutable<Boolean> blacklisted = new ObjectMutable<>(false);
		db.query(
				"SELECT `ACTIVE` FROM `log_blacklist` WHERE `UUID`=ANY(SELECT DISTINCT `UUID` FROM `prev_ips` WHERE `IP`=?) AND `ACTIVE`=TRUE",
				rs -> {
					try {
						while(rs.next())
							blacklisted.set(true);
					} catch(SQLException e) {
						e.printStackTrace();
					}
				},
				ip
				);
		return blacklisted.get();
	}

	@Override
	public boolean isMuted(UUID uuid) {
		ObjectMutable<Boolean> muted = new ObjectMutable<>(false);
		db.query(
				"SELECT `ACTIVE` FROM `log_mute` WHERE `UUID`=? AND `ACTIVE`=TRUE", 
				rs -> {
					try {
						if(rs.next())
							muted.set(true);
					} catch(SQLException e) {
						e.printStackTrace();
					}
				},
				uuid);
		return muted.get();
	}

	@Override
	public boolean isPermanentlyBanned(UUID uuid) {
		ObjectMutable<Boolean> permanentlyBanned = new ObjectMutable<>(false);
		db.query(
				"SELECT `ACTIVE` FROM `log_ban` WHERE `UUID`=? AND `ACTIVE`=TRUE AND `DURATION`=-1", 
				rs -> {
					try {
						if(rs.next())
							permanentlyBanned.set(true);
					} catch(SQLException e) {
						e.printStackTrace();
					}
				},
				uuid);
		return permanentlyBanned.get();
	}

	@Override
	public boolean isTemporarilyBanned(UUID uuid) {
		ObjectMutable<Boolean> temporarilyBanned = new ObjectMutable<>(false);
		db.query(
				"SELECT `ACTIVE` FROM `log_ban` WHERE `UUID`=? AND `ACTIVE`=TRUE AND `DURATION`>=0", 
				rs -> {
					try {
						if(rs.next())
							temporarilyBanned.set(true);
					} catch(SQLException e) {
						e.printStackTrace();
					}
				},
				uuid);
		return temporarilyBanned.get();
	}

	@Override
	public boolean isPermanentlyMuted(UUID uuid) {
		ObjectMutable<Boolean> permanentlyMuted = new ObjectMutable<>(false);
		db.query(
				"SELECT `ACTIVE` FROM `log_mute` WHERE `UUID`=? AND `ACTIVE`=TRUE AND `DURATION`=-1", 
				rs -> {
					try {
						if(rs.next())
							permanentlyMuted.set(true);
					} catch(SQLException e) {
						e.printStackTrace();
					}
				},
				uuid);
		return permanentlyMuted.get();
	}

	@Override
	public boolean isTemporarilyMuted(UUID uuid) {
		ObjectMutable<Boolean> temporarilyMuted = new ObjectMutable<>(false);
		db.query(
				"SELECT `ACTIVE` FROM `log_mute` WHERE `UUID`=? AND `ACTIVE`=TRUE AND `DURATION`>=0", 
				rs -> {
					try {
						if(rs.next())
							temporarilyMuted.set(true);
					} catch(SQLException e) {
						e.printStackTrace();
					}
				},
				uuid);
		return temporarilyMuted.get();
	}

	@Override
	public Ban executeBan(UUID target, long duration, UUID author, String reason, int server) {
		if(isBanned(target)) return null;
		Ban ban = new Ban(target, System.currentTimeMillis(), duration, reason, author, null, 0, null, server, true);
		db.update("INSERT INTO `log_ban` (`UUID`, `TIMESTAMP`, `DURATION`, `REASON`, `AUTHOR`, `SERVER`, `ACTIVE`) VALUES (?, ?, ?, ?, ?, ?, ?)", target, new Timestamp(ban.getTimestamp()), duration, reason, author, server, true);
		addToCache(target, ban);
		return ban;
	}

	@Override
	public Ban executeUnBan(Ban ban, String unReason, UUID unAuthor) {
		if(!ban.isActive()) return ban;
		ban.setActive(false);
		ban.setUnReason(unReason);
		ban.setUnAuthor(unAuthor);
		ban.setUnTimestamp(System.currentTimeMillis());
		db.update("UPDATE `log_ban` SET `ACTIVE`=?, `UNREASON`=?, `UNAUTHOR`=?, `UNTIMESTAMP`=? WHERE `UUID`=? AND `ACTIVE`=TRUE", false, unReason, unAuthor, new Timestamp(ban.getUnTimestamp()), ban.getUniqueId());
		removeFromBanCache(ban.getUniqueId());
		return ban;
	}

	@Override
	public Blacklist executeBlacklist(UUID target, UUID author, String reason, int server) {
		if(isBlacklisted(target)) return null;
		Blacklist blacklist = new Blacklist(target, System.currentTimeMillis(), reason, author, null, 0, null, server, true);
		db.update("INSERT INTO `log_blacklist` (`UUID`, `TIMESTAMP`, `REASON`, `AUTHOR`, `SERVER`, `ACTIVE`) VALUES (?, ?, ?, ?, ?, ?)", target, new Timestamp(blacklist.getTimestamp()), reason, author, server, true);
		return blacklist;
	}

	@Override
	public Blacklist executeUnBlacklist(Blacklist blacklist, String unReason, UUID unAuthor) {
		if(!blacklist.isActive()) return blacklist;
		blacklist.setActive(false);
		blacklist.setUnReason(unReason);
		blacklist.setUnAuthor(unAuthor);
		blacklist.setUnTimestamp(System.currentTimeMillis());
		db.update("UPDATE `log_blacklist` SET `ACTIVE`=?, `UNREASON`=?, `UNAUTHOR`=?, `UNTIMESTAMP`=? WHERE `UUID`=? AND `ACTIVE`=TRUE", false, unReason, unAuthor, new Timestamp(blacklist.getUnTimestamp()), blacklist.getUniqueId());
		return blacklist;
	}

	@Override
	public Mute executeMute(UUID target, long duration, UUID author, String reason, int server) {
		if(isMuted(target)) return null;
		Mute mute = new Mute(target, System.currentTimeMillis(), duration, reason, author, null, 0, null, server, true);
		db.update("INSERT INTO `log_mute` (`UUID`, `TIMESTAMP`, `DURATION`, `REASON`, `AUTHOR`, `SERVER`, `ACTIVE`) VALUES (?, ?, ?, ?, ?, ?, ?)", target, new Timestamp(mute.getTimestamp()), duration, reason, author, server, true);
		addToCache(target, mute);
		return mute;
	}

	@Override
	public Mute executeUnMute(Mute mute, String unReason, UUID unAuthor) {
		if(!mute.isActive()) return mute;
		mute.setActive(false);
		mute.setUnReason(unReason);
		mute.setUnAuthor(unAuthor);
		mute.setUnTimestamp(System.currentTimeMillis());
		db.update("UPDATE `log_mute` SET `ACTIVE`=?, `UNREASON`=?, `UNAUTHOR`=?, `UNTIMESTAMP`=? WHERE `UUID`=? AND `ACTIVE`=TRUE", false, unReason, unAuthor, new Timestamp(mute.getUnTimestamp()), mute.getUniqueId());
		removeFromMuteCache(mute.getUniqueId());
		return mute;
	}

	@Override
	public Kick executeKick(UUID target, UUID author, String reason, int server) {
		Kick kick = new Kick(target, System.currentTimeMillis(), reason, author, server);
		db.update("INSERT INTO `log_kick` (`UUID`, `TIMESTAMP`, `REASON`, `AUTHOR`, `SERVER`) VALUES (?, ?, ?, ?, ?)", target, new Timestamp(kick.getTimestamp()), reason, author, server);
		return kick;
	}
	
}
