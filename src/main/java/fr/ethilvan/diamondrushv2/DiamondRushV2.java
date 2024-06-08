package fr.ethilvan.diamondrushv2;

import fr.ethilvan.diamondrushv2.command.DiamondRushCommand;
import org.bukkit.plugin.java.JavaPlugin;

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
		getLogger().info("Disabled.");
	}


	private void registerCommands() {
		this.getCommand("diamondrush").setExecutor(new DiamondRushCommand(diamondRush));
	}
}
