package lol.kangaroo.bungee.commands.player;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Locale;

import lol.kangaroo.bungee.commands.CommandExecutor;
import lol.kangaroo.bungee.player.PlayerManager;
import lol.kangaroo.bungee.player.PlayerVoteStreak;
import lol.kangaroo.bungee.util.Message;
import lol.kangaroo.common.permissions.Rank;
import lol.kangaroo.common.player.BasePlayer;
import lol.kangaroo.common.player.CachedPlayer;
import lol.kangaroo.common.player.PlayerVariable;
import lol.kangaroo.common.util.DurationFormat;
import lol.kangaroo.common.util.I18N;
import lol.kangaroo.common.util.MSG;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public class LinksCommand extends CommandExecutor {

	public LinksCommand(PlayerManager pm, ProxyServer proxy) {
		super(pm, proxy, "links", Rank.PLAYER.getPerm(), "info", "ts", "teamspeak", "discord", "website", "store", "vote");
	}
	
	@Override
	public void execute(ProxiedPlayer sender, BasePlayer bp, String label, String[] args) {
		if(args.length == 0) {
			// Send message to sender
			switch(label.toLowerCase()) {
			case "teamspeak":
			case "ts":
				sendTeamspeakInfo(bp, null, null, null, label);
				break;
			case "discord":
				sendDiscordInfo(bp, null, null, null, label);
				break;
			case "website":
				sendWebsiteInfo(bp, null, null, null, label);
				break;
			case "store":
				sendStoreInfo(bp, null, null, null, label);
				break;
			case "vote":
				sendVoteInfo(bp, null, null, null, label);
				break;
			case "links":
			case "info":
			default:
				sendAllLinksInfo(bp, null, null, null, label);
				break;
			}
		} else {
			// Send message to target (staff only)
			Rank r = pm.getRankManager().getRank(bp, false);
			if(!r.isStaff()) {
				Message.sendMessage(bp, MSG.PREFIX_ERROR, MSG.NO_PERM);
				return;
			}
			String senderPrefix = pm.getRankManager().getPrefix(bp, true);
			String senderNick = (String) bp.getVariable(PlayerVariable.NICKNAME);
			CachedPlayer cp = pm.getCachedPlayer(pm.getFromCurrentNick(args[0]));
			if(cp == null) {
				Message.sendMessage(bp, MSG.PREFIX_ERROR, MSG.PLAYER_NOTFOUND);
				return;
			}
			ProxiedPlayer pp = proxy.getPlayer(cp.getUniqueId());
			if(pp == null) {
				Message.sendMessage(bp, MSG.PREFIX_ERROR, MSG.PLAYER_OFFLINE);
				return;
			}
			String prefix = pm.getRankManager().getPrefix(cp, true);
			String nickname = (String) cp.getVariable(PlayerVariable.NICKNAME);
			switch(label.toLowerCase()) {
			case "teamspeak":
			case "ts":
				sendTeamspeakInfo(cp, bp, prefix + nickname, senderPrefix + senderNick, label);
				break;
			case "discord":
				sendDiscordInfo(cp, bp, prefix + nickname, senderPrefix + senderNick, label);
				break;
			case "website":
				sendWebsiteInfo(cp, bp, prefix + nickname, senderPrefix + senderNick, label);
				break;
			case "store":
				sendStoreInfo(cp, bp, prefix + nickname, senderPrefix + senderNick, label);
				break;
			case "vote":
				sendVoteInfo(cp, bp, prefix + nickname, senderPrefix + senderNick, label);
				break;
			case "links":
			case "info":
			default:
				sendAllLinksInfo(cp, bp, prefix + nickname, senderPrefix + senderNick, label);
				break;
			}
		}
	}

	@Override
	public void executeConsole(String label, String[] args) {
		if(args.length == 0) {
			Message.sendConsole(MSG.PREFIX_ERROR, MSG.COMMAND_LINKS_ALL);
			return;
		}
		CachedPlayer bp = pm.getCachedPlayer(pm.getFromCurrentNick(args[0]));
		if(bp == null) {
			Message.sendConsole(MSG.PREFIX_ERROR, MSG.PLAYER_NOTFOUND);
			return;
		}
		ProxiedPlayer pp = proxy.getPlayer(bp.getUniqueId());
		if(pp == null) {
			Message.sendConsole( MSG.PREFIX_ERROR, MSG.PLAYER_OFFLINE);
			return;
		}
		switch(label.toLowerCase()) {
		case "teamspeak":
		case "ts":
			sendTeamspeakInfo(bp, null, null, null, label);
			break;
		case "discord":
			sendDiscordInfo(bp, null, null, null, label);
			break;
		case "website":
			sendWebsiteInfo(bp, null, null, null, label);
			break;
		case "store":
			sendStoreInfo(bp, null, null, null, label);
			break;
		case "vote":
			sendVoteInfo(bp, null, null, null, label);
			break;
		case "links":
		case "info":
		default:
			sendAllLinksInfo(bp, null, null, null, label);
			break;
		}
	}
	
	public void sendAllLinksInfo(BasePlayer target, BasePlayer sender, String targetName, String senderName, String label) {
		if(sender != null) {
			Message.sendMessage(target, MSG.COMMAND_LINKS_ALL_OTHER, senderName);
			Message.sendMessage(sender, MSG.COMMAND_LINKS_SENDER, label, targetName);
		} else {
			Message.sendMessage(target, MSG.COMMAND_LINKS_ALL_SELF);
		}
		Message.sendMessage(target, MSG.COMMAND_LINKS_ALL);
	}
	
	public void sendTeamspeakInfo(BasePlayer target, BasePlayer sender, String targetName, String senderName, String label) {
		if(sender != null) {
			Message.sendMessage(target, MSG.COMMAND_LINKS_TS_OTHER, senderName);
			Message.sendMessage(sender, MSG.COMMAND_LINKS_SENDER, label, targetName);
		} else {
			Message.sendMessage(target, MSG.COMMAND_LINKS_TS_SELF);
		}
		Message.sendMessage(target, MSG.COMMAND_LINKS_TS);
	}
	
	public void sendDiscordInfo(BasePlayer target, BasePlayer sender, String targetName, String senderName, String label) {
		if(sender != null) {
			Message.sendMessage(target, MSG.COMMAND_LINKS_DISCORD_OTHER, senderName);
			Message.sendMessage(sender, MSG.COMMAND_LINKS_SENDER, label, targetName);
		} else {
			Message.sendMessage(target, MSG.COMMAND_LINKS_DISCORD_SELF);
		}
		Message.sendMessage(target, MSG.COMMAND_LINKS_DISCORD);
	}
	
	public void sendWebsiteInfo(BasePlayer target, BasePlayer sender, String targetName, String senderName, String label) {
		if(sender != null) {
			Message.sendMessage(target, MSG.COMMAND_LINKS_WEBSITE_OTHER, senderName);
			Message.sendMessage(sender, MSG.COMMAND_LINKS_SENDER, label, targetName);
		} else {
			Message.sendMessage(target, MSG.COMMAND_LINKS_WEBSITE_SELF);
		}
		Message.sendMessage(target, MSG.COMMAND_LINKS_WEBSITE);
	}
	
	public void sendStoreInfo(BasePlayer target, BasePlayer sender, String targetName, String senderName, String label) {
		if(sender != null) {
			Message.sendMessage(target, MSG.COMMAND_LINKS_STORE_OTHER, senderName);
			Message.sendMessage(sender, MSG.COMMAND_LINKS_SENDER, label, targetName);
		} else {
			Message.sendMessage(target, MSG.COMMAND_LINKS_STORE_SELF);
		}
		Message.sendMessage(target, MSG.COMMAND_LINKS_STORE);
	}
	
	public void sendVoteInfo(BasePlayer target, BasePlayer sender, String targetName, String senderName, String label) {
		if(sender != null) {
			Message.sendMessage(target, MSG.COMMAND_LINKS_VOTE_OTHER, senderName);
			Message.sendMessage(sender, MSG.COMMAND_LINKS_SENDER, label, targetName);
		} else {
			Message.sendMessage(target, MSG.COMMAND_LINKS_VOTE_SELF);
		}
		Locale lang = I18N.getPlayerLocale(target);
		int curStreak = PlayerVoteStreak.getStreak(target);
		Instant lvInst = PlayerVoteStreak.getLastVote(target).toInstant();
		boolean expired = Instant.now().isAfter(lvInst.plus(48, ChronoUnit.HOURS));
		boolean timeLeftWarn = Instant.now().isAfter(lvInst.plus(36, ChronoUnit.HOURS)) && !expired;
		String timeLeft = (timeLeftWarn ? 
				MSG.COMMAND_LINKS_VOTE_TIMELEFT.getMessage(
						target, 
						DurationFormat.getFormatted1UnitDuration(
								Duration.between(Instant.now(), lvInst.plus(48, ChronoUnit.HOURS)), 
								lang, 
								false
						)
				)
				: "");
		String lastVote = DurationFormat.getFormatted1UnitDuration(Duration.between(Instant.now(), lvInst), I18N.getPlayerLocale(target), true);
		Message.sendMessage(target, MSG.COMMAND_LINKS_VOTE, lastVote, timeLeft, curStreak, expired ? MSG.COMMAND_LINKS_VOTE_EXPIRED : "");
	}
	
}
