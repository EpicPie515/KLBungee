package lol.kangaroo.bungee.commands.admin;

import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Set;

import de.myzelyam.api.vanish.VanishAPI;
import lol.kangaroo.bungee.commands.AdminCommand;
import lol.kangaroo.bungee.commands.Subcommand;
import lol.kangaroo.bungee.permissions.RankManager;
import lol.kangaroo.bungee.player.Money;
import lol.kangaroo.bungee.player.PlayerLevel;
import lol.kangaroo.bungee.player.PlayerManager;
import lol.kangaroo.bungee.player.PlayerVoteStreak;
import lol.kangaroo.bungee.player.punish.PunishManager;
import lol.kangaroo.bungee.util.DurationFormat;
import lol.kangaroo.bungee.util.Message;
import lol.kangaroo.bungee.util.ThreadManager;
import lol.kangaroo.common.permissions.Rank;
import lol.kangaroo.common.player.BasePlayer;
import lol.kangaroo.common.player.CachedPlayer;
import lol.kangaroo.common.player.PlayerVariable;
import lol.kangaroo.common.player.punish.Ban;
import lol.kangaroo.common.player.punish.Blacklist;
import lol.kangaroo.common.player.punish.Mute;
import lol.kangaroo.common.player.punish.Punishment;
import lol.kangaroo.common.util.I18N;
import lol.kangaroo.common.util.MSG;
import lol.kangaroo.common.util.ServerNameFormat;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public class PlayerinfoSub extends Subcommand {

	public PlayerinfoSub(PlayerManager pm, ProxyServer proxy, AdminCommand parent) {
		super(pm, proxy, parent, "playerinfo", Rank.ADMIN_DEV.getPerm(), "playerdata", "pdata", "pinfo", "pd", "pcheck", "pc");
	}

	@Override
	public void execute(ProxiedPlayer sender, BasePlayer bp, String label, String[] args) {
		ThreadManager.async(() -> {
			if(args.length == 0) {
				Message.sendMessage(bp, MSG.PREFIX_ERROR, MSG.COMMAND_ADMIN_PLAYERINFO_USAGE);
				return;
			}
			CachedPlayer cp = pm.getCachedPlayer(pm.getFromAny(args[0]));
			if(cp == null) {
				Message.sendMessage(bp, MSG.PREFIX_ERROR, MSG.PLAYER_NOTFOUND);
				return;
			}
			RankManager rm = pm.getRankManager();
			PunishManager pum = pm.getPunishManager();
			Rank r = rm.getRank(cp, false); 
			String lastSeen = "";
			ProxiedPlayer pp = proxy.getPlayer(cp.getUniqueId());
			boolean onl = false;
			if(pp != null)
				onl = true;
			Instant lq = ((Timestamp) cp.getVariable(PlayerVariable.LASTQUIT)).toInstant();
			Instant now = Instant.now();
			Duration dur = Duration.between(lq, now);
			lastSeen = DurationFormat.getFormattedDuration(dur);
			Set<Punishment> puns = pum.getActivePunishments(cp.getUniqueId());
			Punishment mostActivePunishment = null;
			if(!puns.isEmpty()) {
				for(Punishment pun : puns) {
					if(pun instanceof Blacklist) {
						if(mostActivePunishment instanceof Blacklist) {
							if(Instant.ofEpochMilli(pun.getTimestamp()).isAfter(Instant.ofEpochMilli(mostActivePunishment.getTimestamp())))
								mostActivePunishment = pun;
							continue;
						} else
							mostActivePunishment = pun;
					} else if (pun instanceof Ban) {
						if(mostActivePunishment instanceof Ban) {
							if(mostActivePunishment.isPermanent())
								if(pun.getTimestamp() > mostActivePunishment.getTimestamp())
									mostActivePunishment = pun;
							else if(pun.isPermanent())
								mostActivePunishment = pun;
							else {
								long curEnd = mostActivePunishment.getTimestamp() + mostActivePunishment.getDuration();
								long punEnd = pun.getTimestamp() + pun.getDuration();
								if(punEnd > curEnd)
									mostActivePunishment = pun;
								else if(punEnd == curEnd && pun.getTimestamp() > mostActivePunishment.getTimestamp())
									mostActivePunishment = pun;
							}
						} else if(mostActivePunishment instanceof Blacklist)
							continue;
						else
							mostActivePunishment = pun;
						continue;
					} else if (pun instanceof Mute) {
						if(mostActivePunishment instanceof Mute) {
							if(mostActivePunishment.isPermanent())
								if(pun.getTimestamp() > mostActivePunishment.getTimestamp())
									mostActivePunishment = pun;
							else if(pun.isPermanent())
								mostActivePunishment = pun;
							else {
								long curEnd = mostActivePunishment.getTimestamp() + mostActivePunishment.getDuration();
								long punEnd = pun.getTimestamp() + pun.getDuration();
								if(punEnd > curEnd)
									mostActivePunishment = pun;
								else if(punEnd == curEnd && pun.getTimestamp() > mostActivePunishment.getTimestamp())
									mostActivePunishment = pun;
							}
						} else if(mostActivePunishment == null)
							mostActivePunishment = pun;
						continue;
					}
				}
			}
			MSG status = null;
			if(mostActivePunishment.isPermanent()) {
				if(mostActivePunishment instanceof Blacklist)
					status = MSG.COMMAND_ADMIN_PLAYERINFO_STATUS_BLACKLISTED;
				else if(mostActivePunishment instanceof Ban)
					status = MSG.COMMAND_ADMIN_PLAYERINFO_STATUS_PERMBANNED;
				else if(mostActivePunishment instanceof Mute)
					status = MSG.COMMAND_ADMIN_PLAYERINFO_STATUS_PERMMUTED;
			} else {
				if(mostActivePunishment instanceof Ban)
					status = MSG.COMMAND_ADMIN_PLAYERINFO_STATUS_TEMPBANNED;
				else if(mostActivePunishment instanceof Mute)
					status = MSG.COMMAND_ADMIN_PLAYERINFO_STATUS_TEMPMUTED;
			}
			CachedPlayer auth = pm.getCachedPlayer(mostActivePunishment.getAuthor());
			String author = rm.getPrefix(auth) + auth.getVariable(PlayerVariable.USERNAME);
			LocalDateTime punDate = new Timestamp(mostActivePunishment.getTimestamp()).toLocalDateTime();
			Duration punDur = Duration.ofMillis(mostActivePunishment.getDuration());
			String timeRemaining = DurationFormat.getFormattedDuration(Duration.between(Instant.now(), punDate.plus(punDur)));
			
			LocalDateTime ldt = PlayerVoteStreak.getLastVote(cp).toLocalDateTime();
			boolean warn = false;
			String vanStatus = MSG.COMMAND_ADMIN_PLAYERINFO_STATUS_VISIBLE.getMessage(Locale.getDefault());
			if(VanishAPI.isInvisibleOffline(cp.getUniqueId())) {
				warn = true;
				vanStatus = MSG.COMMAND_ADMIN_PLAYERINFO_STATUS_VANISHED.getMessage(Locale.getDefault()); 
			}
			Instant i = ((Timestamp) cp.getVariable(PlayerVariable.FIRSTJOIN)).toInstant();
			Duration fj = Duration.between(i, Instant.now());
			
			// TODO nickname support
			String nicked = MSG.COMMAND_ADMIN_PLAYERINFO_STATUS_UNNICKED.getMessage(Locale.getDefault());
			
			int lvl = PlayerLevel.getPlayerLevel(cp, false);
			PlayerLevel pl = PlayerLevel.getLevel(lvl);
			String levelStr = pl.getColor() + pl.getName() + " " + pl.getFormatted(lvl, true);
			
			long curExp = PlayerLevel.getExperience(cp);
			long reqExp = PlayerLevel.getRequiredExperience(lvl+1);
			
			long bal = Money.getBalance(cp);
			
			String lang = I18N.getPlayerLocale(cp).getDisplayName();
			
			if(onl)
				if(warn)
					lastSeen = MSG.COMMAND_ADMIN_PLAYERINFO_STATUS_ONLINE_WARNING.getMessage(Locale.getDefault()) + "&7(&b" + ServerNameFormat.format(pp.getServer().getInfo().getName()) + ")";
				else
					lastSeen = MSG.COMMAND_ADMIN_PLAYERINFO_STATUS_ONLINE.getMessage(Locale.getDefault()) + "&7(&b" + ServerNameFormat.format(pp.getServer().getInfo().getName()) + ")";
			
			Message.sendMessage(bp, MSG.COMMAND_ADMIN_PLAYERINFO, rm.getPrefix(cp) + cp.getVariable(PlayerVariable.USERNAME), r.getColor() + r.getName(), lastSeen);
			Message.sendMessage(bp, MSG.COMMAND_ADMIN_PLAYERINFO_PUNISHED, status.getMessage(Locale.getDefault()), mostActivePunishment.getReason(), author, punDate.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
			if(!mostActivePunishment.isPermanent()) Message.sendMessage(bp, MSG.COMMAND_ADMIN_PLAYERINFO_TEMPPUNISHED, DurationFormat.getFormattedDuration(punDur), timeRemaining);
			Message.sendMessage(bp, MSG.COMMAND_ADMIN_PLAYERINFO_DATA, PlayerVoteStreak.getStreak(cp), ldt.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME), vanStatus, nicked, DurationFormat.getFormattedDuration(fj), levelStr, curExp, reqExp, bal, lang);
		});
	}

	@Override
	public void executeConsole(String label, String[] args) {
		
	}
	
	
}
