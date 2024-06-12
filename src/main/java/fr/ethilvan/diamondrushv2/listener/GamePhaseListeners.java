package fr.ethilvan.diamondrushv2.listener;

import fr.ethilvan.diamondrushv2.DiamondRush;
import fr.ethilvan.diamondrushv2.event.GameStartEvent;
import fr.ethilvan.diamondrushv2.event.TotemPlacementStartEvent;
import fr.ethilvan.diamondrushv2.game.GamePhase;
import fr.ethilvan.diamondrushv2.tools.Timer;
import org.bukkit.Bukkit;
import org.bukkit.GameRule;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class GamePhaseListeners implements Listener {

	private final DiamondRush diamondRush;
	private Timer gameTimer = null;


	public GamePhaseListeners(DiamondRush diamondRush) {
		this.diamondRush = diamondRush;
	}


	@EventHandler
	public void onGameStart(GameStartEvent event) {
		diamondRush.getGame().setPhase(GamePhase.STARTING);
		diamondRush.broadcastMessage("messages.phases.starting.start");

		gameTimer = new Timer(
				diamondRush.getPlugin(),
				5,
				() -> Bukkit.getPluginManager().callEvent(new TotemPlacementStartEvent()),
				"messages.phases.starting.end"
		);
		gameTimer.run();
	}


	@EventHandler
	public void onTotemPlacementStart(TotemPlacementStartEvent event) {
		diamondRush.getGame().setPhase(GamePhase.TOTEM_PLACEMENT);
		diamondRush.getGame().assignLeaders();
		diamondRush.messageLeaders("messages.phases.totemPlacement.start.leader");
		diamondRush.messageOtherPlayersInTeam("messages.phases.totemPlacement.start.player");

		diamondRush.getGame().getWorld().setGameRule(GameRule.KEEP_INVENTORY, true);
		diamondRush.getGame().getWorld().setGameRule(GameRule.ANNOUNCE_ADVANCEMENTS, false);
		diamondRush.getGame().getWorld().setTime(0);

		diamondRush.getGame().resetPlayers();

		gameTimer = new Timer(
				diamondRush.getPlugin(),
				diamondRush.getConfig().getTotemPlacementDuration(),
				() -> {

				},
				"messages.phases.totemPlacement.end"
		);
		gameTimer.run();
	}
}
