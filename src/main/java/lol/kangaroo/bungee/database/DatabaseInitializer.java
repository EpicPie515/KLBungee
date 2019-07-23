package lol.kangaroo.bungee.database;

import lol.kangaroo.common.database.DatabaseManager;

public class DatabaseInitializer {

	private DatabaseManager db;
	
	public DatabaseInitializer(DatabaseManager db) {
		this.db = db;
	}
	
	public void createPlayerTables() {
		
		/** USERS - Main Player Locator Table
		 *  Primary Key - (UUID)
		 */
		db.update("CREATE TABLE IF NOT EXISTS `users` "
				+ "(`UUID` CHAR(36), PRIMARY KEY (`UUID`));");
		
		/** CUR_DATA - Latest Usernames and IPs
		 *  Primary Key - (UUID)
		 */
		db.update("CREATE TABLE IF NOT EXISTS `cur_data` "
				+ "(`UUID` CHAR(36), `NAME` VARCHAR(16), `IP` VARCHAR(15), "
				+ "PRIMARY KEY (`UUID`));");
		
		/** PREV_NAMES - Previous-Known Usernames
		 *  Primary Key - (UUID + Name + FirstSeen)
		 */
		db.update("CREATE TABLE IF NOT EXISTS `prev_names` "
				+ "(`UUID` CHAR(36), `NAME` VARCHAR(16), `INITIAL` TIMESTAMP, "
				+ "PRIMARY KEY (`UUID`, `NAME`, `INITIAL`));");
		
		/** PREV_IPS - Previous-Known IPs
		 *  Primary Key - (UUID + Name)
		 */
		db.update("CREATE TABLE IF NOT EXISTS `prev_ips` "
				+ "(`UUID` CHAR(36), `IP` VARCHAR(15), `INITIAL` TIMESTAMP, "
				+ "PRIMARY KEY (`UUID`, `IP`, `INITIAL`));");
		
		/** PREV_NICKNAMES - Previous-Known Nicknames
		 *  Primary Key - (UUID + Nickname + FirstUsed)
		 */
		db.update("CREATE TABLE IF NOT EXISTS `prev_nicknames` "
				+ "(`UUID` CHAR(36), `NICKNAME` VARCHAR(16), `INITIAL` TIMESTAMP, "
				+ "PRIMARY KEY (`UUID`, `NICKNAME`, `INITIAL`));");
		
		/** JOIN_DATA - Players' First Join and Last Seen
		 *  Primary Key - (UUID)
		 */
		db.update("CREATE TABLE IF NOT EXISTS `join_data` "
				+ "(`UUID` CHAR(36), `FIRSTJOIN` TIMESTAMP, `LASTQUIT` TIMESTAMP, "
				+ "`LASTJOIN` TIMESTAMP, PRIMARY KEY (`UUID`));");
		
		/** PLAYER_DATA - General Values (Money, XP, etc.)
		 *  Primary Key - (UUID + DATA)
		 */
		db.update("CREATE TABLE IF NOT EXISTS `player_data` "
				+ "(`UUID` CHAR(36), `TYPE` VARCHAR(32), `VALUE` VARCHAR(255), "
				+ "PRIMARY KEY (`UUID`, `TYPE`));");
		
		/** RANK_PERMS - External plugin permissions for ranks.
		 *  Primary Key - (RANK + PERM)
		 */
		db.update("CREATE TABLE IF NOT EXISTS `rank_perms` "
				+ "(`RANK` VARCHAR(255), `PERM` VARCHAR(255), `VALUE` BOOLEAN, "
				+ "PRIMARY KEY (`RANK`, `PERM`));");
		
		/** PLAYER_PERMS - External plugin permissions for a player.
		 *  Primary Key - (UUID + PERM)
		 */
		db.update("CREATE TABLE IF NOT EXISTS `player_perms` "
				+ "(`UUID` CHAR(36), `PERM` VARCHAR(255), `VALUE` BOOLEAN, "
				+ "PRIMARY KEY (`UUID`, `PERM`));");
		
		/** NETWORK_MONEY - Network-Wide Money
		 *  Primary Key - (UUID)
		 */
		db.update("CREATE TABLE IF NOT EXISTS `network_money` "
				+ "(`UUID` CHAR(36), `BALANCE` DECIMAL, PRIMARY KEY (`UUID`));");
		
		/** AUTH_SECRETS - Authenticator Secrets
		 *  Primary Key - (UUID)
		 */
		db.update("CREATE TABLE IF NOT EXISTS `auth_secrets` "
				+ "(`UUID` CHAR(36), `SECRET` VARCHAR(255), PRIMARY KEY (`UUID`));");
	}
	
