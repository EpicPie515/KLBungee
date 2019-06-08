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
	
}
