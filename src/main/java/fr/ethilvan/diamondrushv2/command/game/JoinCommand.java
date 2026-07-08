package fr.ethilvan.diamondrushv2.command.game;

import fr.ethilvan.diamondrushv2.DiamondRush;
import fr.ethilvan.diamondrushv2.command.Subcommand;
import fr.ethilvan.diamondrushv2.game.Team;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;


public class JoinCommand extends Subcommand {

	public JoinCommand(DiamondRush diamondRush) {
		super(diamondRush);
	}


	@Override
	public String getName() {
		return "join";
	}

	@Override
	public String getSyntax() {
		return "/diamondrush join <teamName>";
	}

	@Override
	public String getDescription() {
		return "messages.commands.join.description";
	}

	@Override
	public String getPermission() {
		return "";
	}


	@Override
	public void perform(CommandSender sender, @NotNull String[] args) {
		if (!(sender instanceof Player player)) {
			sendMessage(sender, "messages.commands.notAPlayer");
			return;
		}
		if (diamondRush.getGame() == null) {
			sendMessage(sender, "messages.noGameCreated");
			return;
		}
		if (args.length < 2) {
			sendMessage(sender, "messages.commands.join.noTeamSpecified");
			return;
		}
		String teamName = args[1];
		if (!diamondRush.getGame().getTeams().containsKey(teamName)) {
			sendMessage(sender, "messages.commands.join.noSuchTeam");
			return;
		}
		Team team = diamondRush.getGame().getTeam(teamName);
		// Check if player has already joined a team
		for (HashMap.Entry<String, Team> teamEntry : diamondRush.getGame().getTeams().entrySet()) {
			if (teamEntry.getValue().getPlayerUUIDs().contains(player.getUniqueId())) {
				sendMessage(sender, "messages.commands.join.alreadyInATeam");
				return;
			}
		}
		// Add player to team
		team.addPlayerUuid(player.getUniqueId());
		HashMap<String, String> placeholders = new HashMap<>();
		placeholders.put("\\{team-color\\}", team.getTeamColor().getColorName().toLowerCase());
		placeholders.put("\\{team-name\\}", team.getName());
		sendMessage(sender, "messages.commands.join.success", placeholders);
	}


	@Override
	public ArrayList<String> getAutoCompleteChoices(String[] args) {
		if (args.length == 2 && diamondRush.getGame() != null) {
			return new ArrayList<>(diamondRush.getGame().getTeams().keySet());
		}
		return new ArrayList<>();
	}
}
