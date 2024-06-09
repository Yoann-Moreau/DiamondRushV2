package fr.ethilvan.diamondrushv2.command.game;

import fr.ethilvan.diamondrushv2.DiamondRush;
import fr.ethilvan.diamondrushv2.command.Subcommand;
import fr.ethilvan.diamondrushv2.game.Game;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class CreateCommand extends Subcommand {

	public CreateCommand(DiamondRush diamondRush) {
		super(diamondRush);
	}


	@Override
	public String getName() {
		return "create";
	}

	@Override
	public String getSyntax() {
		return "/diamondrush create";
	}

	@Override
	public String getDescription() {
		return "messages.commands.create.description";
	}

	@Override
	public String getPermission() {
		return "diamondrush.create";
	}


	@Override
	public void perform(CommandSender sender, @NotNull String[] args) {
		if (!sender.hasPermission(getPermission())) {
			sendMessage(sender, "messages.commands.noPermission");
			return;
		}
		if (!(sender instanceof Player player)) {
			sendMessage(sender, "messages.commands.notAPlayer");
			return;
		}
		if(diamondRush.getGame() != null) {
			sendMessage(sender, "messages.commands.create.gameAlreadyCreated");
			return;
		}
		diamondRush.setGame(new Game(player.getWorld(), player.getLocation()));
		sendMessage(sender, "messages.commands.create.success");
	}
}
