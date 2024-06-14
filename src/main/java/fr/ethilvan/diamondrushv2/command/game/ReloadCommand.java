package fr.ethilvan.diamondrushv2.command.game;

import fr.ethilvan.diamondrushv2.DiamondRush;
import fr.ethilvan.diamondrushv2.command.Subcommand;
import fr.ethilvan.diamondrushv2.config.Config;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class ReloadCommand extends Subcommand {

	public ReloadCommand(DiamondRush diamondRush) {
		super(diamondRush);
	}


	@Override
	public String getName() {
		return "reload";
	}

	@Override
	public String getSyntax() {
		return "/diamondrush reload";
	}

	@Override
	public String getDescription() {
		return "Reloads the configuration from files.";
	}

	@Override
	public String getPermission() {
		return "diamondrush.reload";
	}

	@Override
	public void perform(CommandSender sender, @NotNull String[] args) {
		if (!sender.hasPermission(getPermission())) {
			sendMessage(sender, "messages.commands.noPermission");
			return;
		}
		try {
			diamondRush.getPlugin().unregisterEvents();
			diamondRush.getPlugin().reloadConfig();
			diamondRush.getConfig().load();
			diamondRush.loadMessages();
			diamondRush.getPlugin().registerEvents();
			sendMessage(sender, "messages.commands.reload.success");
		}
		catch (RuntimeException e) {
			Map<String, String> placeholders = new HashMap<>();
			placeholders.put("\\{message\\}", e.getMessage());
			sendMessage(sender, "messages.commands.reload.failure", placeholders);
		}
	}
}
