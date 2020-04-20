package lol.kangaroo.bungee.commands;

import java.sql.Timestamp;
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
		super(pm, proxy, "cachedump", Rank.ADMIN_DEV.getPerm(), "cachedumpall", "cachedumptiming");
		this.pl = pl;
	}

	@Override
	public void execute(ProxiedPlayer sender, BasePlayer bp, String label, String[] args) {
		Message.sendPrefixedMessage(bp, MSG.PREFIX_ADMIN, "Dumping " + (label.equalsIgnoreCase("cachedumpall") ? "Advanced" : "Basic") + " Cache Data to Console...");
		if(label.equalsIgnoreCase("cachedumpall")) {
			System.out.println("------------Player Cache Dump-------------");
			for(CachedPlayer cp : pl.getPlayerCacheManager().getPlayerCache()) {
				String vars = "";
				for(Entry<PlayerVariable, Object> ve : cp.getAllVariablesMap().entrySet())
					vars += "[" + ve.getKey().name() + ";" + ve.getValue() + "] ";
				System.out.println(cp.getUniqueId() + " | " + vars);
			}
			System.out.println("------------End of Cache Dump-------------");
		} else if(label.equalsIgnoreCase("cachedumptiming")) {
			System.out.println("------------Timing Cache Dump-------------");
			System.out.println("Pull Update: " + pl.getPlayerCacheManager().getMillisUntilPull() + "ms");
			System.out.println("Flush Update: " + pl.getPlayerCacheManager().getMillisUntilFlush() + "ms");
			System.out.println("------------Player Cache Dump-------------");
			System.out.println("** Removed on Flush marked by [REM-NXT] **");
			for(CachedPlayer cp : pl.getPlayerCacheManager().getPlayerCache()) {
				ProxiedPlayer pp = pl.getProxy().getPlayer(cp.getUniqueId());
				if(pp == null)
					System.out.print("[REM-NXT] ");
				
				System.out.print(cp.getUniqueId() + " | " + cp.getVariable(PlayerVariable.USERNAME));
				
				if(pp == null)
					System.out.printf(" | [Last Seen: " + ((System.currentTimeMillis() - ((Timestamp)cp.getVariable(PlayerVariable.LASTQUIT)).getTime())/1000) + "s]");
				
				System.out.println();
					
			}
			System.out.println("------------End of Cache Dump-------------");
		} else {
			System.out.println("------------Player Cache Dump-------------");
			for(CachedPlayer cp : pl.getPlayerCacheManager().getPlayerCache())
				System.out.println(cp.getUniqueId() + " | " + cp.getVariable(PlayerVariable.USERNAME));
			System.out.println("------------End of Cache Dump-------------");
		}
		
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
