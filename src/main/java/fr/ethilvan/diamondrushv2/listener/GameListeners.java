package fr.ethilvan.diamondrushv2.listener;


import com.destroystokyo.paper.event.player.PlayerJumpEvent;
import fr.ethilvan.diamondrushv2.DiamondRush;
import fr.ethilvan.diamondrushv2.config.RespawnEquipment;
import fr.ethilvan.diamondrushv2.event.TeamLossEvent;
import fr.ethilvan.diamondrushv2.game.GamePhase;
import fr.ethilvan.diamondrushv2.game.Team;
import fr.ethilvan.diamondrushv2.region.Region;
import fr.ethilvan.diamondrushv2.tools.ScoreboardTimer;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.*;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.*;
import org.bukkit.event.world.StructureGrowEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
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
		for (HashMap.Entry<String, Region> regionEntry : diamondRush.getGame().getRegions().entrySet()) {
			if (regionEntry.getValue().contains(event.getBlock())) {

				if (!diamondRush.getGame().getPhase().equals(GamePhase.EXPLORATION) &&
						!diamondRush.getGame().getPhase().equals(GamePhase.COMBAT)) {
					event.setCancelled(true);
					return;
				}

				if (event.getBlock().getType().equals(Material.OBSIDIAN)) {
					for (HashMap.Entry<String, Team> teamEntry : diamondRush.getGame().getTeams().entrySet()) {
						Team team = teamEntry.getValue();
						if (regionEntry.getKey().equals(teamEntry.getKey() + "Totem")) {
							int currentLives = team.getLives() - 1;
							team.setLives(currentLives);
							HashMap<String, String> placeholders = new HashMap<>();
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

			for (HashMap.Entry<String, Team> teamEntry : diamondRush.getGame().getTeams().entrySet()) {
				Team team = teamEntry.getValue();
				Block totemBlock = team.getTotemBlock();
				if (totemBlock == null) {
					continue;
				}
				if (totemBlock.getLocation().equals(event.getBlock().getLocation())) {
					Player player = event.getPlayer();
					if (!team.getPlayerUUIDs().contains(player.getUniqueId())) {
						return;
					}
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

			for (HashMap.Entry<String, Team> teamEntry : diamondRush.getGame().getTeams().entrySet()) {
				Team team = teamEntry.getValue();
				Block spawnBlock = team.getSpawnBlock();
				if (spawnBlock == null) {
					continue;
				}
				if (spawnBlock.getLocation().equals(event.getBlock().getLocation())) {
					Player player = event.getPlayer();
					if (!team.getPlayerUUIDs().contains(player.getUniqueId())) {
						return;
					}
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
		for (HashMap.Entry<String, Region> regionEntry : diamondRush.getGame().getRegions().entrySet()) {
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
		for (HashMap.Entry<String, Region> regionEntry : diamondRush.getGame().getRegions().entrySet()) {
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
		for (HashMap.Entry<String, Region> regionEntry : diamondRush.getGame().getRegions().entrySet()) {
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
		Material itemInHand = player.getInventory().getItemInMainHand().getType();
		Material itemInOffHand = player.getInventory().getItemInOffHand().getType();

		// Disable flint and steel
		if (itemInHand.equals(Material.FLINT_AND_STEEL) || itemInOffHand.equals(Material.FLINT_AND_STEEL)) {
			if (targetedBlock == null) {
				return;
			}
			Material targetedBlockType = targetedBlock.getType();
			// Allow nether portals
			if (!isRightClick || targetedBlockType.equals(Material.OBSIDIAN)) {
				return;
			}
			event.setCancelled(true);
		}

		// Allow surrender
		Material surrenderMaterial = Material.getMaterial(diamondRush.getConfig().getSurrenderMaterial());
		if (itemInHand.equals(surrenderMaterial)) {
			if (!diamondRush.getGame().getPhase().equals(GamePhase.COMBAT)) {
				return;
			}
			Team team = diamondRush.getGame().getTeam(player.getUniqueId());
			if (team == null) {
				return;
			}
			if (team.getDeaths() < diamondRush.getConfig().getMinDeathsForSurrender()) {
				return;
			}
			ScoreboardTimer gameTimer = diamondRush.getGame().getGameTimer();
			if (gameTimer == null) {
				return;
			}
			if (gameTimer.getRemainingTime() < 1) {
				return;
			}
			if (team.getSurrenders() >= diamondRush.getConfig().getMaxSurrendersPerTeam()) {
				diamondRush.messagePlayer(player, "messages.phases.combat.surrender.maxSurrendersReached");
				return;
			}
			player.getInventory().removeItem(new ItemStack(surrenderMaterial, 1));
			team.setSurrenders(team.getSurrenders() + 1);
			HashMap<String, String> placeholders = new HashMap<>();
			placeholders.put("\\{team-color\\}", team.getTeamColor().getColorName().toLowerCase());
			placeholders.put("\\{team-name\\}", team.getName());
			diamondRush.broadcastMessage("messages.phases.combat.surrender.success", placeholders);
			gameTimer.setRemainingTime(0);
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
			for (HashMap.Entry<String, Region> regionEntry : diamondRush.getGame().getRegions().entrySet()) {
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
			for (HashMap.Entry<String, Region> regionEntry : diamondRush.getGame().getRegions().entrySet()) {
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

		// Manage spectator inventory click
		Player player = (Player) event.getWhoClicked();
		if (diamondRush.getGame().getSpectatorUuids().contains(player.getUniqueId())) {
			event.setCancelled(true);

			ItemStack itemStack = event.getCurrentItem();
			if (itemStack == null) {
				return;
			}

			// Open spectator inventory on base player head click
			if (player.getInventory().equals(event.getClickedInventory())) {
				if (itemStack.getType() == Material.PLAYER_HEAD) {
					player.openInventory(diamondRush.getGame().getSpectatorInventory().getInventory());
					return;
				}
			}

			// Spectate player when clicking team player head in spectator inventory
			Inventory clickedInventory = event.getClickedInventory();
			if (clickedInventory == null) {
				return;
			}
			boolean isSameInventory = clickedInventory.equals(
					diamondRush.getGame().getSpectatorInventory().getInventory()
			);
			if (itemStack.getType().equals(Material.PLAYER_HEAD) && isSameInventory) {
				SkullMeta headMeta = (SkullMeta) itemStack.getItemMeta();
				if (headMeta == null) {
					return;
				}
				OfflinePlayer playerHeadOwner = headMeta.getOwningPlayer();
				if (playerHeadOwner == null) {
					return;
				}
				for (HashMap.Entry<String, Team> entry : diamondRush.getGame().getTeams().entrySet()) {
					Team team = entry.getValue();
					for (UUID teamPlayerUuid : team.getPlayerUUIDs()) {
						Player teamPlayer = Bukkit.getPlayer(teamPlayerUuid);
						if (teamPlayer == null) {
							continue;
						}
						if (!teamPlayer.getUniqueId().equals(playerHeadOwner.getUniqueId())) {
							continue;
						}
						if (!playerHeadOwner.isOnline()) {
							return;
						}
						if (player.getGameMode() != GameMode.SPECTATOR) {
							return;
						}
						player.setSpectatorTarget(teamPlayer);
						return;
					}
				}
			}
			// Teleport spectator to team totem
			for (HashMap.Entry<String, Team> entry : diamondRush.getGame().getTeams().entrySet()) {
				Team team = entry.getValue();
				if (itemStack.getType().equals(team.getTeamColor().getMaterial()) && isSameInventory) {
					Block totemBlock = team.getTotemBlock();
					if (totemBlock == null) {
						return;
					}
					int totemHeight = diamondRush.getConfig().getTotemHeight();
					player.setSpectatorTarget(null);
					player.teleport(team.getTotemBlock().getLocation().add(0, totemHeight, 0));
					return;
				}
			}
		} // End if spectators contains player

		// Prevent inventory click while the game is paused or in transition
		GamePhase phase = diamondRush.getGame().getPhase();
		if (phase.equals(GamePhase.TRANSITION) || phase.equals(GamePhase.PAUSE)) {
			event.setCancelled(true);
		}
	}


	@EventHandler
	public void onItemDrop(PlayerDropItemEvent event) {
		if (diamondRush.getGame() == null) {
			return;
		}
		GamePhase phase = diamondRush.getGame().getPhase();
		ItemStack droppedItem = event.getItemDrop().getItemStack();
		if (phase.equals(GamePhase.TOTEM_PLACEMENT) && droppedItem.getType().equals(Material.OBSIDIAN)) {
			event.setCancelled(true);
		}
		else if (phase.equals(GamePhase.SPAWN_PLACEMENT) && droppedItem.getType().equals(Material.CHISELED_STONE_BRICKS)) {
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

		// Check phase for inventory management
		GamePhase phase = diamondRush.getGame().getPhase();
		if (phase.equals(GamePhase.COMBAT)) {
			killed.getInventory().clear();
		}

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
		killedTeam.setDeaths(killedTeam.getDeaths() + 1);
		killerTeam.setKills(killerTeam.getKills() + 1);
		rewardPlayerForKill(killer);

		// Get respawn equipments
		List<RespawnEquipment> respawnEquipments = diamondRush.getConfig().getRespawnEquipments();
		for (RespawnEquipment respawnEquipment : respawnEquipments) {
			if (respawnEquipment.getStartCycle() <= diamondRush.getGame().getCycle()) {
				killed.getInventory().clear();
				// Armor
				killed.getInventory().setHelmet(new ItemStack(respawnEquipment.getHelmet()));
				killed.getInventory().setChestplate(new ItemStack(respawnEquipment.getChestplate()));
				killed.getInventory().setLeggings(new ItemStack(respawnEquipment.getLeggings()));
				killed.getInventory().setBoots(new ItemStack(respawnEquipment.getBoots()));
				// Weapon
				killed.getInventory().setItemInMainHand(new ItemStack(respawnEquipment.getWeaponMaterial()));
				// Item
				killed.getInventory().setItemInOffHand(new ItemStack(
						respawnEquipment.getItemMaterial(),
						respawnEquipment.getItemQuantity()
				));
			}
		}
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

		// Limit spectator messages to spectators
		if (diamondRush.getGame().getSpectatorUuids().contains(player.getUniqueId())) {
			messageSpectators(player, textComponent);
			return;
		}

		if (!diamondRush.getGame().getPhase().equals(GamePhase.COMBAT)) {
			// send to spectators (not combat)
			messageSpectators(player, textComponent);
			// Send to team members (not combat)
			for (UUID uuid : team.getPlayerUUIDs()) {
				Player teamPlayer = Bukkit.getPlayer(uuid);
				if (teamPlayer == null) {
					continue;
				}
				HashMap<String, String> placeholders = new HashMap<>();
				placeholders.put("\\{team-color\\}", team.getTeamColor().getColorName().toLowerCase());
				placeholders.put("\\{player-name\\}", player.getName());
				placeholders.put("\\{message\\}", textComponent.content());
				diamondRush.messagePlayer(teamPlayer, "messages.chatMessage", placeholders);
			}
			return;
		}

		// send to everyone (combat)
		String teamColor = "gray";
		if (team != null) {
			teamColor = team.getTeamColor().getColorName();
		}
		HashMap<String, String> placeholders = new HashMap<>();
		placeholders.put("\\{team-color\\}", teamColor.toLowerCase());
		placeholders.put("\\{player-name\\}", player.getName());
		placeholders.put("\\{message\\}", textComponent.content());
		diamondRush.broadcastMessage("messages.chatMessage", placeholders);
	}


	@EventHandler
	public void onEntitySpawn(EntitySpawnEvent event) {
		if (diamondRush.getGame() == null) {
			return;
		}
		if (event.getEntity().getType() == EntityType.PHANTOM) {
			event.setCancelled(true);
		}
	}


	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		if (diamondRush.getGame() == null) {
			return;
		}

		GamePhase gamePhase = diamondRush.getGame().getPhase();

		GamePhase[] survivalPhases = {
				GamePhase.CREATION,
				GamePhase.COMBAT,
				GamePhase.EXPLORATION,
				GamePhase.TOTEM_PLACEMENT,
				GamePhase.SPAWN_PLACEMENT,
				GamePhase.STARTING,
		};

		GamePhase[] creativePhases = {
				GamePhase.TRANSITION,
				GamePhase.PAUSE,
		};

		Player player = event.getPlayer();
		for (HashMap.Entry<String, Team> entry : diamondRush.getGame().getTeams().entrySet()) {
			Team team = entry.getValue();
			if (team.getPlayerUUIDs().contains(player.getUniqueId())) {
				if (Arrays.asList(survivalPhases).contains(gamePhase)) {
					player.setGameMode(GameMode.SURVIVAL);
				}
				else if (Arrays.asList(creativePhases).contains(gamePhase)) {
					player.setGameMode(GameMode.CREATIVE);
				}
			}
		}
	}


	private void changeLeader(Team team, Player newLeader) {
		team.setLeaderUuid(newLeader.getUniqueId());
		diamondRush.messagePlayer(newLeader, "messages.phases.leaderChange.leader");
		HashMap<String, String> placeholders = new HashMap<>();
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


	private void messageSpectators(@NotNull Player player, TextComponent textComponent) {
		for (UUID spectatorUuid : diamondRush.getGame().getSpectatorUuids()) {
			Player spectator = Bukkit.getPlayer(spectatorUuid);
			if (spectator == null) {
				continue;
			}
			HashMap<String, String> placeholders = new HashMap<>();
			placeholders.put("\\{team-color\\}", "dark_gray");
			placeholders.put("\\{player-name\\}", player.getName());
			placeholders.put("\\{message\\}", textComponent.content());
			diamondRush.messagePlayer(spectator, "messages.chatMessage", placeholders);
		}
	}
}
