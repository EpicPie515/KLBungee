package lol.kangaroo.bungee.servers;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lol.kangaroo.bungee.config.ConfigManager;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.config.Configuration;

public class ServerManager {
	
	private ConfigManager configManager;
	private ProxyServer proxy;
	
	public static final Pattern SERVER_NAME_PATTERN = Pattern.compile("^([a-z]{3,5})([0-9]{2})");
	
	public ServerManager(ConfigManager configManager, ProxyServer proxy) {
		this.configManager = configManager;
		this.proxy = proxy;
	}
	
	public int getServerID(String name) {
		Configuration servers = configManager.getConfig("settings").getSection("server-index");
		for(String s : servers.getKeys()) {
			if(servers.getString(s).equalsIgnoreCase(name)) {
				return Integer.valueOf(s);
			}
		}
		if(name.equalsIgnoreCase("network")) return 0;
		throw new RuntimeException("invalid server name, server cannot proceed, so how about you don't fuck with server names");
	}
	
	public String getServerName(int id) {
		Configuration servers = configManager.getConfig("settings").getSection("server-index");
		for(String s : servers.getKeys()) {
			if(s.equals(id + "")) {
				return servers.getString(s);
			}
		}
		if(id == 0) return "network";
		throw new RuntimeException("invalid server id, server cannot proceed, so how about you don't fuck with server ids");
	}
	
	public ServerInfo getServerInfo(int id) {
		return proxy.getServerInfo(getServerName(id));
	}
	
	public ServerType getServerType(int id) {
		return ServerType.getFromID(id);
	}
	
	public boolean isGameServer(int id) {
		switch(getServerType(id)) {
		case HUB:
		case UNKNOWN:
			return false;
		default:
			return true;
		}
	}
	
	public boolean isHub(int id) {
		return getServerType(id).equals(ServerType.HUB);
	}
	
	public int findAvailableHub() {
		// TODO find a hub (balanced and checked for online)
		return 1;
	}
	
	public String formatServerName(int id) {
		if(id == 0) return "Network";
		String srvName = getServerName(id);
		Matcher m = SERVER_NAME_PATTERN.matcher(srvName);
		if(!m.matches()) throw new RuntimeException("Server name does not match pattern: " + srvName + " (id: " + id + ")");
		String fm = m.group(1).toUpperCase() + "-" + m.group(2);
		return fm;
	}
	
}
