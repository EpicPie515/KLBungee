package lol.kangaroo.bungee.config;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

public class ConfigManager {
	
	private File dataFolder;
	private ConfigurationProvider yamlProvider;
	
	private Map<String, File> configFiles = new HashMap<>();
	
	public ConfigManager(File dataFolder) {
		this.dataFolder = dataFolder;
		yamlProvider = ConfigurationProvider.getProvider(YamlConfiguration.class);
	}
	
	public Configuration getConfig(String name) {
		if(configFiles.containsKey(name)) {
			try {
				return yamlProvider.load(configFiles.get(name));
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			File f = new File(dataFolder, name + ".yml");
			configFiles.put(name, f);
			try {
				return yamlProvider.load(f);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return null;
	}
	
}
