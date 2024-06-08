package fr.ethilvan.diamondrushv2.command.game;

import fr.ethilvan.diamondrushv2.DiamondRush;
import fr.ethilvan.diamondrushv2.command.DiamondRushCommand;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class CreateCommand extends DiamondRushCommand {

	public CreateCommand(DiamondRush diamondRush, String commandName) {
		super(diamondRush, commandName);
	}


	@Override
	public void perform(CommandSender sender, @NotNull String[] args) {
		if (!sender.hasPermission("diadmondrush.create")) {
			sendMessage(sender, "messages.commands.noPermission");
			return;
		}
		if (!(sender instanceof Player)) {
			sendMessage(sender, "messages.commands.notAPlayer");
			return;
		}
		sendMessage(sender, "messages.commands.create.success");
	}
}
