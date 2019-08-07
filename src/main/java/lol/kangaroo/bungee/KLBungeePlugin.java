package lol.kangaroo.bungee;

import java.util.List;
import java.util.Locale;

import lol.kangaroo.bungee.commands.AdminCommand;
import lol.kangaroo.bungee.commands.CachedumpCommand;
import lol.kangaroo.bungee.commands.CommandExecutor;
import lol.kangaroo.bungee.commands.player.LinksCommand;
import lol.kangaroo.bungee.commands.player.PingCommand;
import lol.kangaroo.bungee.commands.punish.BanCommand;
import lol.kangaroo.bungee.commands.punish.BlacklistCommand;
import lol.kangaroo.bungee.commands.punish.MuteCommand;
import lol.kangaroo.bungee.commands.punish.UnbanCommand;
import lol.kangaroo.bungee.commands.punish.UnblacklistCommand;
import lol.kangaroo.bungee.commands.punish.UnmuteCommand;
import lol.kangaroo.bungee.config.ConfigManager;
import lol.kangaroo.bungee.database.Auth;
import lol.kangaroo.bungee.database.DatabaseInitializer;
import lol.kangaroo.bungee.database.Logs;
import lol.kangaroo.bungee.listeners.AdminJoinAlertListener;
import lol.kangaroo.bungee.listeners.BanListener;
import lol.kangaroo.bungee.listeners.BlacklistListener;
import lol.kangaroo.bungee.listeners.MuteListener;
import lol.kangaroo.bungee.listeners.PermissionListener;
import lol.kangaroo.bungee.listeners.PlayerDatabaseListener;
import lol.kangaroo.bungee.listeners.VoteListener;
import lol.kangaroo.bungee.permissions.RankManager;
import lol.kangaroo.bungee.permissions.RankPermissionExpiry;
import lol.kangaroo.bungee.player.Money;
import lol.kangaroo.bungee.player.PlayerCacheManager;
import lol.kangaroo.bungee.player.PlayerManager;
import lol.kangaroo.bungee.player.PlayerVariableManager;
import lol.kangaroo.bungee.player.punish.PunishManager;
import lol.kangaroo.bungee.util.PluginMessage;
import lol.kangaroo.common.KLCommon;
import lol.kangaroo.common.database.DatabaseManager;
import lol.kangaroo.common.database.Setting;
import lol.kangaroo.common.permissions.PermissionManager;
import lol.kangaroo.common.player.PlayerHistory;
import lol.kangaroo.common.util.I18N;
import lol.kangaroo.common.util.MSG;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.api.plugin.PluginManager;
import net.md_5.bungee.config.Configuration;

public class KLBungeePlugin extends Plugin implements KLCommon {
	
	public static KLBungeePlugin instance;
	
	private ConfigManager configManager;
	
	private DatabaseManager db;
	
	private I18N i18n;
	
	private PlayerVariableManager pvm;
	private PlayerCacheManager pcm;
	private PunishManager pum;
	private PlayerManager pm;
	private PermissionManager prm;
	private VoteListener vol;
	
	private RankManager rm;
	
	// Listeners
	private PlayerDatabaseListener pdl;
	private PermissionListener prl;
	private AdminJoinAlertListener ajal;
	
	private BanListener bal;
	private BlacklistListener bll;
	private MuteListener mul;
	
	// Loop Tasks
	private RankPermissionExpiry expireLoopTask;
	
	@Override
	public void onEnable() {
		Locale.setDefault(new Locale("en", "US"));
		
		instance = this;
		init();
		setupLanguages();
		registerListeners();
		registerLoopTasks();
		
	}
	
