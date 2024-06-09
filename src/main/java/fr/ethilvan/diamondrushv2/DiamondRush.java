package fr.ethilvan.diamondrushv2;

import fr.ethilvan.diamondrushv2.config.Config;
import fr.ethilvan.diamondrushv2.game.Game;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;

public class DiamondRush {

	private final DiamondRushV2 plugin;
	private final Config config;
	private FileConfiguration messagesConfig;
	private Game game = null;


	public DiamondRush(DiamondRushV2 plugin) {
		this.plugin = plugin;
		this.config = new Config(plugin);

		loadConfig();
		loadMessages();
	}


	public void loadConfig() {
		this.plugin.saveDefaultConfig();
		this.config.reload();
	}


	public DiamondRushV2 getPlugin() {
		return plugin;
	}


	public Config getConfig() {
		return config;
	}


	public FileConfiguration getMessagesConfig() {
		return messagesConfig;
	}


	public Game getGame() {
		return game;
	}

	public void setGame(Game game) {
		this.game = game;
	}


	public void loadMessages() {
		File file = new File(plugin.getDataFolder(), "messages.yml");
		if (!file.exists()) {
			plugin.saveResource("messages.yml", false);
		}
		messagesConfig = YamlConfiguration.loadConfiguration(file);
	}

}
