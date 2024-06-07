package fr.ethilvan.diamondrushv2;

import fr.ethilvan.diamondrushv2.config.Config;

public class DiamondRush {

	private final DiamondRushV2 plugin;
	private final Config config;


	public DiamondRush(DiamondRushV2 plugin) {
		this.plugin = plugin;
		this.config = new Config(plugin);

		checkConfig();
	}


	public void checkConfig() {
		this.plugin.saveDefaultConfig();
		this.config.reload();
	}


	public Config getConfig() {
		return config;
	}

}
