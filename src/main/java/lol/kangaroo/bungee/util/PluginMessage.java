package lol.kangaroo.bungee.util;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.UUID;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;

import lol.kangaroo.bungee.KLBungeePlugin;
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
	
	public static class MessageWrapper {
		
		public ByteArrayOutputStream b;
		public DataOutputStream out;
		
		public MessageWrapper(String subchannel) {
			this.b = new ByteArrayOutputStream();
			this.out = new DataOutputStream(b);
			try {
				out.writeUTF(subchannel);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		public MessageWrapper writeInt(int i) {
			try {
				out.writeInt(i);
			} catch (IOException e) {
				e.printStackTrace();
			}
			return this;
		}
		
		public MessageWrapper writeUTF(String s) {
			try {
				out.writeUTF(s);
			} catch (IOException e) {
				e.printStackTrace();
			}
			return this;
		}
		
		public MessageWrapper writeChar(char c) {
			try {
				out.writeChar(c);
			} catch (IOException e) {
				e.printStackTrace();
			}
			return this;
		}
		
		public MessageWrapper writeUuid(UUID u) {
			try {
				out.writeLong(u.getLeastSignificantBits());
				out.writeLong(u.getMostSignificantBits());
			} catch (IOException e) {
				e.printStackTrace();
			}
			return this;
		}
		
		void close() {
			try {
				out.close();
			} catch(IOException e) {
				e.printStackTrace();
			}
		}
		
	}
	
	@EventHandler
	public void onPluginMessage(PluginMessageEvent e) {
		if(e.getTag().equals("CommandAction")) {
			ByteArrayDataInput in = ByteStreams.newDataInput(e.getData());
			String subchannel = in.readUTF();
			ProxiedPlayer p = (ProxiedPlayer) e.getReceiver();
			String args = in.readUTF();
			ps.getPluginManager().dispatchCommand(p, subchannel + args);
		}
	}
}
