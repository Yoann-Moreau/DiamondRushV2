package fr.ethilvan.diamondrushv2.command.game;

import fr.ethilvan.diamondrushv2.DiamondRush;
import fr.ethilvan.diamondrushv2.command.Subcommand;
import fr.ethilvan.diamondrushv2.event.GameStartEvent;
import fr.ethilvan.diamondrushv2.game.GamePhase;
import fr.ethilvan.diamondrushv2.game.Team;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class StartCommand extends Subcommand {

	public StartCommand(DiamondRush diamondRush) {
		super(diamondRush);
	}

	@Override
	public String getName() {
		return "start";
	}

	@Override
	public String getSyntax() {
		return "/diamondrush start";
	}

	@Override
	public String getDescription() {
		return "Starts the DiamondRush game.";
	}

	@Override
	public String getPermission() {
		return "diamondrush.start";
	}

	@Override
	public void perform(CommandSender sender, @NotNull String[] args) {
		if (!sender.hasPermission(getPermission())) {
			sendMessage(sender, "messages.commands.noPermission");
			return;
		}
		if (diamondRush.getGame() == null) {
			sendMessage(sender, "messages.noGameCreated");
			return;
		}
		if (diamondRush.getGame().getTeams().isEmpty()) { // TODO: Replace with size() < 2
			sendMessage(sender, "messages.commands.start.notEnoughTeams");
			return;
		}
		for (Map.Entry<String, Team> teamEntry : diamondRush.getGame().getTeams().entrySet()) {
			if (teamEntry.getValue().getPlayerUUIDs().isEmpty()) {
				Map<String, String> placeholders = new HashMap<>();
				placeholders.put("\\{team-color\\}", teamEntry.getValue().getTeamColor().getColorName().toLowerCase());
				placeholders.put("\\{team-name\\}", teamEntry.getValue().getName());
				sendMessage(sender, "messages.commands.start.notEnoughPlayersInTeam", placeholders);
				return;
			}
		}
		if (!diamondRush.getGame().getPhase().equals(GamePhase.CREATION)) {
			sendMessage(sender, "messages.commands.start.gameAlreadyStarted");
			return;
		}
		Bukkit.getPluginManager().callEvent(new GameStartEvent());
	}

}
