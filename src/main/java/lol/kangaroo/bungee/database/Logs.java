package lol.kangaroo.bungee.database;

import java.sql.Timestamp;
import java.util.UUID;

import lol.kangaroo.common.database.DatabaseManager;

public class Logs {
	
	public static DatabaseManager db;
	
	public static void init(DatabaseManager dbm) {
		db = dbm;
	}
	
	public static class Vote {
		
		public static void addLog(String username, UUID uuid, long timestamp, String serviceName, int newStreak) {
			db.update("INSERT INTO `log_vote` (`USERNAME`, `UUID`, `TIMESTAMP`, `SERVICENAME`, `NEWSTREAK`) VALUES (?, ?, ?, ?, ?)", username, uuid, new Timestamp(timestamp), serviceName, newStreak);
		}
		
	}
	
	public static class Grant {
		
		public static void addLog(UUID uuid, long timestamp, UUID author, char action, byte type, String typeValue, String note, Boolean permValue) {
			db.update("INSERT INTO `log_grant` (`UUID`, `TIMESTAMP`, `AUTHOR`, `ACTION`, `TYPE`, `TYPEVALUE`, `NOTE`, `PERMVALUE`) "
					+ "VALUES (?, ?, ?, ?, ?, ?, ?, ?)", uuid, new Timestamp(timestamp), author,
					action + "", type == 1 ? "r" : "p", typeValue, note, permValue);
		}
		
	}
	
}
