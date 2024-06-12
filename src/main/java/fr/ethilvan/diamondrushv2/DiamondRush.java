package fr.ethilvan.diamondrushv2;

import fr.ethilvan.diamondrushv2.config.Config;
import fr.ethilvan.diamondrushv2.game.Game;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.Map;

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


	public void broadcastMessage(String messagePath) {
		String message = plugin.getDiamondRush().getMessagesConfig().getString(messagePath);
		if (message == null) {
			missingMessage(messagePath);
			return;
		}
		for (Player player : plugin.getServer().getOnlinePlayers()) {
			player.sendRichMessage(message);
		}
	}

	public void broadcastMessage(String messagePath, Map<String, String> placeholders) {
		String message = plugin.getDiamondRush().getMessagesConfig().getString(messagePath);
		if (message == null) {
			missingMessage(messagePath);
			return;
		}

		for (Map.Entry<String, String> placeholder : placeholders.entrySet()) {
			message = message.replaceAll(placeholder.getKey(), placeholder.getValue());
		}
		for (Player player : plugin.getServer().getOnlinePlayers()) {
			player.sendRichMessage(message);
		}
	}


	private void missingMessage(String messagePath) {
		plugin.getLogger().warning("Message missing from configuration: '" + messagePath + "'.");
	}

}
