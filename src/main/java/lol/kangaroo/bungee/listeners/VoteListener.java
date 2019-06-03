package lol.kangaroo.bungee.listeners;

import java.sql.Timestamp;

import com.vexsoftware.votifier.bungee.events.VotifierEvent;
import com.vexsoftware.votifier.model.Vote;

import lol.kangaroo.bungee.player.Money;
import lol.kangaroo.bungee.player.PlayerLevel;
import lol.kangaroo.bungee.player.PlayerManager;
import lol.kangaroo.bungee.player.PlayerVoteStreak;
import lol.kangaroo.bungee.util.Message;
import lol.kangaroo.common.player.CachedPlayer;
import lol.kangaroo.common.util.MSG;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class VoteListener implements Listener {
	
	private PlayerManager pm;
	
	public VoteListener(PlayerManager pm) {
		this.pm = pm;
	}
	
	@EventHandler
	public void onVote(VotifierEvent e) {
		Vote v = e.getVote();
		CachedPlayer cp = pm.getCachedPlayer(pm.getFromCurrent(v.getUsername()));
		if(cp == null) {
			System.out.println("Received vote of non-player: " + v.getUsername());
			return;
		}
		int streak = PlayerVoteStreak.getStreak(cp);
		int effectiveStreak = Math.min(streak, 250);
		Timestamp ls = PlayerVoteStreak.getLastVote(cp);
		Timestamp now = new Timestamp(System.currentTimeMillis());
		if(ls.toLocalDateTime().isAfter(now.toLocalDateTime().plusHours(48)))
			streak = 0;
		PlayerVoteStreak.setStreak(cp, streak + 1);
		PlayerVoteStreak.setLastVote(cp, now.getTime());
		Message.sendMessage(cp, MSG.VOTE_RECEIVED, v.getServiceName(), streak);
		int addTo;
		if(effectiveStreak < 20) addTo = effectiveStreak * 20;
		else addTo = (int) Math.pow(effectiveStreak, 2);
		Money.addToBalanceAndMessage(cp, true, addTo);
		PlayerLevel.addExperience(cp, addTo, true, true);
	}
	
}
