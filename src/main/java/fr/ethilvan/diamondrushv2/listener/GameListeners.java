package fr.ethilvan.diamondrushv2.listener;


import com.destroystokyo.paper.event.player.PlayerJumpEvent;
import fr.ethilvan.diamondrushv2.DiamondRush;
import fr.ethilvan.diamondrushv2.event.TeamLossEvent;
import fr.ethilvan.diamondrushv2.game.GamePhase;
import fr.ethilvan.diamondrushv2.game.Team;
import fr.ethilvan.diamondrushv2.region.Region;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.world.StructureGrowEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.projectiles.ProjectileSource;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


public class GameListeners implements Listener {

	private final DiamondRush diamondRush;


	public GameListeners(DiamondRush diamondRush) {
		this.diamondRush = diamondRush;
	}


	@EventHandler
	public void onPlayerRespawn(PlayerRespawnEvent event) {
		if (diamondRush.getGame() == null) {
			return;
		}
		Player player = event.getPlayer();

		Team team = diamondRush.getGame().getTeam(player.getUniqueId());
		if (team != null) {
			GamePhase phase = diamondRush.getGame().getPhase();
			if (phase.equals(GamePhase.EXPLORATION) || phase.equals(GamePhase.COMBAT)) {
				player.getInventory().clear();
			}

			Region teamSpawnRegion = diamondRush.getGame().getRegion(team.getName() + "Spawn");
			if (teamSpawnRegion == null) {
				event.setRespawnLocation(diamondRush.getGame().getSpawn());
				return;
			}
			event.setRespawnLocation(teamSpawnRegion.getTeleportLocation());
		}
	}


	@EventHandler
	public void onBlockBreak(BlockBreakEvent event) {
		if (diamondRush.getGame() == null) {
			return;
		}
		if (diamondRush.getGame().getPhase().equals(GamePhase.PAUSE) ||
				diamondRush.getGame().getPhase().equals(GamePhase.TRANSITION)) {
			event.setCancelled(true);
			return;
		}
		// Check for protected regions
		for (Map.Entry<String, Region> regionEntry : diamondRush.getGame().getRegions().entrySet()) {
			if (regionEntry.getValue().contains(event.getBlock())) {

				if (!diamondRush.getGame().getPhase().equals(GamePhase.EXPLORATION) &&
						!diamondRush.getGame().getPhase().equals(GamePhase.COMBAT)) {
					event.setCancelled(true);
					return;
				}

				if (event.getBlock().getType().equals(Material.OBSIDIAN)) {
					for (Map.Entry<String, Team> teamEntry : diamondRush.getGame().getTeams().entrySet()) {
						Team team = teamEntry.getValue();
						if (regionEntry.getKey().equals(teamEntry.getKey() + "Totem")) {
							int currentLives = team.getLives() - 1;
							team.setLives(currentLives);
							Map<String, String> placeholders = new HashMap<>();
							placeholders.put("\\{team-color\\}", team.getTeamColor().getColorName());
							placeholders.put("\\{team-name\\}", team.getName());
							placeholders.put("\\{lives\\}", String.valueOf(currentLives));
							diamondRush.broadcastMessage("messages.teamLosesLife", placeholders);
							if (currentLives == 0) {
								event.getBlock().setType(Material.AIR);
								Bukkit.getPluginManager().callEvent(new TeamLossEvent(team));
							}
							return;
						}
					}
					return;
				}

				event.setCancelled(true);
				return;
			}
		}
	}


	@EventHandler
	public void onBlockTap(BlockDamageEvent event) {
		if (diamondRush.getGame() == null) {
			return;
		}
		// Check for totem placement change
		if (diamondRush.getGame().getPhase().equals(GamePhase.TOTEM_PLACEMENT) &&
				event.getBlock().getType().equals(Material.OBSIDIAN)) {

			for (Map.Entry<String, Team> teamEntry : diamondRush.getGame().getTeams().entrySet()) {
				Team team = teamEntry.getValue();
				if (team.getTotemBlock().getLocation().equals(event.getBlock().getLocation())) {
					Player player = event.getPlayer();
					if (!team.getLeaderUuid().equals(player.getUniqueId())) {
						changeLeader(team, player);
					}
					event.getBlock().setType(Material.AIR);
					player.getInventory().setItemInMainHand(new ItemStack(Material.OBSIDIAN));
					team.setTotemBlock(null);
				}
			}
		}
		// Check for spawn placement change
		if (diamondRush.getGame().getPhase().equals(GamePhase.SPAWN_PLACEMENT) &&
				event.getBlock().getType().equals(Material.CHISELED_STONE_BRICKS)) {

			for (Map.Entry<String, Team> teamEntry : diamondRush.getGame().getTeams().entrySet()) {
				Team team = teamEntry.getValue();
				if (team.getSpawnBlock().getLocation().equals(event.getBlock().getLocation())) {
					Player player = event.getPlayer();
					if (!team.getLeaderUuid().equals(player.getUniqueId())) {
						changeLeader(team, player);
					}
					event.getBlock().setType(Material.AIR);
					player.getInventory().setItemInMainHand(new ItemStack(Material.CHISELED_STONE_BRICKS));
					team.setSpawnBlock(null);
				}
			}
		}
	}


