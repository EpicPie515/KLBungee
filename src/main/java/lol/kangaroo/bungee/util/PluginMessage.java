package lol.kangaroo.bungee.util;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;

import lol.kangaroo.bungee.KLBungeePlugin;
import lol.kangaroo.common.player.CachedPlayer;
import lol.kangaroo.common.player.PlayerVariable;
import lol.kangaroo.common.util.MSG;
import lol.kangaroo.common.util.MessageWrapper;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.connection.Server;
import net.md_5.bungee.api.event.PluginMessageEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class PluginMessage implements Listener {
	
	private static KLBungeePlugin plugin;
	private static ProxyServer ps;
	
	public static void init(KLBungeePlugin pl) {
		plugin = pl;
		ps = plugin.getProxy();
		ps.registerChannel("CommandAction");
		ps.registerChannel("CommandGUI");
		ps.registerChannel("AdminAlert");
		ps.getPluginManager().registerListener(plugin, new PluginMessage());
	}
	
	public static void sendToSpigot(ProxiedPlayer p, String channel, MessageWrapper m) {
		if(!ps.getChannels().contains(channel)) ps.registerChannel(channel);
		p.getServer().getInfo().sendData(channel, m.b.toByteArray());
		m.close();
	}
	
	public static void sendToSpigot(Server s, String channel, MessageWrapper m) {
		if(!ps.getChannels().contains(channel)) ps.registerChannel(channel);
		s.getInfo().sendData(channel, m.b.toByteArray());
		m.close();
	}
	
	public static void sendToSpigot(ServerInfo s, String channel, MessageWrapper m) {
		if(!ps.getChannels().contains(channel)) ps.registerChannel(channel);
		s.sendData(channel, m.b.toByteArray());
		m.close();
	}
	
	// TODO use encryption on plugin messages
	
	@EventHandler
	public void onPluginMessage(PluginMessageEvent e) {
		if(e.getTag().equals("CommandAction")) {
			ByteArrayDataInput in = ByteStreams.newDataInput(e.getData());
			String subchannel = in.readUTF();
			ProxiedPlayer p = (ProxiedPlayer) e.getReceiver();
			String args = in.readUTF();
			ps.getPluginManager().dispatchCommand(p, subchannel + args);
		}
		
		if(e.getTag().equals("AdminAlert")) {
			ByteArrayDataInput in = ByteStreams.newDataInput(e.getData());
			String subchannel = in.readUTF();
			ProxiedPlayer p = (ProxiedPlayer) e.getReceiver();
			CachedPlayer cp = plugin.getPlayerManager().getCachedPlayer(p.getUniqueId());
			if(subchannel.equals("ChatClear"))
				Message.broadcast(plugin.getPlayerManager().getNotifiableStaff(), MSG.ADMIN_CHAT_CLEAR, plugin.getRankManager().getPrefix(cp, false) + cp.getVariable(PlayerVariable.USERNAME), plugin.getServerManager().formatServerName(plugin.getServerManager().getServerID(p.getServer().getInfo().getName())));
		}
		
		if(!e.getTag().equals("BungeeCord") && ps.getChannels().contains(e.getTag())) e.setCancelled(true);
	}
}
