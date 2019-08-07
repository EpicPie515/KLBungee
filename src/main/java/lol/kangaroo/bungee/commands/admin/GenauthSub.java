package lol.kangaroo.bungee.commands.admin;

import com.vexsoftware.votifier.bungee.events.VotifierEvent;
import com.vexsoftware.votifier.model.Vote;

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

	public GenauthSub(PlayerManager pm, ProxyServer proxy, AdminCommand parent) {
		super(pm, proxy, parent, "genauth", Rank.ADMIN_DEV.getPerm(), "getauth", "createauth");
	}

	@Override
	public void execute(ProxiedPlayer sender, BasePlayer bp, String label, String[] args) {
		// TODO REQUIRE AUTH TO GENERATE (and i guess remove self-generating) and ADMIN COMMAND
		ThreadManager.async(() -> {
			if(args.length == 0) {
				if(Auth.hasSecret(bp)) {
					Message.sendMessage(bp, MSG.PREFIX_ERROR, MSG.COMMAND_ADMIN_GENAUTH_ALREADY);
					return;
				}
				String secret = Auth.generateAndAssignSecret(bp);
				Message.sendMessage(bp, MSG.PREFIX_ADMIN, MSG.COMMAND_ADMIN_GENAUTH, secret);
			} else {
				CachedPlayer target = pm.getCachedPlayer(pm.getFromCurrentExact(args[0]));
				if(target == null) {
					Message.sendMessage(bp, MSG.PREFIX_ERROR, MSG.PLAYER_NOTFOUND);
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
			}
		});
	}

	@Override
	public void executeConsole(String label, String[] args) {
		if(args.length == 0) {
			Message.sendConsole(MSG.PREFIX_ERROR, MSG.COMMAND_ADMIN_TESTVOTE_USAGE);
			return;
		}
		proxy.getPluginManager().callEvent(new VotifierEvent(new Vote("Manual Vote", args[0], "", "")));
		Message.sendConsole(MSG.PREFIX_ADMIN, MSG.COMMAND_ADMIN_TESTVOTE, args[0]);
	}
	
	
}
