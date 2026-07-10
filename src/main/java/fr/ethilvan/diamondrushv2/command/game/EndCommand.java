package fr.ethilvan.diamondrushv2.command.game;

import fr.ethilvan.diamondrushv2.DiamondRush;
import fr.ethilvan.diamondrushv2.command.Subcommand;
import fr.ethilvan.diamondrushv2.event.GameEndEvent;
import fr.ethilvan.diamondrushv2.tools.MessageHelper;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;


public class EndCommand extends Subcommand {

	public EndCommand(DiamondRush diamondRush) {
		super(diamondRush);
	}


	@Override
	public String getName() {
		return "end";
	}

	@Override
	public String getSyntax() {
		return "/diamondrush end";
	}

	@Override
	public String getDescription() {
		return "messages.commands.end.description";
	}

	@Override
	public String getPermission() {
		return "diamondrush.admin.end";
	}

	@Override
	public void perform(CommandSender sender, @NotNull String[] args) {
		if (!sender.hasPermission(getPermission())) {
			MessageHelper.sendMessage(diamondRush, sender, "messages.commands.noPermission");
			return;
		}
		if (diamondRush.getGame() == null) {
			MessageHelper.sendMessage(diamondRush, sender, "messages.noGameCreated");
			return;
		}
		Bukkit.getPluginManager().callEvent(new GameEndEvent());
		MessageHelper.broadcastMessage(diamondRush, "messages.commands.end.success");
	}
}
