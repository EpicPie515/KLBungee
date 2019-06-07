package lol.kangaroo.bungee.commands;

import java.util.Arrays;

import lol.kangaroo.bungee.commands.admin.TestvoteSub;
import lol.kangaroo.bungee.player.PlayerManager;
import lol.kangaroo.bungee.util.Message;
import lol.kangaroo.common.permissions.Rank;
import lol.kangaroo.common.player.BasePlayer;
import lol.kangaroo.common.util.MSG;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public class AdminCommand extends CommandExecutor {
	

	public AdminCommand(PlayerManager pm, ProxyServer proxy) {
		super(pm, proxy, "admin", Rank.SRMOD.getPerm(), "adm", "control", "con");
		
		registerSubcommand(new TestvoteSub(pm, proxy, this));
	}

	@Override
	public void execute(ProxiedPlayer sender, BasePlayer bp, String label, String[] args) {
		if(args.length == 0) {
			Message.sendMessage(bp, MSG.PREFIX_ERROR, MSG.COMMAND_ADMIN_USAGE);
			return;
		}
		String[] subArgs = Arrays.copyOfRange(args, 1, args.length);
		for(String k : subCommands.keySet()) {
			if(k.equalsIgnoreCase(args[0])) {
				subCommands.get(k).execute(sender, bp, args[0], subArgs);
				break;
			}
		}
	}

	@Override
	public void executeConsole(String label, String[] args) {
		if(args.length == 0) {
			Message.sendConsole(MSG.PREFIX_ERROR, MSG.COMMAND_ADMIN_USAGE);
			return;
		}
		String[] subArgs = Arrays.copyOfRange(args, 1, args.length);
		for(String k : subCommands.keySet()) {
			if(k.equalsIgnoreCase(args[0])) {
				// TODO permissions
				subCommands.get(k).executeConsole(args[0], subArgs);
				break;
			}
		}
	}
	
}
