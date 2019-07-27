package lol.kangaroo.bungee.commands.admin;

import com.vexsoftware.votifier.bungee.events.VotifierEvent;
import com.vexsoftware.votifier.model.Vote;

import lol.kangaroo.bungee.commands.AdminCommand;
import lol.kangaroo.bungee.commands.Subcommand;
import lol.kangaroo.bungee.player.PlayerManager;
import lol.kangaroo.bungee.util.Message;
import lol.kangaroo.common.permissions.Rank;
import lol.kangaroo.common.player.BasePlayer;
import lol.kangaroo.common.util.MSG;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public class TestvoteSub extends Subcommand {

	public TestvoteSub(PlayerManager pm, ProxyServer proxy, AdminCommand parent) {
		super(pm, proxy, parent, "testvote", Rank.ADMIN_DEV.getPerm(), "tv", "addvote", "manualvote");
	}

	@Override
	public void execute(ProxiedPlayer sender, BasePlayer bp, String label, String[] args) {
		if(args.length == 0) {
			Message.sendMessage(bp, MSG.PREFIX_ADMIN, MSG.COMMAND_ADMIN_TESTVOTE_USAGE);
			return;
		}
		proxy.getPluginManager().callEvent(new VotifierEvent(new Vote("Manual Vote", args[0], "", "")));
		Message.sendMessage(bp, MSG.PREFIX_ADMIN, MSG.COMMAND_ADMIN_TESTVOTE, args[0]);
	}

	@Override
	public void executeConsole(String label, String[] args) {
		if(args.length == 0) {
			Message.sendConsole(MSG.PREFIX_ADMIN, MSG.COMMAND_ADMIN_TESTVOTE_USAGE);
			return;
		}
		proxy.getPluginManager().callEvent(new VotifierEvent(new Vote("Manual Vote", args[0], "", "")));
		Message.sendConsole(MSG.PREFIX_ADMIN, MSG.COMMAND_ADMIN_TESTVOTE, args[0]);
	}
	
	
}
