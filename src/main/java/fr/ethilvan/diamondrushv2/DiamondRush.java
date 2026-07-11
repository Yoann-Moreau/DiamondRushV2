package fr.ethilvan.diamondrushv2;

import fr.ethilvan.diamondrushv2.config.Config;
import fr.ethilvan.diamondrushv2.game.Game;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;


public class DiamondRush {

	private final DiamondRushV2 plugin;
	private final Config config;
	private FileConfiguration messagesConfig;
	private Game game = null;
	private final Scoreboard scoreboard;


	public DiamondRush(DiamondRushV2 plugin) {
		this.plugin = plugin;
		this.config = new Config(plugin);
		this.scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();

		loadConfig();
		loadMessages();
	}


	public void loadConfig() {
		this.plugin.saveDefaultConfig();
		this.config.load();
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


	public Scoreboard getScoreboard() {
		return scoreboard;
	}


	public void loadMessages() {
		File file = new File(plugin.getDataFolder(), "messages.yml");
		if (!file.exists()) {
			plugin.saveResource("messages.yml", false);
			messagesConfig = YamlConfiguration.loadConfiguration(file);
			return;
		}
		updateMessagesConfigFile(file);
	}


	private void updateMessagesConfigFile(File file) {
		messagesConfig = YamlConfiguration.loadConfiguration(file);
		try {
			InputStream inputStream = getPlugin().getResource("messages.yml");
			if (inputStream == null) {
				throw new IllegalStateException("messages.yml file not found!");
			}
			YamlConfiguration defaults = YamlConfiguration.loadConfiguration(
					new InputStreamReader(inputStream, StandardCharsets.UTF_8)
			);
			messagesConfig.setDefaults(defaults);
			messagesConfig.options().copyDefaults(true);
			messagesConfig.save(file);
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
	}


	public void resetScoreboard() {
		ScoreboardManager scoreboardManager = Bukkit.getScoreboardManager();
		for (Objective objective : scoreboardManager.getMainScoreboard().getObjectives()) {
			objective.unregister();
		}
	}

}
