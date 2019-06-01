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

public class MuteCommand extends CommandExecutor {

	public MuteCommand(PlayerManager pm, ProxyServer proxy) {
		super(pm, proxy, "mute", Rank.JRMOD.getPerm(), "smute");
	}
	
	private static final Pattern fullTimeCheck = Pattern.compile("^([0-9]+)([smhdwy])$");

	@Override
	public void execute(ProxiedPlayer sender, BasePlayer bp, String label, String[] args) {
		if(args.length == 0){
			Message.sendMessage(sender, MSG.PREFIX_ERROR, MSG.COMMAND_MUTE_USAGE);
			return;
		}
		proxy.getScheduler().runAsync(KLBungeePlugin.instance, () -> {
			UUID uuid = pm.getFromAny(args[0]);
			CachedPlayer cp = pm.getCachedPlayer(uuid);
			if(args.length == 1) {
				PluginMessage.sendToSpigot(sender, "CommandGUI", new MessageWrapper("mute").writeUuid(uuid));
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
			if(!pm.mutePlayer(cp, dur, reason, bp, (label.equalsIgnoreCase("smute") ? true : false)))
				Message.sendMessage(sender, MSG.PREFIX_ERROR, MSG.COMMAND_MUTE_ALREADY);
		});
	}

	@Override
	public void executeConsole(String label, String[] args) {
		if(args.length <= 1){
			Message.sendConsole(MSG.PREFIX_ERROR, MSG.COMMAND_MUTE_USAGE);
			return;
		}
		proxy.getScheduler().runAsync(KLBungeePlugin.instance, () -> {
			UUID uuid = pm.getFromAny(args[0]);
			CachedPlayer cp = pm.getCachedPlayer(uuid);
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
			if(!pm.mutePlayer(cp, dur, reason, null, (label.equalsIgnoreCase("smute") ? true : false)))
				Message.sendConsole(MSG.PREFIX_ERROR, MSG.COMMAND_MUTE_ALREADY);
		});
	}
	
}
