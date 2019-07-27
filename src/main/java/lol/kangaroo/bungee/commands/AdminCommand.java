package lol.kangaroo.bungee.commands;

import java.util.Arrays;

import lol.kangaroo.bungee.commands.admin.CmdlistSub;
import lol.kangaroo.bungee.commands.admin.GrantSub;
import lol.kangaroo.bungee.commands.admin.PlayerinfoSub;
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
		registerSubcommand(new PlayerinfoSub(pm, proxy, this));
		registerSubcommand(new GrantSub(pm, proxy, this));
		registerSubcommand(new CmdlistSub(pm, proxy, this, subCommands.values()));
	}

	@Override
	public void execute(ProxiedPlayer sender, BasePlayer bp, String label, String[] args) {
		if(args.length == 0) {
			Message.sendMessage(bp, MSG.PREFIX_ADMIN, MSG.COMMAND_ADMIN_USAGE);
			return;
		}
		boolean cmdFound = false;
		String[] subArgs = Arrays.copyOfRange(args, 1, args.length);
		for(String k : subCommands.keySet()) {
			if(k.equalsIgnoreCase(args[0])) {
				Subcommand s = subCommands.get(k);
				cmdFound = true;
				if(sender.hasPermission(s.getPermission()))
					s.execute(sender, bp, args[0], subArgs);
				else
					Message.sendMessage(bp, MSG.PREFIX_ERROR, MSG.NO_PERM);
				break;
			}
		}
		if(!cmdFound)
			Message.sendMessage(bp, MSG.PREFIX_ERROR, MSG.UNKNOWN_COMMAND);
	}

	@Override
	public void executeConsole(String label, String[] args) {
		if(args.length == 0) {
			Message.sendConsole(MSG.PREFIX_ADMIN, MSG.COMMAND_ADMIN_USAGE);
			return;
		}
		boolean cmdFound = false;
		String[] subArgs = Arrays.copyOfRange(args, 1, args.length);
		for(String k : subCommands.keySet()) {
			if(k.equalsIgnoreCase(args[0])) {
				cmdFound = true;
				subCommands.get(k).executeConsole(args[0], subArgs);
				break;
			}
		}
		if(!cmdFound)
			Message.sendConsole(MSG.PREFIX_ERROR, MSG.UNKNOWN_COMMAND);
	}
	
}
