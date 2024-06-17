package fr.ethilvan.diamondrushv2.command.game;

import fr.ethilvan.diamondrushv2.DiamondRush;
import fr.ethilvan.diamondrushv2.command.Subcommand;
import fr.ethilvan.diamondrushv2.game.Team;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.UUID;

public class PauseCommand extends Subcommand {

	public PauseCommand(DiamondRush diamondRush) {
		super(diamondRush);
	}


	@Override
	public String getName() {
		return "pause";
	}

	@Override
	public String getSyntax() {
		return "/diamondrush pause";
	}

	@Override
	public String getDescription() {
		return "messages.commands.pause.description";
	}

	@Override
	public String getPermission() {
		return "diamondrush.pause";
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
		if (diamondRush.getGame().getGameTimer() == null) {
			sendMessage(sender, "messages.commands.pause.noTimer");
			return;
		}
		// Put team players in creative mode
		for (Map.Entry<String, Team> teamEntry : diamondRush.getGame().getTeams().entrySet()) {
			for (UUID uuid : teamEntry.getValue().getPlayerUUIDs()) {
				Player player = Bukkit.getPlayer(uuid);
				if (player == null) {
					continue;
				}
				player.setGameMode(GameMode.CREATIVE);
			}
		}
		diamondRush.getGame().getGameTimer().pause();
	}
}
