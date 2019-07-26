package lol.kangaroo.bungee.util;

import java.util.Locale;
import java.util.Set;

import lol.kangaroo.bungee.KLBungeePlugin;
import lol.kangaroo.bungee.player.PlayerManager;
import lol.kangaroo.common.player.BasePlayer;
import lol.kangaroo.common.player.CachedPlayer;
import lol.kangaroo.common.player.PlayerVariable;
import lol.kangaroo.common.util.MSG;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.HoverEvent.Action;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public class Message {
	
	private static ProxyServer proxy = ProxyServer.getInstance();
	
	private static PlayerManager pm = KLBungeePlugin.instance.getPlayerManager();
	
	/**
	 * Sends the given @MSG to the player if they are online, otherwise returns false.
	 * 
	 * This will send the MSG in the player's own language setting.
	 * @param p the Player to receive the message.
	 * @param msg the Unlocalized MSG to send.
	 * @param args the arguments given to the message.
	 * @return whether the message was sent.
	 */
	public static boolean sendMessage(BasePlayer p, MSG msg, Object... args) {
		ProxiedPlayer pp = proxy.getPlayer(p.getUniqueId());
		if(pp == null) return false;
		String m = msg.getMessage(p, args);
		for(String ms : m.split("\n")) {
			TextComponent tc = new TextComponent(TextComponent.fromLegacyText(ms));
			pp.sendMessage(tc);
		}
		return true;
	}
	
	/**
	 * Sends the given @MSG to the player if they are online, otherwise returns false.
	 * 
	 * This will send the MSG in the player's own language setting.
	 * @param p the Player to receive the message.
	 * @param msg the Unlocalized MSG to send.
	 * @param args the arguments given to the message.
	 * @return whether the message was sent.
	 */
	public static boolean sendMessage(ProxiedPlayer pp, MSG msg, Object... args) {
		CachedPlayer cp = pm.getCachedPlayer(pp.getUniqueId());
		if(pp == null || cp == null) return false;
		String m = msg.getMessage(cp, args);
		for(String ms : m.split("\n")) {
			TextComponent tc = new TextComponent(TextComponent.fromLegacyText(ms));
			pp.sendMessage(tc);
		}
		return true;
	}
	
	/**
	 * Sends the given @MSG with the prefix to the player if they are online, otherwise returns false.
	 * 
	 * This will send the MSG in the player's own language setting.
	 * @param p the Player to receive the message.
	 * @param prefix the Prefixed part of the message, cannot have arguments.
	 * @param msg the Unlocalized MSG to send.
	 * @param args the arguments given to the message.
	 * @return whether the message was sent.
	 */
	public static boolean sendMessage(BasePlayer p, MSG prefix, MSG msg, Object... args) {
		ProxiedPlayer pp = proxy.getPlayer(p.getUniqueId());
		if(pp == null) return false;
		String m = msg.getMessage(p, args);
		String[] ms = m.split("\n");
		TextComponent prefixtc = new TextComponent(TextComponent.fromLegacyText(prefix.getMessage(p, args)));
		TextComponent tc = new TextComponent(TextComponent.fromLegacyText(ms[0]));
		prefixtc.addExtra(tc);
		pp.sendMessage(prefixtc);
		if(ms.length > 1)
			for(int i = 1; i < ms.length; i++) {
				TextComponent mc = new TextComponent(TextComponent.fromLegacyText(ms[i]));
				pp.sendMessage(mc);
			}
		return true;
	}
	
	/**
	 * Sends the given @MSG with the prefix to the player if they are online, otherwise returns false.
	 * 
	 * This will send the MSG in the player's own language setting.
	 * @param p the Player to receive the message.
	 * @param prefix the Prefixed part of the message, cannot have arguments.
	 * @param msg the Unlocalized MSG to send.
	 * @param args the arguments given to the message.
	 * @return whether the message was sent.
	 */
	public static boolean sendMessage(ProxiedPlayer pp, MSG prefix, MSG msg, Object... args) {
		CachedPlayer cp = pm.getCachedPlayer(pp.getUniqueId());
		if(pp == null || cp == null) return false;
		String m = msg.getMessage(cp, args);
		String[] ms = m.split("\n");
		TextComponent prefixtc = new TextComponent(TextComponent.fromLegacyText(prefix.getMessage(cp, args)));
		TextComponent tc = new TextComponent(TextComponent.fromLegacyText(ms[0]));
		prefixtc.addExtra(tc);
		pp.sendMessage(prefixtc);
		if(ms.length > 1)
			for(int i = 1; i < ms.length; i++) {
				TextComponent mc = new TextComponent(TextComponent.fromLegacyText(ms[i]));
				pp.sendMessage(mc);
			}
		return true;
	}
	
	/**
	 * Send the prefixed string message.
	 * @return whether the message was sent.
	 */
	public static boolean sendPrefixedMessage(BasePlayer p, MSG prefix, String msg, Object... args) {
		ProxiedPlayer pp = proxy.getPlayer(p.getUniqueId());
		if(pp == null) return false;
		String m = String.format(msg, args);
		String[] ms = m.split("\n");
		TextComponent prefixtc = new TextComponent(TextComponent.fromLegacyText(prefix.getMessage(p, args)));
		TextComponent tc = new TextComponent(TextComponent.fromLegacyText(ms[0]));
		prefixtc.addExtra(tc);
		pp.sendMessage(prefixtc);
		if(ms.length > 1)
			for(int i = 1; i < ms.length; i++) {
				TextComponent mc = new TextComponent(TextComponent.fromLegacyText(ms[i]));
				pp.sendMessage(mc);
			}
		return true;
	}

	/**
	 * Send the prefixed string message.
	 * @return whether the message was sent.
	 */
	public static boolean sendPrefixedMessage(ProxiedPlayer pp, MSG prefix, String msg, Object... args) {
		CachedPlayer cp = pm.getCachedPlayer(pp.getUniqueId());
		if(cp == null) return false;
		String m = String.format(msg, args);
		String[] ms = m.split("\n");
		TextComponent prefixtc = new TextComponent(TextComponent.fromLegacyText(prefix.getMessage(cp, args)));
		TextComponent tc = new TextComponent(TextComponent.fromLegacyText(ms[0]));
		prefixtc.addExtra(tc);
		pp.sendMessage(prefixtc);
		if(ms.length > 1)
			for(int i = 1; i < ms.length; i++) {
				TextComponent mc = new TextComponent(TextComponent.fromLegacyText(ms[i]));
				pp.sendMessage(mc);
			}
		return true;
	}
	
	/**
	 * Sends the given @MSG with the prefix to the console.
	 * @param prefix the Prefix for the message
	 * @param msg the main message
	 * @param args arguments to the message
	 */
	public static void sendConsole(MSG prefix, MSG msg, Object... args) {
		CommandSender cs = proxy.getConsole();
		String m = msg.getMessage(Locale.getDefault(), args);
		String[] ms = m.split("\n");
		TextComponent prefixtc = new TextComponent(TextComponent.fromLegacyText(prefix.getMessage(Locale.getDefault(), args)));
		TextComponent tc = new TextComponent(TextComponent.fromLegacyText(ms[0]));
		prefixtc.addExtra(tc);
		cs.sendMessage(prefixtc);
		if(ms.length > 1)
			for(int i = 1; i < ms.length; i++) {
				TextComponent mc = new TextComponent(TextComponent.fromLegacyText(ms[i]));
				cs.sendMessage(mc);
			}
	}
	
	/**
	 * Sends the given @MSG to the console.
	 * @param msg the main message
	 * @param args arguments to the message
	 */
	public static void sendConsole(MSG msg, Object... args) {
		CommandSender cs = proxy.getConsole();
		String m = msg.getMessage(Locale.getDefault(), args);
		for(String ms : m.split("\n")) {
			TextComponent tc = new TextComponent(TextComponent.fromLegacyText(ms));
			cs.sendMessage(tc);
		}
	}
	
	/**
	 * Sends the given @MSG to the console.
	 * @param msg the main message
	 * @param args arguments to the message
	 */
	public static void sendConsole(String msg) {
		CommandSender cs = proxy.getConsole();
		String m = MSG.format(msg);
		for(String ms : m.split("\n")) {
			TextComponent tc = new TextComponent(TextComponent.fromLegacyText(ms));
			cs.sendMessage(tc);
		}
	}
	
	/**
	 * Sends the given @MSG to the player if they are online, otherwise returns false.
	 * 
	 * This will send the MSG in the default language,
	 * however a hover message translation will be sent in the player's own language setting.
	 * @param p the Player to receive the message.
	 * @param msg the Unlocalized MSG to send.
	 * @param args the arguments given to the message.
	 * @return whether the message was sent.
	 */
	public static boolean sendMessageForceLanguage(BasePlayer p, MSG msg, Object... args) {
		ProxiedPlayer pp = proxy.getPlayer(p.getUniqueId());
		if(pp == null) return false;

		String lang = (String) p.getVariable(PlayerVariable.LANGUAGE);
		Locale playerLocale = MSG.getLocale(lang);
		String[] ms = msg.getMessage(Locale.getDefault(), args).split("\n");
		for(int i = 0; i < ms.length - 1; i++) {
			TextComponent mc = new TextComponent(TextComponent.fromLegacyText(ms[i]));
			pp.sendMessage(mc);
		}
		TextComponent tc = new TextComponent(TextComponent.fromLegacyText(ms[ms.length-1]));
		String u = msg.getMessage(playerLocale, args).replace('\n', ' ');
		
		// Player's language not same as default.
		if(!playerLocale.equals(Locale.getDefault())) {
			TextComponent hoverMark = new TextComponent("[?]");
			TextComponent hoverMessage = new TextComponent("Translation: ");
			hoverMessage.setColor(ChatColor.YELLOW);
			TextComponent translatedMessage = new TextComponent(u);
			translatedMessage.setColor(ChatColor.GRAY);
			TextComponent[] hover = new TextComponent[] { hoverMessage, translatedMessage };
			HoverEvent he = new HoverEvent(Action.SHOW_TEXT, hover);
			hoverMark.setColor(ChatColor.GRAY);
			hoverMark.setHoverEvent(he);
			tc.addExtra(hoverMark);
		}
		
		pp.sendMessage(tc);
		return true;
	}
	
	/**
	 * Sends the given @MSG to the player if they are online, otherwise returns false.
	 * 
	 * This will send the MSG in the default language,
	 * however a hover message translation will be sent in the player's own language setting.
	 * @param p the Player to receive the message.
	 * @param msg the Unlocalized MSG to send.
	 * @param args the arguments given to the message.
	 * @return whether the message was sent.
	 */
	public static boolean sendMessageForceLanguage(ProxiedPlayer pp, MSG msg, Object... args) {
		CachedPlayer cp = pm.getCachedPlayer(pp.getUniqueId());
		if(pp == null || cp == null) return false;
		
		String lang = (String) cp.getVariable(PlayerVariable.LANGUAGE);
		Locale playerLocale = MSG.getLocale(lang);
		String[] ms = msg.getMessage(Locale.getDefault(), args).split("\n");
		for(int i = 0; i < ms.length - 1; i++) {
			TextComponent mc = new TextComponent(TextComponent.fromLegacyText(ms[i]));
			pp.sendMessage(mc);
		}
		TextComponent tc = new TextComponent(TextComponent.fromLegacyText(ms[ms.length-1]));
		String u = msg.getMessage(playerLocale, args).replace('\n', ' ');
		
		// Player's language not same as default.
		if(!playerLocale.equals(Locale.getDefault())) {
			TextComponent hoverMark = new TextComponent("[?]");
			TextComponent hoverMessage = new TextComponent("Translation: ");
			hoverMessage.setColor(ChatColor.YELLOW);
			TextComponent translatedMessage = new TextComponent(u);
			translatedMessage.setColor(ChatColor.GRAY);
			TextComponent[] hover = new TextComponent[] { hoverMessage, translatedMessage };
			HoverEvent he = new HoverEvent(Action.SHOW_TEXT, hover);
			hoverMark.setColor(ChatColor.GRAY);
			hoverMark.setHoverEvent(he);
			tc.addExtra(hoverMark);
		}
		
		pp.sendMessage(tc);
		return true;
	}
	
	/**
	 * Sends the given string to the player if they are online, otherwise returns false..
	 * 
	 * This will send the MSG to the player without translation,
	 * however a rough automatic translation will be given in a hover message.
	 * @param msg the Unlocalized MSG to send.
	 * @param args the arguments given to the message.
	 */
	public static boolean sendMessage(BasePlayer p, String msg, Object... args) {
		String f = MSG.format(msg, args);
		String u = ChatColor.stripColor(f);
		ProxiedPlayer pp = proxy.getPlayer(p.getUniqueId());
		if(pp == null) return false;
		String lang = (String) p.getVariable(PlayerVariable.LANGUAGE);
		Locale playerLocale = MSG.getLocale(lang);
		TextComponent tc = new TextComponent(TextComponent.fromLegacyText(f));
		
		proxy.getScheduler().runAsync(KLBungeePlugin.instance, () -> {
			// Player's language not same as default.
			if(!playerLocale.equals(Locale.getDefault())) {
				TextComponent hoverMark = new TextComponent("[?]");
				String translated = MSG.getInternetTranslation(u, playerLocale);
				TextComponent hoverMessage = new TextComponent("Google Translate: ");
				hoverMessage.setColor(ChatColor.YELLOW);
				TextComponent translatedMessage = new TextComponent(translated);
				translatedMessage.setColor(ChatColor.GRAY);
				TextComponent[] hover = new TextComponent[] { hoverMessage, translatedMessage };
				HoverEvent he = new HoverEvent(Action.SHOW_TEXT, hover);
				hoverMark.setColor(ChatColor.GRAY);
				hoverMark.setHoverEvent(he);
				tc.addExtra(hoverMark);
			}
			
			pp.sendMessage(tc);
		});
		return true;
	}
	
	public static boolean sendMessage(ProxiedPlayer pp, String msg, Object... args) {
		String f = MSG.format(msg, args);
		String u = ChatColor.stripColor(f);
		CachedPlayer cp = pm.getCachedPlayer(pp.getUniqueId());
		if(pp == null || cp == null) return false;
		String lang = (String) cp.getVariable(PlayerVariable.LANGUAGE);
		Locale playerLocale = MSG.getLocale(lang);
		TextComponent tc = new TextComponent(TextComponent.fromLegacyText(f));
		
		proxy.getScheduler().runAsync(KLBungeePlugin.instance, () -> {
			// Player's language not same as default.
			if(!playerLocale.equals(Locale.getDefault())) {
				TextComponent hoverMark = new TextComponent("[?]");
				String translated = MSG.getInternetTranslation(u, playerLocale);
				TextComponent hoverMessage = new TextComponent("Google Translate: ");
				hoverMessage.setColor(ChatColor.YELLOW);
				TextComponent translatedMessage = new TextComponent(translated);
				translatedMessage.setColor(ChatColor.GRAY);
				TextComponent[] hover = new TextComponent[] { hoverMessage, translatedMessage };
				HoverEvent he = new HoverEvent(Action.SHOW_TEXT, hover);
				hoverMark.setColor(ChatColor.GRAY);
				hoverMark.setHoverEvent(he);
				tc.addExtra(hoverMark);
			}
			
			pp.sendMessage(tc);
		});
		return true;
	}
	
	/**
	 * Sends the given @MSG to all online players.
	 * 
	 * This will send the MSG to all players in their own language setting.
	 * @param msg the Unlocalized MSG to send.
	 * @param args the arguments given to the message.
	 */
	public static void broadcast(MSG msg, Object... args) {
		for(ProxiedPlayer pp : proxy.getPlayers()) {
			CachedPlayer cp = pm.getCachedPlayer(pp.getUniqueId());
			if(cp == null) continue;
			TextComponent tc = new TextComponent(TextComponent.fromLegacyText(msg.getMessage(cp, args)));
			pp.sendMessage(tc);
		}
	}
	
	/**
	 * Sends the given @MSG to the given players.
	 * 
	 * This will send the MSG to all players in the set in their own language setting.
	 * @param msg the Unlocalized MSG to send.
	 * @param players the Players to broadcast to.
	 * @param args the arguments given to the message.
	 */
	public static void broadcast(Set<BasePlayer> players, MSG msg, Object... args) {
		for(BasePlayer p : players) {
			ProxiedPlayer pp = proxy.getPlayer(p.getUniqueId());
			if(pp == null) continue;
			TextComponent tc = new TextComponent(TextComponent.fromLegacyText(msg.getMessage(p, args)));
			pp.sendMessage(tc);
		}
	}
	
	/**
	 * Sends the given string to all online players.
	 * 
	 * This will send the MSG to all players without translation,
	 * however a rough automatic translation will be given in a hover message.
	 * @param msg the Unlocalized MSG to send.
	 * @param args the arguments given to the message.
	 */
	public static void broadcast(String msg, Object... args) {
		String f = MSG.format(msg, args);
		String u = ChatColor.stripColor(f);
		for(ProxiedPlayer pp : proxy.getPlayers()) {
			CachedPlayer cp = pm.getCachedPlayer(pp.getUniqueId());
			if(cp == null) continue;
			String lang = (String) cp.getVariable(PlayerVariable.LANGUAGE);
			Locale playerLocale = MSG.getLocale(lang);
			TextComponent tc = new TextComponent(TextComponent.fromLegacyText(f));
			
			proxy.getScheduler().runAsync(KLBungeePlugin.instance, () -> {
				// Player's language not same as default.
				if(!playerLocale.equals(Locale.getDefault())) {
					TextComponent hoverMark = new TextComponent("[?]");
					String translated = MSG.getInternetTranslation(u, playerLocale);
					TextComponent hoverMessage = new TextComponent("Google Translate: ");
					hoverMessage.setColor(ChatColor.YELLOW);
					TextComponent translatedMessage = new TextComponent(translated);
					translatedMessage.setColor(ChatColor.GRAY);
					TextComponent[] hover = new TextComponent[] { hoverMessage, translatedMessage };
					HoverEvent he = new HoverEvent(Action.SHOW_TEXT, hover);
					hoverMark.setColor(ChatColor.GRAY);
					hoverMark.setHoverEvent(he);
					tc.addExtra(hoverMark);
				}
				
				pp.sendMessage(tc);
			});
		}
	}
	
}
