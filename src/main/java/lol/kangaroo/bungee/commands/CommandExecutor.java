package lol.kangaroo.bungee.commands;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import lol.kangaroo.bungee.KLBungeePlugin;
import lol.kangaroo.bungee.player.PlayerManager;
import lol.kangaroo.bungee.util.Message;
import lol.kangaroo.common.player.BasePlayer;
import lol.kangaroo.common.player.CachedPlayer;
import lol.kangaroo.common.util.MSG;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

public abstract class CommandExecutor {
	

	protected Map<String, Subcommand> subCommands = new HashMap<>();
	
	protected PlayerManager pm;
	protected ProxyServer proxy;
	protected String label;
	protected String perm;
	protected List<String> aliases;
	
	public CommandExecutor(PlayerManager pm, ProxyServer proxy, String label, String perm, String... aliases) {
		this.pm = pm;
		this.proxy = proxy;
		this.label = label;
		this.perm = perm;
		this.aliases = Arrays.asList(aliases);
	}
	
	protected void registerSubcommand(Subcommand sub) {
		subCommands.put(sub.getLabel(), sub);
		for(String al : sub.getAliases())
			subCommands.put(al, sub);
	}
	
	public String getLabel() {
		return label;
	}
	
	public String getPermission() {
		return perm;
	}
	
	public List<String> getAliases() {
		return aliases;
	}
	
	/**
	 * @param sender the ProxiedPlayer Sender.
	 * @param bp the BasePlayer sender.
	 */
	public abstract void execute(ProxiedPlayer sender, BasePlayer bp, String label, String[] args);
	
	public abstract void executeConsole(String label, String[] args);
	
	private static Set<CommandExecutor> commands = new HashSet<>();
	
	public static void registerCommand(CommandExecutor ce) {
		commands.add(ce);
		List<String> aliases = ce.getAliases();
		Command labelCommand = new Command(ce.getLabel(), ce.getPermission()) {
			@Override
			public void execute(CommandSender sender, String[] args) {
					CommandExecutor.execute(ce, sender, this.getName(), args);
			}
		};
		ProxyServer.getInstance().getPluginManager().registerCommand(KLBungeePlugin.instance, labelCommand);
		for(String alias : aliases) {
			Command aliasCommand = new Command(alias, ce.getPermission()) {
				@Override
				public void execute(CommandSender sender, String[] args) {
					CommandExecutor.execute(ce, sender, this.getName(), args);
				}
			};
			ProxyServer.getInstance().getPluginManager().registerCommand(KLBungeePlugin.instance, aliasCommand);
		}
	}
	
	public static boolean execute(CommandExecutor c, CommandSender cs, String label, String[] args) {
		if(cs instanceof ProxiedPlayer) {
			ProxiedPlayer pp = (ProxiedPlayer) cs;
			CachedPlayer cp = KLBungeePlugin.instance.getPlayerManager().getCachedPlayer(pp.getUniqueId());
			if(c.getLabel().equalsIgnoreCase(label)) {
				if(pp.hasPermission(c.getPermission())) {
					c.execute(pp, cp, label, args);
					return true;
				} else {
					Message.sendMessage(cp, MSG.PREFIX_ERROR, MSG.NO_PERM);
					return false;
				}
			} else
				for(String s : c.getAliases())
					if(s.equalsIgnoreCase(label)) {
						if(pp.hasPermission(c.getPermission())) {
							c.execute(pp, cp, label, args);
							return true;
						} else {
							Message.sendMessage(cp, MSG.PREFIX_ERROR, MSG.NO_PERM);
							return false;
						}
					}
			return false;
		}
		if(c.getLabel().equalsIgnoreCase(label)) {
			c.executeConsole(label, args);
			return true;
		} else
			for(String s : c.getAliases())
				if(s.equalsIgnoreCase(label)) {
					c.executeConsole(label, args);
					return true;
				}
		return false;
	}
}
