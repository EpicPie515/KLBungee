package lol.kangaroo.bungee.player;

import java.sql.Timestamp;

import lol.kangaroo.common.player.BasePlayer;
import lol.kangaroo.common.player.CachedPlayer;
import lol.kangaroo.common.player.PlayerUpdateCache;
import lol.kangaroo.common.player.PlayerVariable;

public class PlayerVoteStreak {
	
	public static int getStreak(BasePlayer p) {
		// TODO debug
		if(p == null) System.out.println("WHAT THE FUCK");
		return (int) p.getVariable(PlayerVariable.VOTE_STREAK);
	}
	
	public static Timestamp getLastVote(BasePlayer p) {
		return (Timestamp) p.getVariable(PlayerVariable.VOTE_LAST);
	}
	
	public static void setStreak(CachedPlayer cp, int streak) {
		PlayerUpdateCache u = cp.createUpdateCache();
		cp.setVariableInUpdate(u, PlayerVariable.VOTE_STREAK, streak);
		u.pushUpdates();
	}
	
	public static void setLastVote(CachedPlayer cp, long lastVote) {
		PlayerUpdateCache u = cp.createUpdateCache();
		cp.setVariableInUpdate(u, PlayerVariable.VOTE_LAST, new Timestamp(lastVote));
		u.pushUpdates();
	}
	
	public static void setStreakAndVote(CachedPlayer cp, int streak, long lastVote) {
		PlayerUpdateCache u = cp.createUpdateCache();
		cp.setVariableInUpdate(u, PlayerVariable.VOTE_STREAK, streak);
		cp.setVariableInUpdate(u, PlayerVariable.VOTE_LAST, new Timestamp(lastVote));
		u.pushUpdates();
	}
}
