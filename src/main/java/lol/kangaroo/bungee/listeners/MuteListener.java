package lol.kangaroo.bungee.listeners;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Set;

import lol.kangaroo.bungee.KLBungeePlugin;
import lol.kangaroo.bungee.player.PlayerManager;
import lol.kangaroo.bungee.util.Message;
import lol.kangaroo.common.player.BasePlayer;
import lol.kangaroo.common.player.CachedPlayer;
import lol.kangaroo.common.player.PlayerVariable;
import lol.kangaroo.common.player.punish.Mute;
import lol.kangaroo.common.player.punish.PunishManager;
import lol.kangaroo.common.player.punish.Punishment;
import lol.kangaroo.common.util.DurationFormat;
import lol.kangaroo.common.util.I18N;
import lol.kangaroo.common.util.MSG;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ChatEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class MuteListener implements Listener {

	private PlayerManager pm;
	private KLBungeePlugin pl;
	private PunishManager pum;
	
	public MuteListener(KLBungeePlugin pl) {
		this.pl = pl;
		this.pm = pl.getPlayerManager();
		this.pum = pl.getPunishManager();
	}
	
	/**
	 * Blocks any chats on the network.
	 * TODO do the sync thing for ban listener too, also admin ban expire alert
	 */
	@EventHandler
	public void onChat(ChatEvent e) {
		if(!(e.getSender() instanceof ProxiedPlayer)) return;
		if(e.isCommand()) {
			boolean blocked = false;
			for(String s : pl.getConfigManager().getConfig("settings").getStringList("muted-blocked-commands")) {
				if(e.getMessage().substring(1).equalsIgnoreCase(s)) blocked = true;
				else if(e.getMessage().substring(e.getMessage().indexOf(":")+1).equalsIgnoreCase(s)) blocked = true;
				// ^^ above is for namespaced commands (e.g /minecraft:tell instead of /tell)
			}
			if(!blocked) return;
		}
		ProxiedPlayer pp = (ProxiedPlayer) e.getSender();
		if(pum.isInMuteCache(pp.getUniqueId())) {
			e.setCancelled(true);
			pl.getProxy().getScheduler().runAsync(pl, () -> {
				Mute mute = null;
				for(Punishment pun : pum.getActivePunishments(pp.getUniqueId()))
					if(pun instanceof Mute) mute = (Mute) pun;
				if(mute == null) return;
				if(mute.getDuration() != -1 && mute.getTimestamp() + mute.getDuration() < System.currentTimeMillis()) {
					CachedPlayer p = pm.getCachedPlayer(pp.getUniqueId());
					pum.executeUnMute(mute, "Mute Expired", PunishManager.ZERO_UUID);
					Set<BasePlayer> staff = pm.getNotifiableStaff();
					Message.broadcast(staff, MSG.ADMIN_UNMUTEEXPIREALERT, pm.getRankManager().getPrefix(p, false) + p.getVariable(PlayerVariable.USERNAME));
					Message.sendMessage(p, MSG.UNMUTEMESSAGE_EXPIRED);
					return;
				}
				
				CachedPlayer p = pm.getCachedPlayer(mute.getUniqueId());
				Locale lang = I18N.getPlayerLocale(p);
				CachedPlayer author = pm.getCachedPlayer(mute.getAuthor());
				// No prefixes to reduce chat clutter.
				// NICKNAME should only be used for at-the-moment things, such as chat
				// but not for this because someone could use it to detect the real name of the nicked person by checking it when they arent nicked then again when they are.
				String authorName = author != null ? (pl.getRankManager().getRank(author, false).getColor() + (String) author.getVariable(PlayerVariable.USERNAME)) : MSG.CONSOLE.getMessage(Locale.getDefault());
				String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("d MMM uuuu"));
				String durStr = MSG.TIMEFORMAT_PERMANENT.getMessage(p);
				String timeLeftStr = MSG.BANNED_TIMEPERMANENT.getMessage(p);
				if(mute.getDuration() != -1) {
					Duration dur = Duration.ofMillis(mute.getDuration());
					durStr = DurationFormat.getFormatted1UnitDuration(dur, lang, false);
					Duration tlDur = Duration.ofMillis((mute.getTimestamp() + mute.getDuration()) - System.currentTimeMillis());
					timeLeftStr = DurationFormat.getFormatted1UnitDuration(tlDur, lang, false);
				}
				if(mute.getDuration() != -1)
					Message.sendMessage(p, MSG.MUTEMESSAGE_TEMPORARY, MSG.PUNISHMESSAGE_ARE.getMessage(p), durStr, authorName, mute.getReason(), date, timeLeftStr, MSG.APPEAL_URL.getMessage(p));
				else
					Message.sendMessage(p, MSG.MUTEMESSAGE_PERMANENT, MSG.PUNISHMESSAGE_ARE.getMessage(p), authorName, mute.getReason(), date, MSG.APPEAL_URL.getMessage(p));
			});
		}
	}
	
}
