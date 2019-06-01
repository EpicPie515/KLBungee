package lol.kangaroo.bungee.listeners;

import com.vexsoftware.votifier.bungee.events.VotifierEvent;
import com.vexsoftware.votifier.model.Vote;

import lol.kangaroo.bungee.player.PlayerManager;
import lol.kangaroo.bungee.util.Message;
import lol.kangaroo.common.player.CachedPlayer;
import lol.kangaroo.common.util.MSG;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class BungeeVoteListener implements Listener {
	
	private PlayerManager pm;
	
	public BungeeVoteListener(PlayerManager pm) {
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
		// TODO XP and Money and streaks and stuff
		Message.sendMessage(cp, MSG.VOTE_RECEIVED, v.getServiceName());
	}
	
}
