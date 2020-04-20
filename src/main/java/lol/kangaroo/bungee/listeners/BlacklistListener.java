package lol.kangaroo.bungee.listeners;

import java.net.InetAddress;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Iterator;
import java.util.Locale;
import java.util.UUID;

import lol.kangaroo.bungee.KLBungeePlugin;
import lol.kangaroo.bungee.player.PlayerManager;
import lol.kangaroo.common.player.CachedPlayer;
import lol.kangaroo.common.player.PlayerVariable;
import lol.kangaroo.common.player.punish.Blacklist;
import lol.kangaroo.common.player.punish.PunishManager;
import lol.kangaroo.common.player.punish.Punishment;
import lol.kangaroo.common.util.MSG;
import net.md_5.bungee.api.event.LoginEvent;
import net.md_5.bungee.api.event.PreLoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class BlacklistListener implements Listener {
	
	private PlayerManager pm;
	private KLBungeePlugin pl;
	private PunishManager pum;
	
	public BlacklistListener(KLBungeePlugin pl) {
		this.pl = pl;
		this.pm = pl.getPlayerManager();
		this.pum = pl.getPunishManager();
	}
	
	/**
	 * Blocks similar IPs from joining.
	 */
	@EventHandler
	public void onPreLogin(PreLoginEvent e) {
		InetAddress ip = e.getConnection().getAddress().getAddress();
		if(pum.isBlacklisted(ip)) {
			e.setCancelled(true);
			Iterator<Blacklist> i = pum.getActiveBlacklists(ip).iterator();
			if(i.hasNext()) {
				Blacklist bl = i.next();
				CachedPlayer cp = pm.getCachedPlayer(bl.getUniqueId());
				CachedPlayer author = pm.getCachedPlayer(bl.getAuthor());
				// NICKNAME should only be used for at-the-moment things, such as chat
				// but not for this because someone could use it to detect the real name of the nicked person by checking it when they arent nicked then again when they are.
				String authorName = author != null ? (pl.getRankManager().getRank(author, false).getColor() + (String) author.getVariable(PlayerVariable.USERNAME)) : MSG.CONSOLE.getMessage(Locale.getDefault());
				String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("d MMM uuuu"));
				String bm = MSG.KICKMESSAGE_BLACKLIST.getMessage(cp, MSG.BANSCREEN_LINE.getMessage(cp), MSG.PUNISHMESSAGE_ARE.getMessage(cp), authorName, date, bl.getReason(), MSG.BANSCREEN_LINE.getMessage(cp));
				e.setCancelReason(bm);

				System.out.println("done");
			}
		}
	}
	

	
	/**
	 * Blocks direct user from joining, in case IP changed.
	 * 
	 * Not a handler, called by @LoginListener
	 */
	public void onLogin(LoginEvent e) {
		UUID uuid = e.getConnection().getUniqueId();
		if(pum.isBlacklisted(uuid)) {
			e.setCancelled(true);
			Blacklist bl = null;
			for(Punishment pun : pum.getActivePunishments(uuid))
				if(pun instanceof Blacklist) bl = (Blacklist) pun;
			if(bl == null) return;
			
			CachedPlayer cp = pm.getCachedPlayer(bl.getUniqueId());
			CachedPlayer author = pm.getCachedPlayer(bl.getAuthor());
			String authorName = pl.getRankManager().getPrefix(author, false) + (String) author.getVariable(PlayerVariable.NICKNAME);
			String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("d MMM uuuu"));
			String bm = MSG.KICKMESSAGE_BLACKLIST.getMessage(cp, MSG.BANSCREEN_LINE.getMessage(cp), MSG.PUNISHMESSAGE_ARE.getMessage(cp), authorName, date, bl.getReason(), MSG.BANSCREEN_LINE.getMessage(cp));
			e.setCancelReason(bm);
		}
	}
	
}
