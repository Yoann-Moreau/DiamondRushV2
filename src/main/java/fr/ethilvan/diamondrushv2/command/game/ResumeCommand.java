package fr.ethilvan.diamondrushv2.command.game;

import fr.ethilvan.diamondrushv2.DiamondRush;
import fr.ethilvan.diamondrushv2.command.Subcommand;
import fr.ethilvan.diamondrushv2.game.GamePhase;
import fr.ethilvan.diamondrushv2.game.Team;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.UUID;

public class ResumeCommand extends Subcommand {

	public ResumeCommand(DiamondRush diamondRush) {
		super(diamondRush);
	}


	@Override
	public String getName() {
		return "resume";
	}

	@Override
	public String getSyntax() {
		return "/diamondrush resume";
	}

	@Override
	public String getDescription() {
		return "messages.commands.resume.description";
	}

	@Override
	public String getPermission() {
		return "diamondrush.resume";
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
		if (!diamondRush.getGame().getPhase().equals(GamePhase.PAUSE)) {
			sendMessage(sender, "messages.commands.resume.noGamePaused");
		}
		// Put team players in survival mode
		for (Map.Entry<String, Team> teamEntry : diamondRush.getGame().getTeams().entrySet()) {
			for (UUID uuid : teamEntry.getValue().getPlayerUUIDs()) {
				Player player = Bukkit.getPlayer(uuid);
				if (player == null) {
					continue;
				}
				player.setGameMode(GameMode.SURVIVAL);
			}
		}
		diamondRush.getGame().getGameTimer().resume();
	}
}