	private void init() {
		configManager = new ConfigManager(getDataFolder());
		
		Configuration settings = configManager.getConfig("settings");
		Configuration dbSettings = settings.getSection("db");
		db = new DatabaseManager(dbSettings.getString("user"), dbSettings.getString("pass"), dbSettings.getString("db"), dbSettings.getString("host"), dbSettings.getInt("port"));
		
		DatabaseInitializer di = new DatabaseInitializer(db);
		di.createPlayerTables();
		di.createLogTables();
		
		Setting.init(db);
		Logs.init(db);
		Auth.init(db);
		
		long pullUpdateInterval = settings.getLong("cacheUpdateInterval");
		long flushInterval = settings.getLong("cacheFlushInterval");
		
		pvm = new PlayerVariableManager(db);
		pcm = new PlayerCacheManager(this, pullUpdateInterval, flushInterval);
		pum = new PunishManager(db);
		prm = new PermissionManager(db);
		pm = new PlayerManager(db, getProxy(), this, pvm, pcm, pum, prm);
		rm = new RankManager(pm);
		PlayerHistory.init(db);
		
		PluginMessage.init(this);
		
		Money.init(pm);
		
		CommandExecutor.registerCommand(new BanCommand(pm, getProxy()));
		CommandExecutor.registerCommand(new BlacklistCommand(pm, getProxy()));
		CommandExecutor.registerCommand(new MuteCommand(pm, getProxy()));
		CommandExecutor.registerCommand(new UnbanCommand(pm, getProxy()));
		CommandExecutor.registerCommand(new UnblacklistCommand(pm, getProxy()));
		CommandExecutor.registerCommand(new UnmuteCommand(pm, getProxy()));
		CommandExecutor.registerCommand(new LinksCommand(pm, getProxy()));
		CommandExecutor.registerCommand(new PingCommand(pm, getProxy()));
		CommandExecutor.registerCommand(new AdminCommand(pm, getProxy()));
		CommandExecutor.registerCommand(new CachedumpCommand(pm, getProxy(), this));
		
		
		pcm.scheduleUpdateTasks(pm);
	}
	
	private void setupLanguages() {
		Configuration settings = configManager.getConfig("settings");
		List<String> langs = settings.getSection("languages").getStringList("locales");
		Locale[] locales = new Locale[langs.size()];
		for(int i = 0; i < langs.size(); i++) {
			String[] lc = langs.get(i).split("_");
			locales[i] = new Locale(lc[0], lc[1]);
		}
			
		MSG.init(i18n = new I18N(locales, this.getDataFolder(), this.getClass().getProtectionDomain().getCodeSource().getLocation()));
	}
	
	private void registerListeners() {
		
		PluginManager pluginManager = getProxy().getPluginManager();
		
		pluginManager.registerListener(this, pdl = new PlayerDatabaseListener(pm, this));
		pluginManager.registerListener(this, prl = new PermissionListener(pm, this));
		pluginManager.registerListener(this, ajal = new AdminJoinAlertListener(pm, this));
		pluginManager.registerListener(this, vol = new VoteListener(pm));

		pluginManager.registerListener(this, bal = new BanListener(this));
		pluginManager.registerListener(this, bll = new BlacklistListener(this));
		pluginManager.registerListener(this, mul = new MuteListener(this));
	}
	
	private void registerLoopTasks() {
		expireLoopTask = new RankPermissionExpiry(this);
		
		expireLoopTask.startLoopTask();
	}
	
	public ConfigManager getConfigManager() {
		return configManager;
	}
	
	public DatabaseManager getDatabaseManager() {
		return db;
	}
	
	public PlayerVariableManager getPlayerVariableManager() {
		return pvm;
	}
	
	public PlayerManager getPlayerManager() {
		return pm;
	}
	
	public PlayerCacheManager getPlayerCacheManager() {
		return pcm;
	}
	
	public PunishManager getPunishManager() {
		return pum;
	}
	
	public PermissionManager getPermissionManager() {
		return prm;
	}
	
	public RankManager getRankManager() {
		return rm;
	}
	
	public I18N getI18N() {
		return i18n;
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
	
	
	public PlayerDatabaseListener getPlayerDatabaseListener() {
		return pdl;
	}
	
	public AdminJoinAlertListener getAdminJoinAlertListener() {
		return ajal;
	}
	
	public PermissionListener getPermissionListener() {
		return prl;
	}
	
	public BanListener getBanListener() {
		return bal;
	}
	
	public BlacklistListener getBlacklistListener() {
		return bll;
	}
	
	public MuteListener getMuteListener() {
		return mul;
	}
	
	public VoteListener getVoteListener() {
		return vol;
	}
	
	@Override
	public void onDisable() {
		
	}
	
}
