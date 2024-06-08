package fr.ethilvan.diamondrushv2.command.game;

import fr.ethilvan.diamondrushv2.DiamondRush;
import fr.ethilvan.diamondrushv2.command.Subcommand;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

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
		return "/diamondrush join";
	}

	@Override
	public String getDescription() {
		return "messages.commands.join.description";
	}

	@Override
	public String getPermission() {
		return "diadmondrush.join";
	}


	@Override
	public void perform(CommandSender sender, @NotNull String[] args) {
		if (!sender.hasPermission(getPermission())) {
			sendMessage(sender, "messages.commands.noPermission");
			return;
		}
		if (!(sender instanceof Player)) {
			sendMessage(sender, "messages.commands.notAPlayer");
			return;
		}
		sendMessage(sender, "messages.commands.join.success");
	}
}
