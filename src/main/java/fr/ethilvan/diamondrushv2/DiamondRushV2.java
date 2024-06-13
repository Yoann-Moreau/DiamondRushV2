package fr.ethilvan.diamondrushv2;

import fr.ethilvan.diamondrushv2.command.DiamondRushCommand;
import fr.ethilvan.diamondrushv2.game.Team;
import fr.ethilvan.diamondrushv2.listener.GameListeners;
import fr.ethilvan.diamondrushv2.listener.GamePhaseListeners;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;

import java.util.Map;

public final class DiamondRushV2 extends JavaPlugin {

	private DiamondRush diamondRush;
	private BukkitScheduler scheduler;


	@Override
	public void onEnable() {
		this.diamondRush = new DiamondRush(this);
		this.scheduler = getServer().getScheduler();

		registerCommands();
		registerEvents();

		getLogger().info("Enabled.");
	}


	@Override
	public void onDisable() {

		// Unregister scoreboard teams
		if (diamondRush.getGame() != null) {
			for (Map.Entry<String, Team> teamEntry : diamondRush.getGame().getTeams().entrySet()) {
				teamEntry.getValue().getMinecraftTeam().unregister();
			}
		}

		getLogger().info("Disabled.");
	}


	private void registerCommands() {
		this.getCommand("diamondrush").setExecutor(new DiamondRushCommand(diamondRush));
	}


	private void registerEvents() {
		getServer().getPluginManager().registerEvents(new GamePhaseListeners(this.getDiamondRush()), this);
		getServer().getPluginManager().registerEvents(new GameListeners(this.getDiamondRush()), this);
	}


	public DiamondRush getDiamondRush() {
		return diamondRush;
	}

	public BukkitScheduler getScheduler() {
		return scheduler;
	}
}
