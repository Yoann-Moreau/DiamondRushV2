package fr.ethilvan.diamondrushv2;

import fr.ethilvan.diamondrushv2.config.Config;
import fr.ethilvan.diamondrushv2.game.Game;
import fr.ethilvan.diamondrushv2.game.Team;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;

import java.io.File;
import java.util.Map;
import java.util.UUID;

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


	public void messagePlayer(Player player, String messagePath) {
		String message = plugin.getDiamondRush().getMessagesConfig().getString(messagePath);
		if (message == null) {
			missingMessage(messagePath);
			return;
		}
		player.sendRichMessage(message);
	}


	public void messagePlayer(Player player, String messagePath, Map<String, String> placeholders) {
		String message = plugin.getDiamondRush().getMessagesConfig().getString(messagePath);
		if (message == null) {
			missingMessage(messagePath);
			return;
		}
		for (Map.Entry<String, String> placeholder : placeholders.entrySet()) {
			message = message.replaceAll(placeholder.getKey(), placeholder.getValue());
		}
		player.sendRichMessage(message);
	}


	public void messageLeaders(String messagePath) {
		for (Map.Entry<String, Team> teamEntry : getGame().getTeams().entrySet()) {
			Player player = Bukkit.getPlayer(teamEntry.getValue().getLeaderUuid());
			if (player == null) {
				continue;
			}
			String message = plugin.getDiamondRush().getMessagesConfig().getString(messagePath);
			if (message == null) {
				missingMessage(messagePath);
				return;
			}
			player.sendRichMessage(message);
		}
	}


	public void messageOtherPlayersInTeams(String messagePath) {
		for (Map.Entry<String, Team> teamEntry : getGame().getTeams().entrySet()) {
			UUID leaderUuid = teamEntry.getValue().getLeaderUuid();
			for (UUID uuid : teamEntry.getValue().getPlayerUUIDs()) {
				if (uuid == leaderUuid) { // Skip leader
					continue;
				}
				Player player = Bukkit.getPlayer(uuid);
				if (player == null) {
					continue;
				}
				String message = plugin.getDiamondRush().getMessagesConfig().getString(messagePath);
				if (message == null) {
					missingMessage(messagePath);
					return;
				}
				player.sendRichMessage(message);
			}
		}
	}


	public void messageOtherPlayersInTeam(Team team, String messagePath) {
		for (UUID uuid : team.getPlayerUUIDs()) {
			if (uuid == team.getLeaderUuid()) { // Skip leader
				continue;
			}
			Player player = Bukkit.getPlayer(uuid);
			if (player == null) {
				continue;
			}
			String message = plugin.getDiamondRush().getMessagesConfig().getString(messagePath);
			if (message == null) {
				missingMessage(messagePath);
				return;
			}
			player.sendRichMessage(message);
		}
	}


	public void messageOtherPlayersInTeam(Team team, String messagePath, Map<String, String> placeholders) {
		for (UUID uuid : team.getPlayerUUIDs()) {
			if (uuid == team.getLeaderUuid()) { // Skip leader
				continue;
			}
			Player player = Bukkit.getPlayer(uuid);
			if (player == null) {
				continue;
			}
			String message = plugin.getDiamondRush().getMessagesConfig().getString(messagePath);
			if (message == null) {
				missingMessage(messagePath);
				return;
			}
			for (Map.Entry<String, String> placeholder : placeholders.entrySet()) {
				message = message.replaceAll(placeholder.getKey(), placeholder.getValue());
			}
			player.sendRichMessage(message);
		}
	}


	public void missingMessage(String messagePath) {
		plugin.getLogger().warning("Message missing from configuration: '" + messagePath + "'.");
	}


	public void resetScoreboard() {
		ScoreboardManager scoreboardManager = Bukkit.getScoreboardManager();
		for (Objective objective : scoreboardManager.getMainScoreboard().getObjectives()) {
			objective.unregister();
		}
	}

}
