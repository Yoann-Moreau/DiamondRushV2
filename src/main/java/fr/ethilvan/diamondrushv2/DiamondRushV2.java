package fr.ethilvan.diamondrushv2;

import org.bukkit.plugin.java.JavaPlugin;

public final class DiamondRushV2 extends JavaPlugin {

	private DiamondRush diamondRush;


	@Override
	public void onEnable() {
		this.diamondRush = new DiamondRush(this);

		getLogger().info("Enabled.");
	}


	@Override
	public void onDisable() {
		getLogger().info("Disabled.");
	}
}
