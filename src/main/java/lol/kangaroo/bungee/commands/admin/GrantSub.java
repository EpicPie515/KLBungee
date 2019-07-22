package lol.kangaroo.bungee.commands.admin;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lol.kangaroo.bungee.commands.AdminCommand;
import lol.kangaroo.bungee.commands.Subcommand;
import lol.kangaroo.bungee.player.PlayerManager;
import lol.kangaroo.bungee.util.Message;
import lol.kangaroo.common.permissions.Rank;
import lol.kangaroo.common.player.BasePlayer;
import lol.kangaroo.common.util.MSG;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public class GrantSub extends Subcommand {

	
	public GrantSub(PlayerManager pm, ProxyServer proxy, AdminCommand parent) {
		super(pm, proxy, parent, "grant", Rank.ADMIN_DEV.getPerm(), "gr");
	}
	
	private static final Pattern argPattern = Pattern.compile("^-([a-z][a-z]*)=(.+)$");

	@Override
	public void execute(ProxiedPlayer sender, BasePlayer bp, String label, String[] args) {
		Map<Character, String> grantArgs = new HashMap<>();
		if(args.length == 0) {
			Message.sendMessage(bp, MSG.PREFIX_ERROR, MSG.COMMAND_ADMIN_GRANT_USAGE);
			return;
		}
		for(int i = 0; i < args.length; i++) {
			Matcher m = argPattern.matcher(args[i]);
			if(m.matches()) {
				if(grantArgs.containsKey(m.group(1).charAt(0))) {
					Message.sendMessage(bp, MSG.PREFIX_ERROR, MSG.COMMAND_ADMIN_GRANT_DUPLICATEARG, m.group(1));
					return;
				}
				grantArgs.put(m.group(1).charAt(0), m.group(2));
			} else {
				Message.sendMessage(bp, MSG.PREFIX_ERROR, MSG.COMMAND_ADMIN_GRANT_USAGE);
				return;
			}
		}
		String authCode = null;
		// 1 = rank, 2 = permission
		byte type = 0;
		String typeValue = null, permValue = null, timeStr = null, note = null;
		for(Entry<Character, String> gArg : grantArgs.entrySet()) {
			switch(gArg.getKey()) {
			case 'a':
				authCode = gArg.getValue();
				break;
			case 'r':
				type = 1;
				typeValue = gArg.getValue();
				break;
			case 'p':
				type = 2;
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
		if(timeStr == null) {
			Message.sendMessage(bp, MSG.PREFIX_ERROR, MSG.COMMAND_ADMIN_GRANT_TIMEREQ);
			return;
		}
		// TODO check auth and stuff
	}

	@Override
	public void executeConsole(String label, String[] args) {
		// TODO authenticate still
	}
	
	
}