	public void createLogTables() {
		
		/** LOG_JOIN - Join/Leave Logs
		 *  No Key
		 *  Action - J(oin), L(eave), C(onnection Lost) or K(ick)
		 */
		db.update("CREATE TABLE IF NOT EXISTS `log_join` "
				+ "(`UUID` CHAR(36), `IP` VARCHAR(15), `TIMESTAMP` TIMESTAMP, `ACTION` CHAR(1));");

		/** LOG_BAN - Ban Logs
		 *  No Key
		 */
		db.update("CREATE TABLE IF NOT EXISTS `log_ban` "
				+ "(`UUID` CHAR(36), `TIMESTAMP` TIMESTAMP, `DURATION` BIGINT, "
				+ "`REASON` VARCHAR(255), `AUTHOR` CHAR(36), `UNREASON` VARCHAR(255) DEFAULT NULL, "
				+ "`UNTIMESTAMP` TIMESTAMP NULL DEFAULT NULL, `UNAUTHOR` CHAR(36) DEFAULT NULL, `SERVER` INT, `ACTIVE` BOOLEAN);");

		/** LOG_BLACKLIST - Blacklist Logs
		 *  No Key
		 */
		db.update("CREATE TABLE IF NOT EXISTS `log_blacklist` "
				+ "(`UUID` CHAR(36), `TIMESTAMP` TIMESTAMP, "
				+ "`REASON` VARCHAR(255), `AUTHOR` CHAR(36), `UNREASON` VARCHAR(255) DEFAULT NULL, "
				+ "`UNTIMESTAMP` TIMESTAMP NULL DEFAULT NULL, `UNAUTHOR` CHAR(36) DEFAULT NULL, `SERVER` INT, `ACTIVE` BOOLEAN);");

		/** LOG_KICK - Kick Logs
		 * 	No Key
		 */
		db.update("CREATE TABLE IF NOT EXISTS `log_kick` "
				+ "(`UUID` CHAR(36), `TIMESTAMP` TIMESTAMP, `REASON` VARCHAR(255), "
				+ "`AUTHOR` CHAR(36), `SERVER` INT)");
		
		/** LOG_VOTE - Vote Logs
		 * 	No Key
		 */
		db.update("CREATE TABLE IF NOT EXISTS `log_vote` "
				+ "(`USERNAME` VARCHAR(16), `UUID` CHAR(36) DEFAULT NULL, `TIMESTAMP` TIMESTAMP, `SERVICENAME` VARCHAR(255), "
				+ "`NEWSTREAK` INT)");
		
		/** LOG_MUTE - Mute Logs
		 * 	No Key
		 */
		db.update("CREATE TABLE IF NOT EXISTS `log_mute` "
				+ "(`UUID` CHAR(36), `TIMESTAMP` TIMESTAMP, `DURATION` BIGINT, "
				+ "`REASON` VARCHAR(255), AUTHOR CHAR(36), `UNREASON` VARCHAR(255) DEFAULT NULL, "
				+ "`UNTIMESTAMP` TIMESTAMP NULL DEFAULT NULL, `UNAUTHOR` CHAR(36) DEFAULT NULL, `SERVER` INT, `ACTIVE` BOOLEAN);");
		
		/** LOG_GRANT - Grant Logs
		 * 	No Key
		 */
		db.update("CREATE TABLE IF NOT EXISTS `log_grant` "
				+ "(`UUID` CHAR(36), `TIMESTAMP` TIMESTAMP, `AUTHOR` CHAR(36), "
				+ "`ACTION` CHAR(1), `TYPE` CHAR(1), `TYPEVALUE` VARCHAR(255), `NOTE` VARCHAR(255), "
				+ "`PERMVALUE` BOOLEAN NULL DEFAULT NULL)");
	
		/** SETTINGS - InGame-Controlled Settings
		 *  Primary Key - (SETTING)
		 */
		db.update("CREATE TABLE IF NOT EXISTS `settings` "
				+ "(`SETTING` VARCHAR(128), `VALUE` VARCHAR(255),"
				+ "PRIMARY KEY(`SETTING`))");
	}
	
}
