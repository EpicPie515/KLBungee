package lol.kangaroo.bungee.util;

import lol.kangaroo.bungee.KLBungeePlugin;

public class ThreadManager {
	
	public static void async(Runnable run) {
		KLBungeePlugin pl = KLBungeePlugin.instance;
		pl.getProxy().getScheduler().runAsync(pl, run);
	}
	
}