	@EventHandler
	public void onBlockPlace(BlockPlaceEvent event) {
		if (diamondRush.getGame() == null) {
			return;
		}
		if (diamondRush.getGame().getPhase().equals(GamePhase.PAUSE) ||
				diamondRush.getGame().getPhase().equals(GamePhase.TRANSITION)) {
			event.setCancelled(true);
			return;
		}
		// Check for totem placement
		if (diamondRush.getGame().getPhase().equals(GamePhase.TOTEM_PLACEMENT) &&
				event.getBlock().getType().equals(Material.OBSIDIAN)) {

			Team team = diamondRush.getGame().getTeam(event.getPlayer().getUniqueId());
			if (team == null) {
				event.setCancelled(true);
				return;
			}
			if (!team.getLeaderUuid().equals(event.getPlayer().getUniqueId())) {
				event.setCancelled(true);
				return;
			}
			team.setTotemBlock(event.getBlock());
		}
		// Check for team spawn placement
		if (diamondRush.getGame().getPhase().equals(GamePhase.SPAWN_PLACEMENT) &&
				event.getBlock().getType().equals(Material.CHISELED_STONE_BRICKS)) {

			Team team = diamondRush.getGame().getTeam(event.getPlayer().getUniqueId());
			if (team == null) {
				event.setCancelled(true);
				return;
			}
			if (!team.getLeaderUuid().equals(event.getPlayer().getUniqueId())) {
				event.setCancelled(true);
				return;
			}
			team.setSpawnBlock(event.getBlock());
		}
		// Check for protected regions
		for (Map.Entry<String, Region> regionEntry : diamondRush.getGame().getRegions().entrySet()) {
			if (regionEntry.getValue().contains(event.getBlock())) {
				event.setCancelled(true);
				return;
			}
		}
	}


	@EventHandler
	public void onBlockChange(BlockFromToEvent event) {
		if (diamondRush.getGame() == null) {
			return;
		}
		for (Map.Entry<String, Region> regionEntry : diamondRush.getGame().getRegions().entrySet()) {
			if (regionEntry.getValue().contains(event.getToBlock())) {
				event.setCancelled(true);
				return;
			}
		}
	}


	@EventHandler
	public void onEmptyBucket(PlayerBucketEmptyEvent event) {
		if (diamondRush.getGame() == null) {
			return;
		}
		if (diamondRush.getGame().getPhase().equals(GamePhase.PAUSE) ||
				diamondRush.getGame().getPhase().equals(GamePhase.TRANSITION)) {
			event.setCancelled(true);
			return;
		}
		// Prevent use of lava buckets
		if (event.getBucket().equals(Material.LAVA_BUCKET)) {
			event.setCancelled(true);
			return;
		}
		// Prevent use of bucket in protected regions
		for (Map.Entry<String, Region> regionEntry : diamondRush.getGame().getRegions().entrySet()) {
			if (regionEntry.getValue().contains(event.getBlock())) {
				event.setCancelled(true);
				return;
			}
		}
	}


	@EventHandler
	public void onStructureGrow(StructureGrowEvent event) {
		if (diamondRush.getGame() == null) {
			return;
		}
		List<BlockState> blockStates = event.getBlocks();
		Iterator<BlockState> iterator = blockStates.iterator();
		while (iterator.hasNext()) {
			Block block = iterator.next().getBlock();
			for (Map.Entry<String, Region> regionEntry : diamondRush.getGame().getRegions().entrySet()) {
				if (regionEntry.getValue().contains(block)) {
					iterator.remove();
					break;
				}
			}
		}
	}


