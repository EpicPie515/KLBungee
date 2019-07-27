package lol.kangaroo.bungee.commands.admin;

import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lol.kangaroo.bungee.commands.AdminCommand;
import lol.kangaroo.bungee.commands.Subcommand;
import lol.kangaroo.bungee.database.Auth;
import lol.kangaroo.bungee.database.Logs;
import lol.kangaroo.bungee.player.PlayerManager;
import lol.kangaroo.bungee.util.Message;
import lol.kangaroo.bungee.util.ThreadManager;
import lol.kangaroo.common.permissions.PermissionManager;
import lol.kangaroo.common.permissions.Rank;
import lol.kangaroo.common.player.BasePlayer;
import lol.kangaroo.common.player.CachedPlayer;
import lol.kangaroo.common.player.PlayerUpdateCache;
import lol.kangaroo.common.player.PlayerVariable;
import lol.kangaroo.common.util.DurationFormat;
import lol.kangaroo.common.util.DurationStringCalc;
import lol.kangaroo.common.util.I18N;
import lol.kangaroo.common.util.MSG;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public class GrantSub extends Subcommand {

	
	public GrantSub(PlayerManager pm, ProxyServer proxy, AdminCommand parent) {
		super(pm, proxy, parent, "grant", Rank.ADMIN_DEV.getPerm(), "gr");
	}
	
	private static final Pattern argPattern = Pattern.compile("^-([a-z][a-z]*)=(.+)$");
	private static final Pattern timePattern = Pattern.compile("^([0-9]+)([smhdwy])$");

	@Override
	public void execute(ProxiedPlayer sender, BasePlayer bp, String label, String[] args) {
		Map<Character, String> grantArgs = new HashMap<>();
		if(args.length <= 1) {
			Message.sendMessage(bp, MSG.PREFIX_ERROR, MSG.COMMAND_ADMIN_GRANT_USAGE);
			return;
		}
		CachedPlayer target = pm.getCachedPlayer(pm.getFromCurrentExact(args[0]));
		if(target == null) {
			Message.sendMessage(bp, MSG.PREFIX_ERROR, MSG.PLAYER_NOTFOUND);
			return;
		}
		for(int i = 1; i < args.length; i++) {
			Matcher m = argPattern.matcher(args[i]);
			if(m.matches()) {
				char param = m.group(1).charAt(0);
				if(grantArgs.containsKey(param)) {
					Message.sendMessage(bp, MSG.PREFIX_ERROR, MSG.COMMAND_ADMIN_GRANT_DUPLICATEARG, m.group(1));
					return;
				}
				grantArgs.put(param, m.group(2));
			} else {
				Message.sendMessage(bp, MSG.PREFIX_ERROR, MSG.COMMAND_ADMIN_GRANT_USAGE);
				return;
			}
		}
		String authCode = null;
		// 1 = rank, 2 = permission
		byte type = 0;
		String typeValue = null, permValue = null, timeStr = null, note = "None";
		for(Entry<Character, String> gArg : grantArgs.entrySet()) {
			switch(gArg.getKey()) {
			case 'a':
				authCode = gArg.getValue();
				break;
			case 'p':
				type = 2;
				typeValue = gArg.getValue();
				break;
			case 'r':
				type = 1;
				typeValue = gArg.getValue();
				break;
			case 'v':
				permValue = gArg.getValue();
				break;
			case 't':
				timeStr = gArg.getValue();
				break;
			case 'n':
				note = gArg.getValue();
				break;
			default:
				Message.sendMessage(bp, MSG.PREFIX_ERROR, MSG.COMMAND_ADMIN_GRANT_INVALIDARG, gArg.getKey());
				return;
			}
		}
		if(type == 0) {
			Message.sendMessage(bp, MSG.PREFIX_ERROR, MSG.COMMAND_ADMIN_GRANT_RANKORPERMREQ);
			return;
		}
		if(permValue == null) {
			Message.sendMessage(bp, MSG.PREFIX_ERROR, MSG.COMMAND_ADMIN_GRANT_PERMREQVALUE);
			return;
		}
		boolean pVal = true;
		if(permValue.toLowerCase().startsWith("t")) 
			pVal = true;
		else if(permValue.toLowerCase().startsWith("f"))
			pVal = false;
		else if(permValue.equals("0"))
			pVal = false;
		else if(permValue.equals("1"))
			pVal = true;
		else {
			Message.sendMessage(bp, MSG.PREFIX_ERROR, MSG.COMMAND_ADMIN_GRANT_PERMREQVALUE);
			return;
		}
		if(timeStr == null) {
			Message.sendMessage(bp, MSG.PREFIX_ERROR, MSG.COMMAND_ADMIN_GRANT_TIMEREQ);
			return;
		}
		if(authCode == null) {
			Message.sendMessage(bp, MSG.PREFIX_ERROR, MSG.ADMIN_AUTH_REQUIRED);
			return;
		}
		int otpCode = -1;
		try {
			otpCode = Integer.parseInt(authCode);
		} catch(NumberFormatException e) {
			Message.sendMessage(bp, MSG.PREFIX_ERROR, MSG.ADMIN_AUTH_FAIL);
			return;
		}
		grantAsync(bp, target, otpCode, timeStr, note, type, typeValue, pVal);
	}
	
	private void grantAsync(final BasePlayer bp, final CachedPlayer target, final int otpCode, final String timeStr, final String note, final byte type, final String typeValue, final boolean pVal) {
		ThreadManager.async(() -> {
			
			if(!Auth.validate(bp, otpCode, 20000)) {
				Message.sendMessage(bp, MSG.PREFIX_ERROR, MSG.ADMIN_AUTH_FAIL);
				return;
			}
			long durNum = -1;
			Matcher match = timePattern.matcher(timeStr);
			if(match.matches()) {
				long n = Long.parseLong(match.group(1));
				char u = match.group(2).charAt(0);
				durNum = DurationStringCalc.calculate(n, u);
			} else {
				if(!timeStr.equals("0")) {
					Message.sendMessage(bp, MSG.PREFIX_ERROR, MSG.TIMEFORMAT_INVALID);
					return;
				}
			}
			boolean permanent = durNum == -1;
			Instant end = Instant.now().plusMillis(durNum);
			String typeValFormatted = null;
			String dur = DurationFormat.getFormattedDuration(Duration.between(Instant.now(), end), I18N.getPlayerLocale(bp), false);
			if(type == 1) {
				Rank rank = Rank.getByName(typeValue);
				typeValFormatted = rank.getColor() + rank.getName();
				PlayerUpdateCache c = target.createUpdateCache();
				target.setVariableInUpdate(c, PlayerVariable.RANK, rank);
				target.setVariableInUpdate(c, PlayerVariable.RANK_EXPIRETIME, (permanent ? null : Timestamp.from(end)));
				c.pushUpdates();
				Message.sendMessage(bp, MSG.PREFIX_ADMIN, MSG.COMMAND_ADMIN_GRANT_SUCCESS, 
						pm.getRankManager().getPrefixDirect(target.getUniqueId()) + target.getVariable(PlayerVariable.USERNAME), 
						typeValFormatted, 
						dur, 
						note);
				Message.sendMessage(target, MSG.PLAYER_GRANTEDRANK, typeValFormatted, dur);
			} else if(type == 2) {
				typeValFormatted = pVal ? (ChatColor.GREEN + "+" + typeValue.toLowerCase()) : (ChatColor.RED + "-" + typeValue.toLowerCase());
				PermissionManager prm = pm.getPermissionManager();
				prm.setPlayerPermission(target, typeValue, pVal, (permanent ? null : Timestamp.from(end)));
				Message.sendMessage(bp, MSG.PREFIX_ADMIN, MSG.COMMAND_ADMIN_GRANT_SUCCESS, 
						pm.getRankManager().getPrefixDirect(target.getUniqueId()) + target.getVariable(PlayerVariable.USERNAME), 
						typeValFormatted, 
						dur, 
						note);
				Message.sendMessage(target, MSG.PLAYER_GRANTEDRANK, typeValFormatted, dur);
			}
			Logs.Grant.addLog(target.getUniqueId(), System.currentTimeMillis(), bp.getUniqueId(), 'g', type, ChatColor.stripColor(typeValFormatted), note, pVal);
		});
	}

	@Override
	public void executeConsole(String label, String[] args) {
		// TODO authenticate still
	}
	
	
}
