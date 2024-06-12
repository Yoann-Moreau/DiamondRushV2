package fr.ethilvan.diamondrushv2.listener;

import fr.ethilvan.diamondrushv2.DiamondRush;
import fr.ethilvan.diamondrushv2.event.GameStartEvent;
import fr.ethilvan.diamondrushv2.game.GamePhase;
import fr.ethilvan.diamondrushv2.tools.Timer;
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
				this::startGame,
				"messages.phases.starting.end"
		);
		gameTimer.run();
	}

	private void startGame() {

	}
}
