package fr.ethilvan.diamondrushv2.command.game;

import fr.ethilvan.diamondrushv2.DiamondRush;
import fr.ethilvan.diamondrushv2.command.Subcommand;
import fr.ethilvan.diamondrushv2.game.Team;
import fr.ethilvan.diamondrushv2.game.TeamColor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TeamCommand extends Subcommand {

	public TeamCommand(DiamondRush diamondRush) {
		super(diamondRush);
	}


	@Override
	public String getName() {
		return "team";
	}

	@Override
	public String getSyntax() {
		return "/diamondrush team <add|remove|modify> <teamName>";
	}

	@Override
	public String getDescription() {
		return "messages.commands.team.description";
	}

	@Override
	public String getPermission() {
		return "diamondrush.setup.team";
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
		if (args.length < 2) {
			sendMessage(sender, "messages.commands.team.noActionSpecified");
			return;
		}
		if (args[1].equalsIgnoreCase("add")) {
			addTeam(sender, args);
		}
		else if (args[1].equalsIgnoreCase("remove")) {
			removeTeam(sender, args);
		}
	}


	@Override
	public List<String> getAutoCompleteChoices(String[] args) {
		if (args.length == 2) {
			ArrayList<String> actions = new ArrayList<>();
			actions.add("add");
			actions.add("remove");
			actions.add("modify");
			return actions;
		}

		if (args.length == 3) {
			if (args[1].equalsIgnoreCase("remove")) {
				return new ArrayList<>(diamondRush.getGame().getTeams().keySet());
			}
		}

		if (args.length == 4) {
			if (args[1].equalsIgnoreCase("add")) {
				ArrayList<String> colors = new ArrayList<>();
				for (TeamColor teamColor : TeamColor.values()) {
					colors.add(teamColor.name());
				}
				return colors;
			}
		}
		return List.of();
	}


	private void addTeam(CommandSender sender, @NotNull String[] args) {
		if (args.length < 3) {
			sendMessage(sender, "messages.commands.team.noTeamSpecified");
			return;
		}
		if (args.length < 4) {
			sendMessage(sender, "messages.commands.team.noColorSpecified");
			return;
		}
		String teamName = args[2];
		String colorName = args[3];

		if (teamName.equals("game")) {
			Map<String, String> placeholders = new HashMap<>();
			placeholders.put("\\{word\\}", teamName);
			sendMessage(sender, "messages.commands.team.reservedWord", placeholders);
			return;
		}

		TeamColor teamColor;
		try {
			teamColor = TeamColor.valueOf(colorName.toUpperCase());
		} catch (IllegalArgumentException e) {
			sendMessage(sender, "messages.commands.team.notAValidColor");
			return;
		}
		// Check if team name is taken
		if (diamondRush.getGame().getTeam(teamName) != null) {
			sendMessage(sender, "messages.commands.team.teamAlreadyExists");
			return;
		}
		// Check if team color is taken
		for (Map.Entry<String, Team> teamEntry : diamondRush.getGame().getTeams().entrySet()) {
			if (teamEntry.getValue().getTeamColor().equals(teamColor)) {
				sendMessage(sender, "messages.commands.team.colorTaken");
				return;
			}
		}

		Team team = new Team(teamName, teamColor);
		diamondRush.getGame().addTeam(teamName, team);
		Map<String, String> placeholders = new HashMap<>();
		placeholders.put("\\{team-color\\}", team.getTeamColor().getColorName().toLowerCase());
		placeholders.put("\\{team-name\\}", team.getName());
		sendMessage(sender, "messages.commands.team.addSuccess", placeholders);
	}


	private void removeTeam(CommandSender sender, @NotNull String[] args) {
		if (args.length < 3) {
			sendMessage(sender, "messages.commands.team.noTeamSpecified");
			return;
		}
		String teamName = args[2];
		for (Map.Entry<String, Team> teamEntry : diamondRush.getGame().getTeams().entrySet()) {
			if (!teamEntry.getKey().equals(teamName)) {
				continue;
			}
			Map<String, String> placeholders = new HashMap<>();
			placeholders.put("\\{team-color\\}", teamEntry.getValue().getTeamColor().getColorName().toLowerCase());
			placeholders.put("\\{team-name\\}", teamEntry.getValue().getName());
			teamEntry.getValue().getMinecraftTeam().unregister();
			diamondRush.getGame().getTeams().remove(teamName);
			sendMessage(sender, "messages.commands.team.removeSuccess", placeholders);
			return;
		}

		sendMessage(sender, "messages.commands.team.noSuchTeam");
	}
}
