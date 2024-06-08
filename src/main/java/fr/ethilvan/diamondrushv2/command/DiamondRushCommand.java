package fr.ethilvan.diamondrushv2.command;

import fr.ethilvan.diamondrushv2.DiamondRush;
import fr.ethilvan.diamondrushv2.command.game.CreateCommand;
import fr.ethilvan.diamondrushv2.command.game.JoinCommand;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class DiamondRushCommand implements TabExecutor {

	protected DiamondRush diamondRush;
	protected ArrayList<Subcommand> subcommands;


	public DiamondRushCommand(DiamondRush diamondRush) {
		this.diamondRush = diamondRush;

		subcommands = new ArrayList<>();
		subcommands.add(new CreateCommand(diamondRush));
		subcommands.add(new JoinCommand(diamondRush));
	}


	@Override
	public boolean onCommand(
			@NotNull CommandSender sender,
			@NotNull Command command,
			@NotNull String label,
			@NotNull String[] args
	) {

		if (args.length < 1) {
			sender.sendMessage("----------------Available commands----------------");
			for (Subcommand subcommand : subcommands) {
				if (!sender.hasPermission(subcommand.getPermission())) {
					continue;
				}
				String description = diamondRush.getMessagesConfig().getString(subcommand.getDescription());
				sender.sendRichMessage("<gold>" + subcommand.getSyntax() + "<dark_gray>: <white>" + description);
			}
			sender.sendMessage("--------------------------------------------------");
			return true;
		}

		for (Subcommand subcommand : subcommands) {
			if (args[0].equalsIgnoreCase(subcommand.getName())) {
				subcommand.perform(sender, args);
				return true;
			}
		}

		return false;
	}


	@Override
	public @Nullable List<String> onTabComplete(
			@NotNull CommandSender sender,
			@NotNull Command command,
			@NotNull String label,
			@NotNull String[] args
	) {

		if (args.length == 1) {
			ArrayList<String> subcommandNames = new ArrayList<>();

			for (Subcommand subcommand : subcommands) {
				subcommandNames.add(subcommand.getName());
			}
			return subcommandNames;
		}

		if (args.length > 1) {
			for (Subcommand subcommand : subcommands) {
				if (args[0].equalsIgnoreCase(subcommand.getName())) {
					return subcommand.getAutoCompleteChoices(args);
				}
			}
		}

		return List.of();
	}
}
