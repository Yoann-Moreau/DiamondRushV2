package fr.ethilvan.diamondrushv2.command.game;

import fr.ethilvan.diamondrushv2.DiamondRush;
import fr.ethilvan.diamondrushv2.command.Subcommand;
import fr.ethilvan.diamondrushv2.game.SpectatorInventory;
import fr.ethilvan.diamondrushv2.game.Team;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;


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
		return "/diamondrush join <teamName>";
	}

	@Override
	public String getDescription() {
		return "messages.commands.join.description";
	}

	@Override
	public String getPermission() {
		return "";
	}


	@Override
	public void perform(CommandSender sender, @NotNull String[] args) {
		if (!(sender instanceof Player player)) {
			sendMessage(sender, "messages.commands.notAPlayer");
			return;
		}
		if (diamondRush.getGame() == null) {
			sendMessage(sender, "messages.noGameCreated");
			return;
		}
		if (args.length < 2) {
			sendMessage(sender, "messages.commands.join.noTeamSpecified");
			return;
		}

		if (diamondRush.getGame().getSpectators().contains(player)) {
			sendMessage(sender, "messages.commands.join.spectating");
			return;
		}

		String teamName = args[1];
		if (!diamondRush.getGame().getTeams().containsKey(teamName)) {
			sendMessage(sender, "messages.commands.join.noSuchTeam");
			return;
		}
		Team team = diamondRush.getGame().getTeam(teamName);
		if (team.getPlayerUUIDs().size() >= 8) {
			sendMessage(sender, "messages.commands.join.teamAtMaxCapacity");
			return;
		}
		// Check if player has already joined a team
		for (HashMap.Entry<String, Team> teamEntry : diamondRush.getGame().getTeams().entrySet()) {
			if (teamEntry.getValue().getPlayerUUIDs().contains(player.getUniqueId())) {
				sendMessage(sender, "messages.commands.join.alreadyInATeam");
				return;
			}
		}
		// Add player to team
		team.addPlayerUuid(player.getUniqueId());
		HashMap<String, String> placeholders = new HashMap<>();
		placeholders.put("\\{team-color\\}", team.getTeamColor().getColorName().toLowerCase());
		placeholders.put("\\{team-name\\}", team.getName());
		sendMessage(sender, "messages.commands.join.success", placeholders);
		updateSpectatorInventory();
	}


	@Override
	public ArrayList<String> getAutoCompleteChoices(String[] args) {
		if (args.length == 2 && diamondRush.getGame() != null) {
			return new ArrayList<>(diamondRush.getGame().getTeams().keySet());
		}
		return new ArrayList<>();
	}


	private void updateSpectatorInventory() {
		int numberOfTeams = diamondRush.getGame().getTeams().size();
		SpectatorInventory spectatorInventory = new SpectatorInventory(diamondRush, numberOfTeams);

		int teamIndex = 0;
		for (HashMap.Entry<String, Team> entry : diamondRush.getGame().getTeams().entrySet()) {
			Team team = entry.getValue();
			Material teamMaterial = team.getTeamColor().getMaterial();
			spectatorInventory.addItem(new ItemStack(teamMaterial), teamIndex * 9);

			int playerIndex = 0;
			for (UUID playerUuid : team.getPlayerUUIDs()) {
				Player teamPlayer = Bukkit.getPlayer(playerUuid);
				if (teamPlayer == null) {
					continue;
				}
				ItemStack head = new ItemStack(Material.PLAYER_HEAD);
				head.editMeta(SkullMeta.class, skullMeta ->  {
					skullMeta.setOwningPlayer(teamPlayer);
					skullMeta.displayName(Component.text(teamPlayer.getName()));
				});
				spectatorInventory.addItem(head, playerIndex * 9 + 1);
				playerIndex++;
			}

			teamIndex++;
		}

		diamondRush.getGame().setSpectatorInventory(spectatorInventory);
	}
}
