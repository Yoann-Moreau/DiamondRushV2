package fr.ethilvan.diamondrushv2.command.game;

import fr.ethilvan.diamondrushv2.DiamondRush;
import fr.ethilvan.diamondrushv2.command.Subcommand;
import fr.ethilvan.diamondrushv2.game.Team;
import fr.ethilvan.diamondrushv2.tools.MessageHelper;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;


public class SpectateCommand extends Subcommand {


	public SpectateCommand(DiamondRush diamondRush) {
		super(diamondRush);
	}


	@Override
	public String getName() {
		return "spectate";
	}


	@Override
	public String getSyntax() {
		return "/dr spectate";
	}


	@Override
	public String getDescription() {
		return "messages.commands.spectate.description";
	}


	@Override
	public String getPermission() {
		return "";
	}


	@Override
	public void perform(CommandSender sender, @NotNull String[] args) {
		if (!(sender instanceof Player player)) {
			MessageHelper.sendMessage(diamondRush, sender, "messages.commands.notAPlayer");
			return;
		}

		if (diamondRush.getGame() == null) {
			MessageHelper.sendMessage(diamondRush, sender, "messages.noGameCreated");
			return;
		}

		for (HashMap.Entry<String, Team> entry : diamondRush.getGame().getTeams().entrySet()) {
			Team team = entry.getValue();
			if (team.getPlayerUUIDs().contains(player.getUniqueId())) {
				MessageHelper.sendMessage(diamondRush, sender, "messages.commands.spectate.alreadyInTeam");
				return;
			}
		}

		diamondRush.getGame().addSpectatorUuid(player.getUniqueId());
		player.setGameMode(GameMode.SPECTATOR);
		MessageHelper.sendMessage(diamondRush, player, "messages.commands.spectate.spectating");
		player.getInventory().clear();
		giveSpectatorManagementItems(player);
	}


	private void giveSpectatorManagementItems(Player player) {
		player.getInventory().setItemInMainHand(new ItemStack(Material.PLAYER_HEAD));
	}
}
