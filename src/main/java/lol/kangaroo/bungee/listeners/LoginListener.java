package lol.kangaroo.bungee.listeners;

import lol.kangaroo.bungee.KLBungeePlugin;
import lol.kangaroo.bungee.util.ThreadManager;
import net.md_5.bungee.api.event.LoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;

public class LoginListener implements Listener {

	private KLBungeePlugin pl;
	
	private BlacklistListener bll;
	private BanListener bal;
	private PlayerDatabaseListener pdl;
	
	public LoginListener(KLBungeePlugin pl, BlacklistListener bll, BanListener bal, PlayerDatabaseListener pdl) {
		this.bll = bll;
		this.bal = bal;
		this.pdl = pdl;
		this.pl = pl;
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onLogin(LoginEvent e) {
		e.registerIntent(pl);
		ThreadManager.async(() -> {
			// PDL MUST BE FIRST
			pdl.onLogin(e);
			
			bll.onLogin(e);
			bal.onLogin(e);
			e.completeIntent(pl);
		});
	}
	
}
