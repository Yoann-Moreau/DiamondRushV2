package fr.ethilvan.diamondrushv2.listener;

import fr.ethilvan.diamondrushv2.DiamondRush;
import fr.ethilvan.diamondrushv2.event.*;
import fr.ethilvan.diamondrushv2.game.GamePhase;
import fr.ethilvan.diamondrushv2.game.Team;
import fr.ethilvan.diamondrushv2.region.CuboidRegion;
import fr.ethilvan.diamondrushv2.region.CylindricalRegion;
import fr.ethilvan.diamondrushv2.region.Region;
import fr.ethilvan.diamondrushv2.region.pattern.Pattern;
import fr.ethilvan.diamondrushv2.region.pattern.TotemFloorPattern;
import fr.ethilvan.diamondrushv2.region.pattern.TotemPattern;
import fr.ethilvan.diamondrushv2.tools.Timer;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
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
		diamondRush.messageOtherPlayersInTeams("messages.phases.totemPlacement.start.player");

		diamondRush.getGame().getWorld().setGameRule(GameRule.KEEP_INVENTORY, true);
		diamondRush.getGame().getWorld().setGameRule(GameRule.ANNOUNCE_ADVANCEMENTS, false);
		diamondRush.getGame().getWorld().setTime(0);

		diamondRush.getGame().resetPlayers();

		teleportPlayersToGameSpawn();
		// Give obsidian to leaders
		for (Map.Entry<String, Team> teamEntry : diamondRush.getGame().getTeams().entrySet()) {
			Player leader = Bukkit.getPlayer(teamEntry.getValue().getLeaderUuid());
			if (leader == null) {
				continue;
			}
			leader.getInventory().setItemInMainHand(new ItemStack(Material.OBSIDIAN));
		}

		gameTimer = new Timer(
				diamondRush.getPlugin(),
				diamondRush.getConfig().getTotemPlacementDuration(),
				() -> Bukkit.getPluginManager().callEvent(new TotemPlacementEndEvent()),
				"messages.phases.totemPlacement.end.end"
		);
		gameTimer.run();
	}


	@EventHandler
	public void onTotemPlacementEnd(TotemPlacementEndEvent event) {
		// Check for missing totem
		for (Map.Entry<String, Team> teamEntry : diamondRush.getGame().getTeams().entrySet()) {
			if (teamEntry.getValue().getTotemBlock() == null) {
				diamondRush.broadcastMessage("messages.phases.totemPlacement.end.goAgain");
				Bukkit.getPluginManager().callEvent(new TotemPlacementStartEvent());
				return;
			}
		}
		// Place totems
		for (Map.Entry<String, Team> teamEntry : diamondRush.getGame().getTeams().entrySet()) {
			Block totemBlock = teamEntry.getValue().getTotemBlock();
			int height = totemBlock.getWorld().getMaxHeight() - totemBlock.getY();
			CuboidRegion region = new CuboidRegion(totemBlock, 3, 3, height);
			int totemHeight = diamondRush.getConfig().getTotemHeight();
			List<Pattern> patterns = new ArrayList<>();
			patterns.add(new TotemFloorPattern(region, teamEntry.getValue().getTeamColor()));
			patterns.add(new TotemPattern(region, totemHeight));
			region.create(patterns);
			diamondRush.getGame().addRegion(teamEntry.getValue().getName() + "Totem", region);
		}
		Bukkit.getPluginManager().callEvent(new SpawnPlacementStartEvent());
	}


	@EventHandler
	public void onSpawnPlacementStart(SpawnPlacementStartEvent event) {
		diamondRush.getGame().setPhase(GamePhase.SPAWN_PLACEMENT);
		for (Map.Entry<String, Team> teamEntry : diamondRush.getGame().getTeams().entrySet()) {
			// Set team lives
			int lives = diamondRush.getConfig().getTotemHeight() + 2;
			teamEntry.getValue().setLives(lives);
			// Give chiseled stone bricks to leaders
			Player leader = Bukkit.getPlayer(teamEntry.getValue().getLeaderUuid());
			if (leader == null) {
				continue;
			}
			leader.getInventory().setItemInMainHand(new ItemStack(Material.CHISELED_STONE_BRICKS));
		}
		// Message players
		diamondRush.messageLeaders("messages.phases.spawnPlacement.start.leader");
		diamondRush.messageOtherPlayersInTeams("messages.phases.spawnPlacement.start.player");

		gameTimer = new Timer(
				diamondRush.getPlugin(),
				diamondRush.getConfig().getSpawnPlacementDuration(),
				() -> Bukkit.getPluginManager().callEvent(new SpawnPlacementEndEvent()),
				"messages.phases.spawnPlacement.end"
		);
		gameTimer.run();
	}


	@EventHandler
	public void onSpawnPlacementEnd(SpawnPlacementEndEvent event) {
		for (Map.Entry<String, Team> teamEntry : diamondRush.getGame().getTeams().entrySet()) {
			Block spawnBlock;
			if (teamEntry.getValue().getSpawnBlock() == null) {
				Player leader = Bukkit.getPlayer(teamEntry.getValue().getLeaderUuid());
				if (leader == null) {
					continue;
				}
				spawnBlock = leader.getLocation().getBlock();
			}
			else {
				spawnBlock = teamEntry.getValue().getSpawnBlock();
			}
			CuboidRegion region = new CuboidRegion(spawnBlock, 3, 3, 3);
			region.create(new TotemFloorPattern(region, teamEntry.getValue().getTeamColor()));
			diamondRush.getGame().addRegion(teamEntry.getValue().getName() + "Spawn", region);
		}
		Bukkit.getPluginManager().callEvent(new ExplorationStartEvent());
	}


	@EventHandler
	public void onExplorationStart(ExplorationStartEvent event) {
		diamondRush.getGame().setPhase(GamePhase.EXPLORATION);
	}


	@EventHandler
	public void onTeamLoss(TeamLossEvent event) {
		// Increment defeated teams
		diamondRush.getGame().setDefeatedTeams(diamondRush.getGame().getDefeatedTeams() + 1);
		// Call game end if only 1 team remaining
		if (diamondRush.getGame().getDefeatedTeams() >= diamondRush.getGame().getTeams().size() - 1) {
			Bukkit.getPluginManager().callEvent(new GameEndEvent());
			return;
		}
		// Eliminate team and put its players in spectator mode
		for (UUID uuid : event.getTeam().getPlayerUUIDs()) {
			Player player = Bukkit.getPlayer(uuid);
			if (player == null) {
				continue;
			}
			player.setGameMode(GameMode.SPECTATOR);
		}
	}


	@EventHandler
	public void onGameEnd(GameEndEvent event) {
		diamondRush.getGame().resetPlayers();
		for (Map.Entry<String, Team> teamEntry : diamondRush.getGame().getTeams().entrySet()) {
			Team team = teamEntry.getValue();
			for (UUID uuid : team.getPlayerUUIDs()) {
				Player player = Bukkit.getPlayer(uuid);
				if (player == null) {
					continue;
				}
				player.teleportAsync(diamondRush.getGame().getSpawn());
				player.setGameMode(GameMode.ADVENTURE);
				team.getMinecraftTeam().removePlayer(player);
			}
			team.getMinecraftTeam().unregister();
		}
		gameTimer.cancel();
		diamondRush.setGame(null);
	}


	private void teleportPlayersToGameSpawn() {
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
				player.teleportAsync(startPosition);
			}

			// Calculate new angle for next team
			i += 360 / diamondRush.getGame().getTeams().size();
		}
	}
}
