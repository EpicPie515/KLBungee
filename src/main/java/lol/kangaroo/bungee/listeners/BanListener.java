package lol.kangaroo.bungee.listeners;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Locale;
import java.util.UUID;

import lol.kangaroo.bungee.KLBungeePlugin;
import lol.kangaroo.bungee.player.PlayerManager;
import lol.kangaroo.bungee.player.punish.PunishManager;
import lol.kangaroo.common.player.CachedPlayer;
import lol.kangaroo.common.player.PlayerVariable;
import lol.kangaroo.common.player.punish.Ban;
import lol.kangaroo.common.player.punish.Punishment;
import lol.kangaroo.common.util.MSG;
import net.md_5.bungee.api.event.LoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

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
	 */
	@EventHandler
	public void onLogin(LoginEvent e) {
		UUID uuid = e.getConnection().getUniqueId();
		e.registerIntent(pl);
		pl.getProxy().getScheduler().runAsync(pl, () -> {
			if(pum.isBanned(uuid)) {
				e.setCancelled(true);
				Ban ban = null;
				for(Punishment pun : pum.getActivePunishments(uuid))
					if(pun instanceof Ban) ban = (Ban) pun;
				if(ban == null) return;
				if(ban.getDuration() != -1 && ban.getTimestamp() + ban.getDuration() < System.currentTimeMillis()) {
					pum.executeUnBan(ban, "Ban Expired", PunishManager.ZERO_UUID);
					e.setCancelled(false);
					e.completeIntent(pl);
					return;
				}
				
				CachedPlayer p = pm.getCachedPlayer(ban.getUniqueId());
				CachedPlayer author = pm.getCachedPlayer(ban.getAuthor());
				// NICKNAME should only be used for at-the-moment things, such as chat
				// but not for this because someone could use it to detect the real name of the nicked person by checking it when they arent nicked then again when they are.
				String authorName = author != null ? (pl.getRankManager().getRank(author, false).getColor() + (String) author.getVariable(PlayerVariable.USERNAME)) : MSG.CONSOLE.getMessage(Locale.getDefault());
				String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("d MMM uuuu"));
				String durStr = MSG.TIMEFORMAT_PERMANENT.getMessage(p);
				String timeLeftStr = MSG.BANNED_TIMEPERMANENT.getMessage(p);
				if(ban.getDuration() != -1) {
					Duration dur = Duration.ofMillis(ban.getDuration());
					long years = dur.get(ChronoUnit.YEARS); dur.minus(years, ChronoUnit.YEARS);
					long days = dur.get(ChronoUnit.DAYS); dur.minusDays(days);
					long hours = dur.get(ChronoUnit.HOURS); dur.minusHours(hours);
					long minutes = dur.get(ChronoUnit.MINUTES);
					durStr = (years > 0 ? years + MSG.TIMEFORMAT_YEARS.getMessage(p) + ", " : "")
							+ (days > 0 ? days + MSG.TIMEFORMAT_DAYS.getMessage(p) + ", " : "")
							+ (hours > 0 ? hours + MSG.TIMEFORMAT_HOURS.getMessage(p) + ", " : "")
							+ minutes + MSG.TIMEFORMAT_MINUTES.getMessage(p);
					Duration tlDur = Duration.ofMillis((ban.getTimestamp() + ban.getDuration()) - System.currentTimeMillis());
					long tlyears = tlDur.get(ChronoUnit.YEARS); tlDur.minus(years, ChronoUnit.YEARS);
					long tldays = tlDur.get(ChronoUnit.DAYS); tlDur.minusDays(days);
					long tlhours = tlDur.get(ChronoUnit.HOURS); tlDur.minusHours(hours);
					long tlminutes = tlDur.get(ChronoUnit.MINUTES);
					String timeLeft = (tlyears > 0 ? tlyears + MSG.TIMEFORMAT_YEARS.getMessage(p) + ", " : "")
							+ (tldays > 0 ? tldays + MSG.TIMEFORMAT_DAYS.getMessage(p) + ", " : "")
							+ (tlhours > 0 ? tlhours + MSG.TIMEFORMAT_HOURS.getMessage(p) + ", " : "")
							+ tlminutes + MSG.TIMEFORMAT_MINUTES.getMessage(p);
					timeLeftStr = MSG.BANNED_TIMEREMAINING.getMessage(p, timeLeft);
				}
				String bm = MSG.KICKMESSAGE_BAN.getMessage(p, MSG.BANSCREEN_LINE.getMessage(p), MSG.PUNISHMESSAGE_HAVEBEEN.getMessage(p), authorName, date, durStr, ban.getReason(), timeLeftStr, MSG.APPEAL_URL.getMessage(p), MSG.BANSCREEN_LINE.getMessage(p));
				e.setCancelReason(bm);
			}
			e.completeIntent(pl);
		});
	}
	
}
