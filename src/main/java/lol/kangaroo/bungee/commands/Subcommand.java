package lol.kangaroo.bungee.commands;

import java.util.Arrays;
import java.util.List;

import lol.kangaroo.bungee.player.PlayerManager;
import lol.kangaroo.common.player.BasePlayer;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public abstract class Subcommand {
	
	protected PlayerManager pm;
	protected ProxyServer proxy;
	protected String label;
	protected String perm;
	protected CommandExecutor parent;
	protected List<String> aliases;
	
	public Subcommand(PlayerManager pm, ProxyServer proxy, CommandExecutor parent, String label, String perm, String... aliases) {
		this.pm = pm;
		this.proxy = proxy;
		this.label = label;
		this.perm = perm;
		this.parent = parent;
		this.aliases = Arrays.asList(aliases);
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
}
