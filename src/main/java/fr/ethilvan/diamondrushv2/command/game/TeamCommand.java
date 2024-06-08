package fr.ethilvan.diamondrushv2.command.game;

import fr.ethilvan.diamondrushv2.DiamondRush;
import fr.ethilvan.diamondrushv2.command.Subcommand;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
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
		return "diamondrush.team";
	}

	@Override
	public void perform(CommandSender sender, @NotNull String[] args) {
		if (!sender.hasPermission(getPermission())) {
			sendMessage(sender, "messages.commands.noPermission");
			return;
		}
		Map<String, String> placeholders = new HashMap<>();
		placeholders.put("\\{team-color\\}", "gold");
		placeholders.put("\\{team-name\\}", "Orange");
		sendMessage(sender, "messages.commands.team.addSuccess", placeholders);
	}
}
