package fr.ethilvan.diamondrushv2;

import fr.ethilvan.diamondrushv2.command.DiamondRushCommand;
import fr.ethilvan.diamondrushv2.game.Team;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Map;

public final class DiamondRushV2 extends JavaPlugin {

	private DiamondRush diamondRush;


	@Override
	public void onEnable() {
		this.diamondRush = new DiamondRush(this);

		registerCommands();

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
}
