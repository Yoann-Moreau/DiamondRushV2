package fr.ethilvan.diamondrushv2.listener;


import fr.ethilvan.diamondrushv2.DiamondRush;
import fr.ethilvan.diamondrushv2.game.GamePhase;
import fr.ethilvan.diamondrushv2.game.Team;
import fr.ethilvan.diamondrushv2.region.Region;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockEvent;
import org.bukkit.event.block.BlockPlaceEvent;

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
		checkForProtectedRegions(event);
	}


	@EventHandler
	public void onBlockPlace(BlockPlaceEvent event) {
		if (diamondRush.getGame() == null) {
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


	private void checkForProtectedRegions(BlockEvent event) {
		for (Map.Entry<String, Region> regionEntry : diamondRush.getGame().getRegions().entrySet()) {
			if (regionEntry.getValue().contains(event.getBlock())) {

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
			}
		}
	}
}
