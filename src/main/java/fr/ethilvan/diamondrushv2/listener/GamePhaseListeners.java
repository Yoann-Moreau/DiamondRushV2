package fr.ethilvan.diamondrushv2.listener;

import fr.ethilvan.diamondrushv2.DiamondRush;
import fr.ethilvan.diamondrushv2.event.GameStartEvent;
import fr.ethilvan.diamondrushv2.event.TotemPlacementStartEvent;
import fr.ethilvan.diamondrushv2.game.GamePhase;
import fr.ethilvan.diamondrushv2.game.Team;
import fr.ethilvan.diamondrushv2.region.CylindricalRegion;
import fr.ethilvan.diamondrushv2.region.Region;
import fr.ethilvan.diamondrushv2.tools.Timer;
import org.bukkit.Bukkit;
import org.bukkit.GameRule;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

import java.util.Map;
import java.util.UUID;

public class GamePhaseListeners implements Listener {

	private final DiamondRush diamondRush;
	private Timer gameTimer = null;


	public GamePhaseListeners(DiamondRush diamondRush) {
		this.diamondRush = diamondRush;
	}


	@EventHandler
	public void onGameStart(GameStartEvent event) {
		diamondRush.getGame().setPhase(GamePhase.STARTING);
		diamondRush.broadcastMessage("messages.phases.starting.start");

		gameTimer = new Timer(
				diamondRush.getPlugin(),
				5,
				() -> Bukkit.getPluginManager().callEvent(new TotemPlacementStartEvent()),
				"messages.phases.starting.end"
		);
		gameTimer.run();
	}


	@EventHandler
	public void onTotemPlacementStart(TotemPlacementStartEvent event) {
		diamondRush.getGame().setPhase(GamePhase.TOTEM_PLACEMENT);
		diamondRush.getGame().assignLeaders();
		diamondRush.messageLeaders("messages.phases.totemPlacement.start.leader");
		diamondRush.messageOtherPlayersInTeam("messages.phases.totemPlacement.start.player");

		diamondRush.getGame().getWorld().setGameRule(GameRule.KEEP_INVENTORY, true);
		diamondRush.getGame().getWorld().setGameRule(GameRule.ANNOUNCE_ADVANCEMENTS, false);
		diamondRush.getGame().getWorld().setTime(0);

		diamondRush.getGame().resetPlayers();

		Region spawnRegion = diamondRush.getGame().getRegion("gameSpawn");
		if (!(spawnRegion instanceof CylindricalRegion cylindricalRegion)) {
			throw new RuntimeException("The game spawn must be a cylindrical region.");
		}
		Block center = cylindricalRegion.getCenter();
		int radius = cylindricalRegion.getRadius();
		int i = 0;
		for (Map.Entry<String, Team> teamEntry : diamondRush.getGame().getTeams().entrySet()) {
			// Calculate start position based on radius of spawn platform and number of teams
			double angle = i * Math.PI / 180;
			int x = (int) Math.round(center.getX() + radius * Math.cos(angle));
			int y = center.getY();
			int z = (int) Math.round(center.getZ() + radius * Math.sin(angle));

			Location startPosition = new Location(cylindricalRegion.getWorld(), x + 0.5, y, z + 0.5, i - 90, 0);
			Block teamBlock = cylindricalRegion.getWorld().getBlockAt(x, y -1, z);
			teamBlock.setType(teamEntry.getValue().getTeamColor().getMaterial());

			for (UUID uuid : teamEntry.getValue().getPlayerUUIDs()) {
				Player player = Bukkit.getPlayer(uuid);
				if (player == null) {
					continue;
				}
				if (uuid == teamEntry.getValue().getLeaderUuid()) {
					player.getInventory().setItemInMainHand(new ItemStack(Material.OBSIDIAN));
				}
				player.teleportAsync(startPosition);
			}

			// Calculate new angle for next team
			i += 360 / diamondRush.getGame().getTeams().size();
		}

		gameTimer = new Timer(
				diamondRush.getPlugin(),
				diamondRush.getConfig().getTotemPlacementDuration(),
				() -> {

				},
				"messages.phases.totemPlacement.end"
		);
		gameTimer.run();
	}
}
