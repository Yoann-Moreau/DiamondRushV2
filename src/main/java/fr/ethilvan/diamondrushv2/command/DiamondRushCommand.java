package fr.ethilvan.diamondrushv2.command;

import fr.ethilvan.diamondrushv2.DiamondRush;
import fr.ethilvan.diamondrushv2.command.game.CreateCommand;
import fr.ethilvan.diamondrushv2.command.game.JoinCommand;
import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class DiamondRushCommand implements BasicCommand, TabExecutor {

	protected DiamondRush diamondRush;
	protected ArrayList<Subcommand> subcommands;


	public DiamondRushCommand(DiamondRush diamondRush) {
		this.diamondRush = diamondRush;

		subcommands = new ArrayList<>();
		subcommands.add(new CreateCommand(diamondRush));
		subcommands.add(new JoinCommand(diamondRush));
	}


	@Override
	public void execute(@NotNull CommandSourceStack commandSourceStack, @NotNull String[] args) {
		CommandSender sender = commandSourceStack.getSender();

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
			return;
		}

		for (Subcommand subcommand : subcommands) {
			if (args[0].equalsIgnoreCase(subcommand.getName())) {
				subcommand.perform(sender, args);
				;
				break;
			}
		}

	}

	@Override
	public @NotNull Collection<String> suggest(@NotNull CommandSourceStack commandSourceStack, @NotNull String[] args) {
		return BasicCommand.super.suggest(commandSourceStack, args);
	}


	@Override
	public boolean onCommand(
			@NotNull CommandSender sender,
			@NotNull Command command,
			@NotNull String label,
			@NotNull String[] args
	) {
		return false;
	}


	@Override
	public @Nullable List<String> onTabComplete(
			@NotNull CommandSender sender,
			@NotNull Command command,
			@NotNull String label,
			@NotNull String[] args
	) {
		return List.of();
	}
}
