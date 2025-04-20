package fr.ethilvan.diamondrushv2.listener;

import fr.ethilvan.diamondrushv2.DiamondRush;
import fr.ethilvan.diamondrushv2.config.ExplorationReward;
import fr.ethilvan.diamondrushv2.event.*;
import fr.ethilvan.diamondrushv2.game.GamePhase;
import fr.ethilvan.diamondrushv2.game.Team;
import fr.ethilvan.diamondrushv2.region.CuboidRegion;
import fr.ethilvan.diamondrushv2.region.CylindricalRegion;
import fr.ethilvan.diamondrushv2.region.Region;
import fr.ethilvan.diamondrushv2.region.pattern.Pattern;
import fr.ethilvan.diamondrushv2.region.pattern.TotemFloorPattern;
import fr.ethilvan.diamondrushv2.region.pattern.TotemPattern;
import fr.ethilvan.diamondrushv2.tools.ScoreboardTimer;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class GamePhaseListeners implements Listener {

	private final DiamondRush diamondRush;


	public GamePhaseListeners(DiamondRush diamondRush) {
		this.diamondRush = diamondRush;
	}


	@EventHandler
	public void onGameStart(GameStartEvent event) {
		diamondRush.getGame().setPhase(GamePhase.STARTING);
		diamondRush.broadcastMessage("messages.phases.starting.start");

		diamondRush.getGame().setGameTimer(new ScoreboardTimer(
				diamondRush,
				5,
				() -> Bukkit.getPluginManager().callEvent(new TotemPlacementStartEvent(true)),
				"messages.phases.starting.name",
				"messages.phases.starting.end"
		));
		diamondRush.getGame().getGameTimer().run();
	}


	@EventHandler
	public void onTotemPlacementStart(TotemPlacementStartEvent event) {
		diamondRush.getGame().setPhase(GamePhase.TOTEM_PLACEMENT);

		// First totem placement
		if (event.isFirstPlacement()) {
			diamondRush.getGame().assignLeaders();
			diamondRush.messageLeaders("messages.phases.totemPlacement.start.leader");
			diamondRush.messageOtherPlayersInTeams("messages.phases.totemPlacement.start.player");
			// Hide advancements
			diamondRush.getGame().getWorld().setGameRule(GameRule.ANNOUNCE_ADVANCEMENTS, false);
			diamondRush.getGame().getNetherWorld().setGameRule(GameRule.ANNOUNCE_ADVANCEMENTS, false);
			// Hide coordinates
			diamondRush.getGame().getWorld().setGameRule(GameRule.REDUCED_DEBUG_INFO, true);
			diamondRush.getGame().getNetherWorld().setGameRule(GameRule.REDUCED_DEBUG_INFO, true);

			diamondRush.getGame().resetPlayers();
			teleportPlayersToGameSpawn(false);

			// Give obsidian to leaders
			for (Map.Entry<String, Team> teamEntry : diamondRush.getGame().getTeams().entrySet()) {
				Player leader = Bukkit.getPlayer(teamEntry.getValue().getLeaderUuid());
				if (leader == null) {
					continue;
				}
				leader.getInventory().setItemInMainHand(new ItemStack(Material.OBSIDIAN));
			}
		}
		// New totem placement phase
		else {
			for (Map.Entry<String, Team> teamEntry : diamondRush.getGame().getTeams().entrySet()) {
				// Reset totem block
				if (teamEntry.getValue().getTotemBlock() != null) {
					teamEntry.getValue().getTotemBlock().setType(Material.AIR);
					teamEntry.getValue().setTotemBlock(null);
				}

				Player leader = Bukkit.getPlayer(teamEntry.getValue().getLeaderUuid());
				if (leader == null) {
					continue;
				}
				// Give obsidian to leaders if necessary
				if (!leader.getInventory().contains(Material.OBSIDIAN)) {
					leader.getInventory().setItemInMainHand(new ItemStack(Material.OBSIDIAN));
				}
			}
			teleportPlayersToGameSpawn(true);
		}

		diamondRush.getGame().getWorld().setTime(0);

		diamondRush.getGame().setGameTimer(new ScoreboardTimer(
				diamondRush,
				diamondRush.getConfig().getTotemPlacementDuration(),
				() -> Bukkit.getPluginManager().callEvent(new TotemPlacementEndEvent()),
				"messages.phases.totemPlacement.name",
				"messages.phases.totemPlacement.end.end"
		));
		diamondRush.getGame().getGameTimer().run();
	}


	@EventHandler
	public void onTotemPlacementEnd(TotemPlacementEndEvent event) {
		// Check for missing totem
		for (Map.Entry<String, Team> teamEntry : diamondRush.getGame().getTeams().entrySet()) {
			if (teamEntry.getValue().getTotemBlock() == null) {
				UUID leaderUuid = teamEntry.getValue().getLeaderUuid();
				Player leader = Bukkit.getPlayer(leaderUuid);
				// End game if leader is missing
				if (leader == null) {
					Bukkit.getPluginManager().callEvent(new GameEndEvent());
					return;
				}
				// Else put totemBlock at leader location
				Block playerLocationBlock = leader.getLocation().getBlock();
				Team team = teamEntry.getValue();
				team.setTotemBlock(playerLocationBlock);
				leader.getInventory().remove(Material.OBSIDIAN);
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
		Bukkit.getPluginManager().callEvent(new SpawnPlacementStartEvent(true));
	}


	@EventHandler
	public void onSpawnPlacementStart(SpawnPlacementStartEvent event) {
		diamondRush.getGame().setPhase(GamePhase.SPAWN_PLACEMENT);
		if (event.isFirstPlacement()) {
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
		}
		// Next spawn placements
		else {
			for (Map.Entry<String, Team> teamEntry : diamondRush.getGame().getTeams().entrySet()) {
				Team team = teamEntry.getValue();
				int minDistance = diamondRush.getConfig().getMinDistanceFromTotem();
				if (team.getSpawnBlock() == null) {
					continue;
				}
				if (team.getSpawnBlock().getLocation().distance(team.getTotemBlock().getLocation()) >= minDistance) {
					continue;
				}
				team.getSpawnBlock().setType(Material.AIR);
				team.setSpawnBlock(null);
				Player leader = Bukkit.getPlayer(team.getLeaderUuid());
				if (leader == null) {
					continue;
				}
				// Give chiseled stone bricks to leaders if necessary
				if (!leader.getInventory().contains(Material.CHISELED_STONE_BRICKS)) {
					leader.getInventory().setItemInMainHand(new ItemStack(Material.CHISELED_STONE_BRICKS));
				}
			}
		}

		diamondRush.getGame().setGameTimer(new ScoreboardTimer(
				diamondRush,
				diamondRush.getConfig().getSpawnPlacementDuration(),
				() -> Bukkit.getPluginManager().callEvent(new SpawnPlacementEndEvent()),
				"messages.phases.spawnPlacement.name",
				"messages.phases.spawnPlacement.end.end"
		));
		diamondRush.getGame().getGameTimer().run();
	}


	@EventHandler
	public void onSpawnPlacementEnd(SpawnPlacementEndEvent event) {
		for (Map.Entry<String, Team> teamEntry : diamondRush.getGame().getTeams().entrySet()) {
			Block spawnBlock;
			Player leader = Bukkit.getPlayer(teamEntry.getValue().getLeaderUuid());
			if (leader == null) {
				continue;
			}
			if (teamEntry.getValue().getSpawnBlock() == null) {
				spawnBlock = leader.getLocation().getBlock();
			}
			else {
				spawnBlock = teamEntry.getValue().getSpawnBlock();
			}
			// Check distance with totem
			Block totemBlock = teamEntry.getValue().getTotemBlock();
			int distance = (int) totemBlock.getLocation().distance(spawnBlock.getLocation());
			if (distance < diamondRush.getConfig().getMinDistanceFromTotem()) {
				Map<String, String> placeholders = new HashMap<>();
				placeholders.put("\\{team-color\\}", teamEntry.getValue().getTeamColor().getColorName().toLowerCase());
				placeholders.put("\\{team-name\\}", teamEntry.getValue().getName());
				diamondRush.broadcastMessage("messages.phases.spawnPlacement.end.goAgain", placeholders);

				Bukkit.getPluginManager().callEvent(new SpawnPlacementStartEvent(false));
				return;
			}
			// Check if block was placed
			if (teamEntry.getValue().getSpawnBlock() == null) {
				leader.getInventory().removeItem(new ItemStack(Material.CHISELED_STONE_BRICKS));
			}
			// Place spawn region
			CuboidRegion region = createSpawnRegion(totemBlock, spawnBlock);
			region.create(new TotemFloorPattern(region, teamEntry.getValue().getTeamColor()));
			diamondRush.getGame().addRegion(teamEntry.getValue().getName() + "Spawn", region);
			// Set compass target to team spawn
			for (UUID uuid : teamEntry.getValue().getPlayerUUIDs()) {
				Player player = Bukkit.getPlayer(uuid);
				if (player == null) {
					continue;
				}
				player.setCompassTarget(spawnBlock.getLocation());
			}
		}
		Bukkit.getPluginManager().callEvent(new ExplorationStartEvent());
	}


	@EventHandler
	public void onExplorationStart(ExplorationStartEvent event) {
		diamondRush.getGame().setPhase(GamePhase.EXPLORATION);
		diamondRush.getGame().setNextPhase(GamePhase.COMBAT);
		diamondRush.broadcastMessage("messages.phases.exploration.start");
		diamondRush.getGame().getWorld().setGameRule(GameRule.KEEP_INVENTORY, false);
		diamondRush.getGame().getNetherWorld().setGameRule(GameRule.KEEP_INVENTORY, false);

		changePlayersGameMode(GameMode.SURVIVAL);
		giveExplorationRewards();

		int firstExploration = diamondRush.getConfig().getFirstExplorationDuration();
		int explorationChange = diamondRush.getConfig().getExplorationChange();
		int cycle = diamondRush.getGame().getCycle();
		int numberOfChanges = diamondRush.getConfig().getNumberOfChanges();
		int duration = firstExploration + (cycle - 1) * explorationChange;
		if (cycle > numberOfChanges) {
			duration = firstExploration + numberOfChanges * explorationChange;
		}

		diamondRush.getGame().setGameTimer(new ScoreboardTimer(
				diamondRush,
				duration,
				() -> Bukkit.getPluginManager().callEvent(new TransitionStartEvent()),
				"messages.phases.exploration.name",
				"messages.phases.exploration.end"
		));
		diamondRush.getGame().getGameTimer().run();
	}


	@EventHandler
	public void onTransitionStart(TransitionStartEvent event) {
		diamondRush.getGame().setPhase(GamePhase.TRANSITION);
		diamondRush.broadcastMessage("messages.phases.transition.start");

		changePlayersGameMode(GameMode.CREATIVE);

		Runnable runnable;
		if (diamondRush.getGame().getNextPhase().equals(GamePhase.COMBAT)) {
			runnable = () -> Bukkit.getPluginManager().callEvent(new CombatStartEvent());
		}
		else {
			diamondRush.getGame().setCycle(diamondRush.getGame().getCycle() + 1);
			runnable = () -> Bukkit.getPluginManager().callEvent(new ExplorationStartEvent());
		}

		diamondRush.getGame().setGameTimer(new ScoreboardTimer(
				diamondRush,
				diamondRush.getConfig().getTransitionDuration(),
				runnable,
				"messages.phases.transition.name",
				"messages.phases.transition.end"
		));
		diamondRush.getGame().getGameTimer().run();
	}


	@EventHandler
	public void onCombatStart(CombatStartEvent event) {
		diamondRush.getGame().setPhase(GamePhase.COMBAT);
		diamondRush.getGame().setNextPhase(GamePhase.EXPLORATION);
		diamondRush.broadcastMessage("messages.phases.combat.start");
		diamondRush.getGame().getWorld().setGameRule(GameRule.KEEP_INVENTORY, true);
		diamondRush.getGame().getNetherWorld().setGameRule(GameRule.KEEP_INVENTORY, true);

		// Reset kills and deaths for teams
		for (Map.Entry<String, Team> teamEntry : diamondRush.getGame().getTeams().entrySet()) {
			teamEntry.getValue().setKills(0);
			teamEntry.getValue().setDeaths(0);
		}

		changePlayersGameMode(GameMode.SURVIVAL);

		int firstCombat = diamondRush.getConfig().getFirstCombatDuration();
		int combatChange = diamondRush.getConfig().getCombatChange();
		int cycle = diamondRush.getGame().getCycle();
		int numberOfChanges = diamondRush.getConfig().getNumberOfChanges();
		int duration = firstCombat + (cycle - 1) * combatChange;
		if (cycle > numberOfChanges) {
			duration = firstCombat + numberOfChanges * combatChange;
		}

		diamondRush.getGame().setGameTimer(new ScoreboardTimer(
				diamondRush,
				duration,
				() -> Bukkit.getPluginManager().callEvent(new TransitionStartEvent()),
				"messages.phases.combat.name",
				"messages.phases.combat.end"
		));
		diamondRush.getGame().getGameTimer().run();
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
		if (diamondRush.getGame().getGameTimer() != null) {
			diamondRush.getGame().getGameTimer().cancel();
		}
		diamondRush.resetScoreboard();
		diamondRush.setGame(null);
	}


	private void teleportPlayersToGameSpawn(boolean leadersOnly) {
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
			Block teamBlock = cylindricalRegion.getWorld().getBlockAt(x, y - 1, z);
			teamBlock.setType(teamEntry.getValue().getTeamColor().getMaterial());

			if (leadersOnly) {
				Player leader = Bukkit.getPlayer(teamEntry.getValue().getLeaderUuid());
				if (leader != null) {
					leader.teleportAsync(startPosition);
				}
			}
			else {
				for (UUID uuid : teamEntry.getValue().getPlayerUUIDs()) {
					Player player = Bukkit.getPlayer(uuid);
					if (player == null) {
						continue;
					}
					player.teleportAsync(startPosition);
				}
			}

			// Calculate new angle for next team
			i += 360 / diamondRush.getGame().getTeams().size();
		}
	}


	private void changePlayersGameMode(GameMode gameMode) {
		for (Map.Entry<String, Team> teamEntry : diamondRush.getGame().getTeams().entrySet()) {
			for (UUID uuid : teamEntry.getValue().getPlayerUUIDs()) {
				Player player = Bukkit.getPlayer(uuid);
				if (player == null) {
					continue;
				}
				player.setGameMode(gameMode);
			}
		}
	}


	private CuboidRegion createSpawnRegion(@NotNull Block totemBlock, @NotNull Block spawnBlock) {
		CuboidRegion region = new CuboidRegion(spawnBlock, 3, 3, 3);
		// Set teleport location (facing totem)
		double deltaX = totemBlock.getX() - spawnBlock.getX();
		double deltaZ = totemBlock.getZ() - spawnBlock.getZ();
		double angle = Math.atan(deltaZ / deltaX);
		int correction = 90;
		if (deltaX > 0) {
			correction = -90;
		}
		float yaw = (float) (angle * 180 / Math.PI) + correction;
		region.setTeleportLocation(
				new Location(spawnBlock.getWorld(),
						spawnBlock.getX() + 0.5,
						spawnBlock.getY(),
						spawnBlock.getZ() + 0.5,
						yaw,
						0
				));
		return region;
	}


	private void giveExplorationRewards() {
		int startCycle = diamondRush.getConfig().getRewardsStartCycle();
		if (diamondRush.getGame().getCycle() < startCycle) {
			return;
		}
		List<ExplorationReward> explorationRewards = diamondRush.getConfig().getExplorationRewards();
		for (Map.Entry<String, Team> teamEntry : diamondRush.getGame().getTeams().entrySet()) {
			Team team = teamEntry.getValue();
			for (ExplorationReward explorationReward : explorationRewards) {
				Material material = Material.getMaterial(explorationReward.getMaterial());
				int quantity = explorationReward.getQuantity();
				// If leader
				if (explorationReward.getWho().equals("leader")) {
					Player leader = Bukkit.getPlayer(team.getLeaderUuid());
					if (leader == null || material == null) {
						continue;
					}
					leader.getInventory().addItem(new ItemStack(material, quantity));
				}
				// If player
				else {
					for (UUID uuid : team.getPlayerUUIDs()) {
						Player player = Bukkit.getPlayer(uuid);
						if (player == null || material == null) {
							continue;
						}
						player.getInventory().addItem(new ItemStack(material, quantity));
					}
				}
			}
		}
	}
}
