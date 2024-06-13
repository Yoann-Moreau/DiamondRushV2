package fr.ethilvan.diamondrushv2.listener;


import fr.ethilvan.diamondrushv2.DiamondRush;
import fr.ethilvan.diamondrushv2.game.GamePhase;
import fr.ethilvan.diamondrushv2.game.Team;
import fr.ethilvan.diamondrushv2.region.Region;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;


public class GameListeners implements Listener {

	private final DiamondRush diamondRush;


	public GameListeners(DiamondRush diamondRush) {
		this.diamondRush = diamondRush;
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
		checkForProtectedRegions(event);
	}


	@EventHandler
	public void onBlockTap(BlockDamageEvent event) {
		if (diamondRush.getGame() == null) {
			return;
		}
		if (diamondRush.getGame().getPhase().equals(GamePhase.TOTEM_PLACEMENT) &&
				event.getBlock().getType().equals(Material.OBSIDIAN)) {

			for (Map.Entry<String, Team> teamEntry : diamondRush.getGame().getTeams().entrySet()) {
				Team team = teamEntry.getValue();
				if (team.getTotemBlock().getLocation().equals(event.getBlock().getLocation())) {
					Player player = event.getPlayer();
					if (!team.getLeaderUuid().equals(player.getUniqueId())) {
						// Change leader
						team.setLeaderUuid(player.getUniqueId());
						diamondRush.messagePlayer(player, "messages.phases.leaderChange.leader");
						Map<String, String> placeholders = new HashMap<>();
						placeholders.put("\\{player\\}", player.getName());
						diamondRush.messageOtherPlayersInTeam(team, "messages.phases.leaderChange.player", placeholders);
					}
					event.getBlock().setType(Material.AIR);
					player.getInventory().setItemInMainHand(new ItemStack(Material.OBSIDIAN));
					team.setTotemBlock(null);
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
		// Check for protected regions
		checkForProtectedRegions(event);
	}


	@EventHandler
	public void onBlockChange(BlockFromToEvent event) {
		if (diamondRush.getGame() == null) {
			return;
		}
		checkForProtectedRegions(event);
	}


	private void checkForProtectedRegions(BlockEvent event) {
		for (Map.Entry<String, Region> regionEntry : diamondRush.getGame().getRegions().entrySet()) {
			if (regionEntry.getValue().contains(event.getBlock())) {

				// Prevent breaking blocks
				if (event instanceof BlockBreakEvent blockBreakEvent) {

					if (!diamondRush.getGame().getPhase().equals(GamePhase.EXPLORATION) &&
							!diamondRush.getGame().getPhase().equals(GamePhase.COMBAT)) {

						blockBreakEvent.setCancelled(true);
						return;
					}

					if (blockBreakEvent.getBlock().getType().equals(Material.OBSIDIAN)) {
						return;
					}
					blockBreakEvent.setCancelled(true);
					return;
				}
				// Prevent placing blocks
				else if (event instanceof BlockPlaceEvent blockPlaceEvent) {
					blockPlaceEvent.setCancelled(true);
					return;
				}
			}
			// Prevent changing blocks
			if (event instanceof BlockFromToEvent blockFromToEvent) {
				if (regionEntry.getValue().contains(blockFromToEvent.getToBlock())) {
					blockFromToEvent.setCancelled(true);
					return;
				}
			}
		}
	}
}
