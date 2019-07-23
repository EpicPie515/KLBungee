package lol.kangaroo.bungee.database;

import java.security.GeneralSecurityException;
import java.sql.SQLException;

import com.j256.twofactorauth.TimeBasedOneTimePasswordUtil;

import lol.kangaroo.common.database.DatabaseManager;
import lol.kangaroo.common.player.BasePlayer;

public class Auth {

	public static DatabaseManager db;
	
	public static void init(DatabaseManager dbm) {
		db = dbm;
	}
	
	private static String getSecret(BasePlayer bp) {
		String secret = "";
		db.query("SELECT `SECRET` FROM `auth_secrets` WHERE `UUID`=?", rs -> {
			try {
				if(rs.next()) {
					secret.concat(rs.getString("SECRET"));
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}, bp.getUniqueId());
		return secret;
	}
	
	public static boolean validate(BasePlayer bp, int otpCode, int millisBuffer) {
		String secret = getSecret(bp);
		if(secret == "") return false;
		try {
			return TimeBasedOneTimePasswordUtil.validateCurrentNumber(secret, otpCode, millisBuffer);
		} catch (GeneralSecurityException e) {
			e.printStackTrace();
			return false;
		}
	}
	
	public static String generateAndAssignSecret(BasePlayer bp) {
		String secret = TimeBasedOneTimePasswordUtil.generateBase32Secret();
		db.update("INSERT INTO `auth_secrets` (`UUID`, `SECRET`) VALUES (?, ?) ON DUPLICATE KEY UPDATE `SECRET`=VALUES(`SECRET`);", bp.getUniqueId(), secret);
		return secret;
	}
	
}
