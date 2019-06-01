package lol.kangaroo.bungee.commands.punish;

import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lol.kangaroo.bungee.KLBungeePlugin;
import lol.kangaroo.bungee.commands.CommandExecutor;
import lol.kangaroo.bungee.player.PlayerManager;
import lol.kangaroo.bungee.util.DurationStringCalc;
import lol.kangaroo.bungee.util.Message;
import lol.kangaroo.bungee.util.PluginMessage;
import lol.kangaroo.bungee.util.PluginMessage.MessageWrapper;
import lol.kangaroo.common.permissions.Rank;
import lol.kangaroo.common.player.BasePlayer;
import lol.kangaroo.common.player.CachedPlayer;
import lol.kangaroo.common.util.MSG;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public class BanCommand extends CommandExecutor {

	public BanCommand(PlayerManager pm, ProxyServer proxy) {
		super(pm, proxy, "ban", Rank.MOD.getPerm(), "sban");
	}

	private static final Pattern fullTimeCheck = Pattern.compile("^([0-9]+)([smhdwy])$");

	@Override
	public void execute(ProxiedPlayer sender, BasePlayer bp, String label, String[] args) {
		if(args.length == 0){
			Message.sendMessage(sender, MSG.PREFIX_ERROR, MSG.COMMAND_BAN_USAGE);
			return;
		}
		proxy.getScheduler().runAsync(KLBungeePlugin.instance, () -> {
			UUID uuid = pm.getFromAny(args[0]);
			CachedPlayer cp = pm.getCachedPlayer(uuid);
			if(args.length == 1) {
				PluginMessage.sendToSpigot(sender, "CommandGUI", new MessageWrapper("ban").writeUuid(uuid));
				return;
			}
			long dur = -1;
			int argI = 1;
			Matcher match = fullTimeCheck.matcher(args[1]);
			if(match.matches()) {
				argI = 2;
				long n = Long.parseLong(match.group(1));
				char u = args[1].charAt(match.start(2));
				dur = DurationStringCalc.calculate(n, u);
			}
			String reason = "No Reason Specified";
			if(args.length > argI) {
				reason = "";
				for(int i = argI; i < args.length; i++) {
					reason += args[i] + " ";
				}
			}
			reason = reason.trim();
			if(!pm.banPlayer(cp, dur, reason, bp, (label.equalsIgnoreCase("sban") ? true : false)))
				Message.sendMessage(sender, MSG.PREFIX_ERROR, MSG.COMMAND_BAN_ALREADY);
		});
	}

	@Override
	public void executeConsole(String label, String[] args) {
		if(args.length <= 1){
			Message.sendConsole(MSG.PREFIX_ERROR, MSG.COMMAND_BAN_USAGE);
			return;
		}
		proxy.getScheduler().runAsync(KLBungeePlugin.instance, () -> {
			UUID uuid = pm.getFromAny(args[0]);
			CachedPlayer cp = pm.getCachedPlayer(uuid);
			long dur = -1;
			int argI = 1;
			if(args[1].startsWith("-")) {
				argI = 2;
				long n = Long.parseLong(args[1].substring(1, args[1].length() - 1));
				dur = DurationStringCalc.calculate(n, args[1].charAt(args[1].length() - 1));
			}
			String reason = "No Reason Specified";
			if(args.length > argI) {
				reason = "";
				for(int i = argI; i < args.length; i++) {
					reason += args[i] + " ";
				}
			}
			reason = reason.trim();
			if(!pm.banPlayer(cp, dur, reason, null, (label.equalsIgnoreCase("sban") ? true : false)))
				Message.sendConsole(MSG.PREFIX_ERROR, MSG.COMMAND_BAN_ALREADY);
		});
	}
	
}
