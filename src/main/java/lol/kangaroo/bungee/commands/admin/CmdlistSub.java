package lol.kangaroo.bungee.commands.admin;

import java.util.Collection;

import lol.kangaroo.bungee.commands.AdminCommand;
import lol.kangaroo.bungee.commands.Subcommand;
import lol.kangaroo.bungee.player.PlayerManager;
import lol.kangaroo.bungee.util.Message;
import lol.kangaroo.common.permissions.Rank;
import lol.kangaroo.common.player.BasePlayer;
import lol.kangaroo.common.util.MSG;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public class CmdlistSub extends Subcommand {

	private Collection<Subcommand> subs;
	
	public CmdlistSub(PlayerManager pm, ProxyServer proxy, AdminCommand parent, Collection<Subcommand> collection) {
		super(pm, proxy, parent, "cmdlist", Rank.SRMOD.getPerm(), "cmds", "help");
		this.subs = collection;
	}

	@Override
	public void execute(ProxiedPlayer sender, BasePlayer bp, String label, String[] args) {
		String[] lines = new String[(subs.size() / 5) + 1];
		Subcommand[] s = subs.toArray(new Subcommand[subs.size()]);
		int l = 0;
		int wi = 0;
		for(int i = 0; i < s.length; i++) {
			if(sender.hasPermission(s[i].getPermission()))
				if(++wi % 5 == 0) {
					lines[l] = "&c" + s[i].getLabel();
					l++;
				} else lines[l] = "&c" + s[i].getLabel() + "&f | ";
		}
		Message.sendMessage(bp, MSG.COMMAND_ADMIN_CMDLIST);
		for(String lin : lines)
			Message.sendMessage(bp, lin);
		
	}

	@Override
	public void executeConsole(String label, String[] args) {
		String[] lines = new String[(subs.size() / 5) + 1];
		Subcommand[] s = subs.toArray(new Subcommand[subs.size()]);
		int l = 0;
		for(int i = 0; i < s.length; i++) {
			if(l % 5 == 0) {
				lines[l] = "&c" + s[i].getLabel();
				l++;
			} else lines[l] = "&c" + s[i].getLabel() + "&f | ";
		}
		Message.sendConsole(MSG.COMMAND_ADMIN_CMDLIST);
		for(String lin : lines)
			Message.sendConsole(lin);
	}
	
	
}
