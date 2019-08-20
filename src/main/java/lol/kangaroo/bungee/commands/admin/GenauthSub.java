package lol.kangaroo.bungee.commands.admin;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lol.kangaroo.bungee.commands.AdminCommand;
import lol.kangaroo.bungee.commands.Subcommand;
import lol.kangaroo.bungee.database.Auth;
import lol.kangaroo.bungee.player.PlayerManager;
import lol.kangaroo.bungee.util.Message;
import lol.kangaroo.bungee.util.ThreadManager;
import lol.kangaroo.common.permissions.Rank;
import lol.kangaroo.common.player.BasePlayer;
import lol.kangaroo.common.player.CachedPlayer;
import lol.kangaroo.common.player.PlayerVariable;
import lol.kangaroo.common.util.MSG;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public class GenauthSub extends Subcommand {

	private static final Pattern argPattern = Pattern.compile("^-([a-z][a-z]*)=(.+)$", Pattern.CASE_INSENSITIVE);
	
	public GenauthSub(PlayerManager pm, ProxyServer proxy, AdminCommand parent) {
		super(pm, proxy, parent, "genauth", Rank.ADMIN_DEV.getPerm(), "getauth", "createauth");
	}

	@Override
	public void execute(ProxiedPlayer sender, BasePlayer bp, String label, String[] args) {
		ThreadManager.async(() -> {
			if(args.length <= 1 || argPattern.matcher(args[0]).matches()) {
				Message.sendMessage(bp, MSG.PREFIX_ERROR, MSG.COMMAND_ADMIN_GENAUTH_USAGE);
				return;
			}
			CachedPlayer target = pm.getCachedPlayer(pm.getFromCurrentExact(args[0]));
			if(target == null) {
				Message.sendMessage(bp, MSG.PREFIX_ERROR, MSG.PLAYER_NOTFOUND);
				return;
			}
			Matcher match = argPattern.matcher(args[1]);
			if(!match.matches() || !(match.group(1).toLowerCase().charAt(0) == 'a')) {
				Message.sendMessage(bp, MSG.PREFIX_ERROR, MSG.COMMAND_ADMIN_GENAUTH_USAGE);
				return;
			}
			String authStr = match.group(2);
			int authCode = 0;
			try {
				authCode = Integer.parseInt(authStr);
			} catch(NumberFormatException e) {
				Message.sendMessage(bp, MSG.PREFIX_ERROR, MSG.ADMIN_AUTH_FAIL);
				return;
			}
			if(!Auth.validate(bp, authCode, 20000)) {
				Message.sendMessage(bp, MSG.PREFIX_ERROR, MSG.ADMIN_AUTH_FAIL);
				return;
			}
			Rank r = pm.getRankManager().getRank(target, false);
			String targetName = pm.getRankManager().getPrefix(target) + target.getVariable(PlayerVariable.USERNAME);
			if(!r.isStaff()) {
				Message.sendMessage(bp, MSG.PREFIX_ERROR, MSG.COMMAND_ADMIN_GENAUTH_NOTSTAFF, targetName, r.getColor() + r.name());
				return;
			}
			ProxiedPlayer pp = proxy.getPlayer(target.getUniqueId());
			if(pp == null) {
				Message.sendMessage(bp, MSG.PREFIX_ERROR, MSG.PLAYER_NOTFOUND);
				return;
			}
			if(Auth.hasSecret(target)) {
				Message.sendMessage(bp, MSG.PREFIX_ERROR, MSG.COMMAND_ADMIN_GENAUTH_OTHER_ALREADY);
				return;
			}
			String secret = Auth.generateAndAssignSecret(target);
			Message.sendMessage(target, MSG.PREFIX_ADMIN, MSG.COMMAND_ADMIN_GENAUTH, secret);
			Message.sendMessage(bp, MSG.COMMAND_ADMIN_GENAUTH_OTHER, pm.getRankManager().getPrefix(target) + target.getVariable(PlayerVariable.USERNAME));
		});
	}

	@Override
	public void executeConsole(String label, String[] args) {
		Message.sendConsole(MSG.PREFIX_ERROR, MSG.MUST_BE_PLAYER);
	}
	
	
}
