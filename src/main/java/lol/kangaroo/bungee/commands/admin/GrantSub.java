package lol.kangaroo.bungee.commands.admin;

import lol.kangaroo.bungee.commands.AdminCommand;
import lol.kangaroo.bungee.commands.Subcommand;
import lol.kangaroo.bungee.player.PlayerManager;
import lol.kangaroo.common.permissions.Rank;
import lol.kangaroo.common.player.BasePlayer;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public class GrantSub extends Subcommand {

	
	public GrantSub(PlayerManager pm, ProxyServer proxy, AdminCommand parent) {
		super(pm, proxy, parent, "grant", Rank.ADMIN_DEV.getPerm(), "gr");
	}

	@Override
	public void execute(ProxiedPlayer sender, BasePlayer bp, String label, String[] args) {
		// TODO authenticate first
		
	}

	@Override
	public void executeConsole(String label, String[] args) {
		// TODO authenticate still
	}
	
	
}