	@EventHandler
	public void onEntityExplode(EntityExplodeEvent event) {
		if (diamondRush.getGame() == null) {
			return;
		}
		List<Block> blocks = event.blockList();
		Iterator<Block> iterator = blocks.iterator();
		while (iterator.hasNext()) {
			Block block = iterator.next();
			for (Map.Entry<String, Region> regionEntry : diamondRush.getGame().getRegions().entrySet()) {
				if (regionEntry.getValue().contains(block)) {
					iterator.remove();
					break;
				}
			}
		}
	}


	@EventHandler
	public void onInventoryOpen(InventoryOpenEvent event) {
		if (diamondRush.getGame() == null) {
			return;
		}
		GamePhase phase = diamondRush.getGame().getPhase();
		if (phase.equals(GamePhase.TRANSITION) || phase.equals(GamePhase.PAUSE)) {
			event.setCancelled(true);
		}
	}


	@EventHandler
	public void onInventoryClick(InventoryClickEvent event) {
		if (diamondRush.getGame() == null) {
			return;
		}
		GamePhase phase = diamondRush.getGame().getPhase();
		if (phase.equals(GamePhase.TRANSITION) || phase.equals(GamePhase.PAUSE)) {
			event.setCancelled(true);
		}
	}


	@EventHandler
	public void onPlayerMove(PlayerMoveEvent event) {
		if (diamondRush.getGame() == null) {
			return;
		}
		Player player = event.getPlayer();
		GamePhase phase = diamondRush.getGame().getPhase();
		if ((phase.equals(GamePhase.TRANSITION) || phase.equals(GamePhase.PAUSE)) &&
				!player.getGameMode().equals(GameMode.SPECTATOR)) {
			event.setCancelled(true);
		}
	}


	@EventHandler
	public void onPlayerJump(PlayerJumpEvent event) {
		if (diamondRush.getGame() == null) {
			return;
		}
		GamePhase phase = diamondRush.getGame().getPhase();
		if ((phase.equals(GamePhase.TRANSITION) || phase.equals(GamePhase.PAUSE))) {
			event.setCancelled(true);
		}
	}


	@EventHandler
	public void onEntityDamageEntity(EntityDamageByEntityEvent event) {
		if (diamondRush.getGame() == null) {
			return;
		}
		Entity damager = event.getDamager();
		Entity target = event.getEntity();

		if (damager instanceof Player damagerPlayer) {
			GamePhase phase = diamondRush.getGame().getPhase();
			// Check for player damaging player
			if (target instanceof Player targetPlayer && !phase.equals(GamePhase.COMBAT)) {
				event.setCancelled(true); // cancel damage
				if (phase.equals(GamePhase.EXPLORATION)) {
					spot(damagerPlayer, targetPlayer);
				}
			}
		}
	}


	@EventHandler
	public void onProjectileHit(ProjectileHitEvent event) {
		if (diamondRush.getGame() == null) {
			return;
		}
		Entity target = event.getHitEntity();
		ProjectileSource damager = event.getEntity().getShooter();

		if (target instanceof Player targetPlayer && damager instanceof Player damagerPlayer) {
			GamePhase phase = diamondRush.getGame().getPhase();
			if (!phase.equals(GamePhase.COMBAT)) {
				event.setCancelled(true); // cancel damage
				if (phase.equals(GamePhase.EXPLORATION)) {
					spot(damagerPlayer, targetPlayer);
				}
			}
		}
	}


	private void changeLeader(Team team, Player newLeader) {
		team.setLeaderUuid(newLeader.getUniqueId());
		diamondRush.messagePlayer(newLeader, "messages.phases.leaderChange.leader");
		Map<String, String> placeholders = new HashMap<>();
		placeholders.put("\\{player\\}", newLeader.getName());
		diamondRush.messageOtherPlayersInTeam(team, "messages.phases.leaderChange.player", placeholders);
	}


	private void spot(Player damager, Player target) {
		Location location = target.getLocation();

		Team damagerTeam = diamondRush.getGame().getTeam(damager.getUniqueId());
		Team targetTeam = diamondRush.getGame().getTeam(target.getUniqueId());

		if (damagerTeam != null && targetTeam != null && !damagerTeam.equals(targetTeam)) {
			int distanceToSpot = diamondRush.getConfig().getDistanceToSpot();

			Region totemRegion = diamondRush.getGame().getRegion(damagerTeam.getName() + "Totem");
			Region spawnRegion = diamondRush.getGame().getRegion(targetTeam.getName() + "Spawn");
			Block totemBlock = totemRegion.getCenter();

			if (location.distance(totemBlock.getLocation()) <= distanceToSpot) {
				target.teleportAsync(spawnRegion.getTeleportLocation());
			}
		}
	}
}
