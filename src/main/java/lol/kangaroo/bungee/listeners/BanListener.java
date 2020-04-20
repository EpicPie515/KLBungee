package lol.kangaroo.bungee.listeners;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.UUID;

import lol.kangaroo.bungee.KLBungeePlugin;
import lol.kangaroo.bungee.player.PlayerManager;
import lol.kangaroo.bungee.util.Message;
import lol.kangaroo.common.player.CachedPlayer;
import lol.kangaroo.common.player.PlayerVariable;
import lol.kangaroo.common.player.punish.Ban;
import lol.kangaroo.common.player.punish.PunishManager;
import lol.kangaroo.common.player.punish.Punishment;
import lol.kangaroo.common.util.DurationFormat;
import lol.kangaroo.common.util.I18N;
import lol.kangaroo.common.util.MSG;
import net.md_5.bungee.api.event.LoginEvent;
import net.md_5.bungee.api.plugin.Listener;

public class BanListener implements Listener {

	private PlayerManager pm;
	private KLBungeePlugin pl;
	private PunishManager pum;
	
	public BanListener(KLBungeePlugin pl) {
		this.pl = pl;
		this.pm = pl.getPlayerManager();
		this.pum = pl.getPunishManager();
	}
	
	/**
	 * Blocks direct user from joining, in case IP changed.
	 * TODO no idea why this is here, IP isn't checked on normal bans anywhere
	 * 
	 * Not a handler, called by @LoginListener
	 */
	public void onLogin(LoginEvent e) {
		UUID uuid = e.getConnection().getUniqueId();
		if(pum.isBanned(uuid)) {
			e.setCancelled(true);
			Ban ban = null;
			for(Punishment pun : pum.getActivePunishments(uuid))
				if(pun instanceof Ban) ban = (Ban) pun;
			if(ban == null) return;
			CachedPlayer p = pm.getCachedPlayer(ban.getUniqueId());
			Locale lang = I18N.getPlayerLocale(p);
			if(ban.getDuration() != -1 && ban.getTimestamp() + ban.getDuration() < System.currentTimeMillis()) {
				pum.executeUnBan(ban, "Ban Expired", PunishManager.ZERO_UUID);
				e.setCancelled(false);
				e.completeIntent(pl);
				Message.broadcast(pm.getNotifiableStaff(), MSG.ADMIN_UNBANEXPIREALERT, pm.getRankManager().getPrefix(p, false) + p.getVariable(PlayerVariable.USERNAME));
				pm.unbannedJoining.add(uuid);
				return;
			}

			CachedPlayer author = pm.getCachedPlayer(ban.getAuthor());
			// NICKNAME should only be used for at-the-moment things, such as chat
			// but not for this because someone could use it to detect the real name of the nicked person by checking it when they arent nicked then again when they are.
			String authorName = author != null ? (pl.getRankManager().getRank(author, false).getColor() + (String) author.getVariable(PlayerVariable.USERNAME)) : MSG.CONSOLE.getMessage(Locale.getDefault());
			String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("d MMM uuuu"));
			String durStr = MSG.TIMEFORMAT_PERMANENT.getMessage(p);
			String timeLeftStr = MSG.BANNED_TIMEPERMANENT.getMessage(p);
			if(ban.getDuration() != -1) {
				Duration dur = Duration.ofMillis(ban.getDuration());
				durStr = DurationFormat.getFormatted1UnitDuration(dur, lang, false);
				Duration tlDur = Duration.ofMillis((ban.getTimestamp() + ban.getDuration()) - System.currentTimeMillis());
				String tlraw = DurationFormat.getFormatted1UnitDuration(tlDur, lang, false);
				timeLeftStr = MSG.BANNED_TIMEREMAINING.getMessage(p, tlraw);
			}
			String bm = MSG.KICKMESSAGE_BAN.getMessage(p, MSG.BANSCREEN_LINE.getMessage(p), MSG.PUNISHMESSAGE_ARE.getMessage(p), authorName, date, durStr, ban.getReason(), timeLeftStr, MSG.APPEAL_URL.getMessage(p), MSG.BANSCREEN_LINE.getMessage(p));
			e.setCancelReason(bm);
		}
		
	}
	
}
