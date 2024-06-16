package fr.ethilvan.diamondrushv2.listener;


import com.destroystokyo.paper.event.player.PlayerJumpEvent;
import fr.ethilvan.diamondrushv2.DiamondRush;
import fr.ethilvan.diamondrushv2.event.TeamLossEvent;
import fr.ethilvan.diamondrushv2.game.GamePhase;
import fr.ethilvan.diamondrushv2.game.Team;
import fr.ethilvan.diamondrushv2.region.Region;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.TextComponent;
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
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.world.StructureGrowEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.projectiles.ProjectileSource;
import org.jetbrains.annotations.NotNull;

import java.util.*;


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
							placeholders.put("\\{team-color\\}", team.getTeamColor().getColorName().toLowerCase());
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
	public void onInteract(PlayerInteractEvent event) {
		if (diamondRush.getGame() == null) {
			return;
		}
		Player player = event.getPlayer();
		boolean isRightClick = event.getAction().isRightClick();
		Block targetedBlock = event.getClickedBlock();
		if (targetedBlock == null) {
			return;
		}
		Material targetedBlockType = targetedBlock.getType();
		Material itemInHand = player.getInventory().getItemInMainHand().getType();
		Material itemInOffHand = player.getInventory().getItemInOffHand().getType();
		if (!isRightClick || targetedBlockType.equals(Material.OBSIDIAN)) {
			return;
		}
		if (!itemInHand.equals(Material.FLINT_AND_STEEL) && !itemInOffHand.equals(Material.FLINT_AND_STEEL)) {
			return;
		}
		event.setCancelled(true);
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
			Location to = event.getFrom();
			to.setPitch(event.getTo().getPitch());
			to.setYaw(event.getTo().getYaw());
			event.setTo(to);
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


	@EventHandler
	public void onPlayerDeath(PlayerDeathEvent event) {
		if (diamondRush.getGame() == null) {
			return;
		}
		Player killed = event.getPlayer();
		Player killer = event.getPlayer().getKiller();
		if (killer == null || killer.equals(killed)) {
			return;
		}

		Team killerTeam = diamondRush.getGame().getTeam(killer.getUniqueId());
		Team killedTeam = diamondRush.getGame().getTeam(killed.getUniqueId());
		if (killedTeam == null || killerTeam == null || killerTeam.equals(killedTeam)) {
			return;
		}
		// Increment kills for team
		killerTeam.setKills(killerTeam.getKills() + 1);
		rewardPlayerForKill(killer);
	}


	@EventHandler
	public void onPlayerChat(AsyncChatEvent event) {
		if (diamondRush.getGame() == null) {
			return;
		}
		if (diamondRush.getGame().getPhase().equals(GamePhase.CREATION)) {
			return;
		}
		TextComponent textComponent = (TextComponent) event.message();
		event.setCancelled(true);
		Player player = event.getPlayer();
		Team team = diamondRush.getGame().getTeam(player.getUniqueId());
		if (!diamondRush.getGame().getPhase().equals(GamePhase.COMBAT)) {
			// send to spectators if not in team
			if (team == null) {
				for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
					if (!onlinePlayer.getGameMode().equals(GameMode.SPECTATOR)) {
						continue;
					}
					Map<String, String> placeholders = new HashMap<>();
					placeholders.put("\\{team-color\\}", "dark_gray");
					placeholders.put("\\{player-name\\}", player.getName());
					placeholders.put("\\{message\\}", textComponent.content());
					diamondRush.messagePlayer(onlinePlayer, "messages.chatMessage", placeholders);
				}
				return;
			}
			// Send to team members
			for (UUID uuid : team.getPlayerUUIDs()) {
				Player teamPlayer = Bukkit.getPlayer(uuid);
				if (teamPlayer == null) {
					continue;
				}
				Map<String, String> placeholders = new HashMap<>();
				placeholders.put("\\{team-color\\}", team.getTeamColor().getColorName().toLowerCase());
				placeholders.put("\\{player-name\\}", player.getName());
				placeholders.put("\\{message\\}", textComponent.content());
				diamondRush.messagePlayer(teamPlayer, "messages.chatMessage", placeholders);
			}
			return;
		}
		// send to everyone
		String teamColor = "gray";
		if (team != null) {
			teamColor = team.getTeamColor().getColorName();
		}
		Map<String, String> placeholders = new HashMap<>();
		placeholders.put("\\{team-color\\}", teamColor.toLowerCase());
		placeholders.put("\\{player-name\\}", player.getName());
		placeholders.put("\\{message\\}", textComponent.content());
		diamondRush.broadcastMessage("messages.chatMessage", placeholders);
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


	private void rewardPlayerForKill(@NotNull Player player) {
		int nextKillsThreshold = diamondRush.getConfig().getNextKillsThreshold();
		String firstKillsMaterialString = diamondRush.getConfig().getFirstKillsMaterial();
		int firstKillsQuantity = diamondRush.getConfig().getFirstKillsQuantity();
		String nextKillsMaterialString = diamondRush.getConfig().getNextKillsMaterial();
		int nextKillsQuantity = diamondRush.getConfig().getNextKillsQuantity();

		Team playerTeam = diamondRush.getGame().getTeam(player.getUniqueId());
		if (playerTeam == null) {
			return;
		}
		if (playerTeam.getKills() < nextKillsThreshold) {
			Material firstKillsMaterial = Material.getMaterial(firstKillsMaterialString);
			if (firstKillsMaterial == null) {
				return;
			}
			player.getInventory().addItem(new ItemStack(firstKillsMaterial, firstKillsQuantity));
		}
		else {
			Material nextKillsMaterial = Material.getMaterial(nextKillsMaterialString);
			if (nextKillsMaterial == null) {
				return;
			}
			player.getInventory().addItem(new ItemStack(nextKillsMaterial, nextKillsQuantity));
		}
		diamondRush.messagePlayer(player, "messages.killReward");
	}
}
