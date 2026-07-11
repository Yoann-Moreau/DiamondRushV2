package fr.ethilvan.diamondrushv2.tools;

import fr.ethilvan.diamondrushv2.DiamondRush;
import fr.ethilvan.diamondrushv2.game.Team;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.UUID;


public class MessageHelper {

	public static void sendMessage(
			DiamondRush diamondRush,
			CommandSender commandSender,
			String messagePath
	) {
		String message = diamondRush.getMessagesConfig().getString(messagePath);
		if (message == null) {
			missingMessage(commandSender, messagePath);
			return;
		}
		commandSender.sendRichMessage(message);
	}


	public static void sendMessage(
			DiamondRush diamondRush,
			CommandSender commandSender,
			String messagePath,
			HashMap<String, String> placeholders
	) {
		String message = diamondRush.getMessagesConfig().getString(messagePath);
		if (message == null) {
			missingMessage(commandSender, messagePath);
			return;
		}

		for (HashMap.Entry<String, String> placeholder : placeholders.entrySet()) {
			message = message.replaceAll(placeholder.getKey(), placeholder.getValue());
		}
		commandSender.sendRichMessage(message);
	}


	public static void messageSpectators(
			DiamondRush diamondRush,
			@NotNull Player player,
			TextComponent textComponent,
			String color
	) {
		for (UUID spectatorUuid : diamondRush.getGame().getSpectatorUuids()) {
			Player spectator = Bukkit.getPlayer(spectatorUuid);
			if (spectator == null) {
				continue;
			}
			HashMap<String, String> placeholders = new HashMap<>();
			placeholders.put("\\{team-color\\}", color);
			placeholders.put("\\{player-name\\}", player.getName());
			placeholders.put("\\{message\\}", textComponent.content());
			messagePlayer(diamondRush, spectator, "messages.chatMessage", placeholders);
		}
	}


	public static void broadcastMessage(DiamondRush diamondRush, String messagePath) {
		String message = diamondRush.getMessagesConfig().getString(messagePath);
		if (message == null) {
			missingMessage(diamondRush, messagePath);
			return;
		}
		for (Player player : diamondRush.getPlugin().getServer().getOnlinePlayers()) {
			player.sendRichMessage(message);
		}
	}

	public static void broadcastMessage(
			DiamondRush diamondRush,
			String messagePath,
			HashMap<String, String> placeholders
	) {
		String message = diamondRush.getMessagesConfig().getString(messagePath);
		if (message == null) {
			missingMessage(diamondRush, messagePath);
			return;
		}

		for (HashMap.Entry<String, String> placeholder : placeholders.entrySet()) {
			message = message.replaceAll(placeholder.getKey(), placeholder.getValue());
		}
		for (Player player : diamondRush.getPlugin().getServer().getOnlinePlayers()) {
			player.sendRichMessage(message);
		}
	}


	public static void messagePlayer(DiamondRush diamondRush, Player player, String messagePath) {
		String message = diamondRush.getMessagesConfig().getString(messagePath);
		if (message == null) {
			missingMessage(diamondRush, messagePath);
			return;
		}
		player.sendRichMessage(message);
	}


	public static void messagePlayer(
			DiamondRush diamondRush,
			Player player,
			String messagePath,
			HashMap<String, String> placeholders
	) {
		String message = diamondRush.getMessagesConfig().getString(messagePath);
		if (message == null) {
			missingMessage(diamondRush, messagePath);
			return;
		}
		for (HashMap.Entry<String, String> placeholder : placeholders.entrySet()) {
			message = message.replaceAll(placeholder.getKey(), placeholder.getValue());
		}
		player.sendRichMessage(message);
	}


	public static void messageLeaders(DiamondRush diamondRush, String messagePath) {
		for (HashMap.Entry<String, Team> teamEntry : diamondRush.getGame().getTeams().entrySet()) {
			Player player = Bukkit.getPlayer(teamEntry.getValue().getLeaderUuid());
			if (player == null) {
				continue;
			}
			String message = diamondRush.getMessagesConfig().getString(messagePath);
			if (message == null) {
				missingMessage(diamondRush, messagePath);
				return;
			}
			player.sendRichMessage(message);
		}
	}


	public static void messageOtherPlayersInTeams(DiamondRush diamondRush, String messagePath) {
		for (HashMap.Entry<String, Team> teamEntry : diamondRush.getGame().getTeams().entrySet()) {
			UUID leaderUuid = teamEntry.getValue().getLeaderUuid();
			for (UUID uuid : teamEntry.getValue().getPlayerUUIDs()) {
				if (uuid == leaderUuid) { // Skip leader
					continue;
				}
				Player player = Bukkit.getPlayer(uuid);
				if (player == null) {
					continue;
				}
				String message = diamondRush.getMessagesConfig().getString(messagePath);
				if (message == null) {
					missingMessage(diamondRush, messagePath);
					return;
				}
				player.sendRichMessage(message);
			}
		}
	}


	public static void messageOtherPlayersInTeam(DiamondRush diamondRush, Team team, String messagePath) {
		messageOtherPlayersInTeam(diamondRush, team, messagePath, new HashMap<>());
	}


	public static void messageOtherPlayersInTeam(
			DiamondRush diamondRush,
			Team team,
			String messagePath,
			HashMap<String, String> placeholders
	) {
		for (UUID uuid : team.getPlayerUUIDs()) {
			if (uuid == team.getLeaderUuid()) { // Skip leader
				continue;
			}
			Player player = Bukkit.getPlayer(uuid);
			if (player == null) {
				continue;
			}
			String message = diamondRush.getMessagesConfig().getString(messagePath);
			if (message == null) {
				missingMessage(diamondRush, messagePath);
				return;
			}
			for (HashMap.Entry<String, String> placeholder : placeholders.entrySet()) {
				message = message.replaceAll(placeholder.getKey(), placeholder.getValue());
			}
			player.sendRichMessage(message);
		}
	}


	public static void missingMessage(DiamondRush diamondRush, String messagePath) {
		diamondRush.getPlugin().getLogger().warning("Message missing from configuration: '" + messagePath + "'.");
	}


	public static void missingMessage(CommandSender sender, String messagePath) {
		sender.sendRichMessage("<red>Message missing from configuration: '" + messagePath + "'.");
	}
}
