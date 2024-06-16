package fr.ethilvan.diamondrushv2;

import fr.ethilvan.diamondrushv2.command.DiamondRushCommand;
import fr.ethilvan.diamondrushv2.game.Team;
import fr.ethilvan.diamondrushv2.listener.GameListeners;
import fr.ethilvan.diamondrushv2.listener.GamePhaseListeners;
import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;

import java.util.Map;

public final class DiamondRushV2 extends JavaPlugin {

	private DiamondRush diamondRush;
	private BukkitScheduler scheduler;
	private Listener[] listeners;


	@Override
	public void onEnable() {
		this.diamondRush = new DiamondRush(this);
		this.scheduler = getServer().getScheduler();
		this.listeners = new Listener[2];

		listeners[0] = new GamePhaseListeners(this.getDiamondRush());
		listeners[1] = new GameListeners(this.getDiamondRush());

		registerCommands();
		registerEvents();

		getLogger().info("Enabled.");
	}


	@Override
	public void onDisable() {

		// Unregister scoreboard teams
		if (diamondRush.getGame() != null) {
			for (Map.Entry<String, Team> teamEntry : diamondRush.getGame().getTeams().entrySet()) {
				if (teamEntry.getValue().getMinecraftTeam() != null) {
					teamEntry.getValue().getMinecraftTeam().unregister();
				}
			}
		}

		diamondRush.resetScoreboard();

		getLogger().info("Disabled.");
	}


	private void registerCommands() {
		this.getCommand("diamondrush").setExecutor(new DiamondRushCommand(diamondRush));
	}


	public void registerEvents() {
		PluginManager pluginManager = Bukkit.getPluginManager();
		for (Listener listener : listeners) {
			pluginManager.registerEvents(listener, this);
		}
	}

	public void unregisterEvents() {
		for (Listener listener : listeners) {
			HandlerList.unregisterAll(listener);
		}
	}


	public DiamondRush getDiamondRush() {
		return diamondRush;
	}

	public BukkitScheduler getScheduler() {
		return scheduler;
	}

	public Listener[] getListeners() {
		return listeners;
	}
}
