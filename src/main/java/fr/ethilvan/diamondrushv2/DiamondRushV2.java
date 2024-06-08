package fr.ethilvan.diamondrushv2;

import fr.ethilvan.diamondrushv2.command.DiamondRushCommand;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.plugin.lifecycle.event.LifecycleEventManager;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import org.bukkit.plugin.Plugin;
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
		LifecycleEventManager<Plugin> manager = this.getLifecycleManager();
		manager.registerEventHandler(LifecycleEvents.COMMANDS, event -> {
			final Commands commands = event.registrar();
			commands.register("diamondrush", new DiamondRushCommand(diamondRush));
		});
	}
}
