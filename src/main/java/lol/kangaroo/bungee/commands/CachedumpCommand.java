package lol.kangaroo.bungee.commands;

import java.util.Map.Entry;
import java.util.UUID;

import lol.kangaroo.bungee.KLBungeePlugin;
import lol.kangaroo.bungee.player.PlayerManager;
import lol.kangaroo.bungee.util.Message;
import lol.kangaroo.common.permissions.Rank;
import lol.kangaroo.common.player.BasePlayer;
import lol.kangaroo.common.player.CachedPlayer;
import lol.kangaroo.common.player.PlayerVariable;
import lol.kangaroo.common.util.MSG;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public class CachedumpCommand extends CommandExecutor {

	private KLBungeePlugin pl;
	
	public CachedumpCommand(PlayerManager pm, ProxyServer proxy, KLBungeePlugin pl) {
		super(pm, proxy, "cachedump", Rank.OWNER.getPerm(), "cachedumpall");
		this.pl = pl;
	}

	@Override
	public void execute(ProxiedPlayer sender, BasePlayer bp, String label, String[] args) {
		Message.sendPrefixedMessage(bp, MSG.PREFIX_ADMIN, "Dumping " + (label.equalsIgnoreCase("cachedumpall") ? "Advanced" : "Basic") + " Cache Data to Console...");
		if(label.equalsIgnoreCase("cachedumpall")) {
			System.out.println("------------Player Cache-------------");
			for(CachedPlayer cp : pl.getPlayerCacheManager().getPlayerCache()) {
				String vars = "";
				for(Entry<PlayerVariable, Object> ve : cp.getAllVariablesMap().entrySet())
					vars += "[" + ve.getKey().name() + ";" + ve.getValue() + "] ";
				System.out.println(cp.getUniqueId() + " | " + vars);
			}
		} else {
			System.out.println("------------Player Cache-------------");
			for(CachedPlayer cp : pl.getPlayerCacheManager().getPlayerCache())
				System.out.println(cp.getUniqueId() + " | " + cp.getVariable(PlayerVariable.USERNAME));
		}
		System.out.println("------------UUID Cache-------------");
		for(UUID u : pl.getPlayerCacheManager().getUUIDCache().keySet())
			System.out.println(u + " | " + pl.getPlayerCacheManager().getUUIDCache().get(u).getVariable(PlayerVariable.USERNAME));
		
	}

	@Override
	public void executeConsole(String label, String[] args) {
		Message.sendConsole("Dumping " + (label.equalsIgnoreCase("cachedumpall") ? "Advanced" : "Basic") + " Cache Data to Console...");
		if(label.equalsIgnoreCase("cachedumpall")) {
			System.out.println("------------Player Cache-------------");
			for(CachedPlayer cp : pl.getPlayerCacheManager().getPlayerCache()) {
				String vars = "";
				for(Entry<PlayerVariable, Object> ve : cp.getAllVariablesMap().entrySet())
					vars += "[" + ve.getKey().name() + ";" + ve.getValue() + "] ";
				System.out.println(cp.getUniqueId() + " | " + vars);
			}
		} else {
			System.out.println("------------Player Cache-------------");
			for(CachedPlayer cp : pl.getPlayerCacheManager().getPlayerCache())
				System.out.println(cp.getUniqueId() + " | " + cp.getVariable(PlayerVariable.USERNAME));
		}
		System.out.println("------------UUID Cache-------------");
		for(UUID u : pl.getPlayerCacheManager().getUUIDCache().keySet())
			System.out.println(u + " | " + pl.getPlayerCacheManager().getUUIDCache().get(u).getVariable(PlayerVariable.USERNAME));
		
	}
	
}
