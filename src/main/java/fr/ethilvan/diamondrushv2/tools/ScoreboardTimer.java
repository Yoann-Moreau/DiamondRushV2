package fr.ethilvan.diamondrushv2.tools;

import fr.ethilvan.diamondrushv2.DiamondRush;
import fr.ethilvan.diamondrushv2.game.GamePhase;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scoreboard.*;

public class ScoreboardTimer implements Runnable {

	private final DiamondRush diamondRush;
	private int remainingTime;
	private final Runnable runnable;
	private String entryName;
	private final String entryNamePath;
	private final String endMessagePath;
	private int taskId;
	private int pauseTimer;



	public ScoreboardTimer(
			DiamondRush diamondRush,
			int duration,
			Runnable runnable,
			String entryNamePath,
			String endMessagePath
	) {
		this.diamondRush = diamondRush;
		this.remainingTime = duration;
		this.runnable = runnable == null ? () -> diamondRush.broadcastMessage(endMessagePath) : runnable;
		this.entryNamePath = entryNamePath;
		this.endMessagePath = endMessagePath;
		this.taskId = -1;
		this.pauseTimer = 0;

		init();
	}


	public int getRemainingTime() {
		return remainingTime;
	}

	public void setRemainingTime(int remainingTime) {
		this.remainingTime = remainingTime;
	}


	private void init() {
		entryName = diamondRush.getMessagesConfig().getString(entryNamePath);
		if (entryName == null) {
			diamondRush.missingMessage(entryNamePath);
			return;
		}
		initScoreboard();
	}


	private void initScoreboard() {
		Objective sidebar = diamondRush.getScoreboard().getObjective("sidebar");
		if (sidebar == null) {
			return;
		}
		Scoreboard scoreboard = sidebar.getScoreboard();
		if (scoreboard == null) {
			return;
		}
		for (String entry : scoreboard.getEntries()) {
			scoreboard.resetScores(entry);
		}
		Score timer = sidebar.getScore(entryName);
		timer.setScore(remainingTime);
	}


	@Override
	public void run() {
		BukkitScheduler scheduler = diamondRush.getPlugin().getScheduler();
		taskId = scheduler.scheduleSyncDelayedTask(diamondRush.getPlugin(), () -> {
			remainingTime--;
			Objective sidebar = diamondRush.getScoreboard().getObjective("sidebar");
			if (sidebar == null) {
				return;
			}
			Score timer = sidebar.getScore(entryName);
			timer.setScore(remainingTime);
			if (remainingTime > 0) {
				run();
				return;
			}
			diamondRush.broadcastMessage(endMessagePath);
			runnable.run();
		}, 20L);
	}


	public void pause() {
		diamondRush.getGame().setNextPhase(diamondRush.getGame().getPhase());
		diamondRush.getGame().setPhase(GamePhase.PAUSE);
		BukkitScheduler scheduler = diamondRush.getPlugin().getScheduler();
		scheduler.cancelTask(taskId);
		taskId = scheduler.scheduleSyncDelayedTask(diamondRush.getPlugin(), () -> {
			pauseTimer = 0;
			Objective sidebar = diamondRush.getScoreboard().getObjective("sidebar");
			if (sidebar == null) {
				return;
			}
			String pauseName = diamondRush.getMessagesConfig().getString("messages.phases.pause.name");
			if (pauseName == null) {
				diamondRush.missingMessage("messages.phases.pause.name");
				return;
			}
			Score timer = sidebar.getScore(pauseName);
			timer.setScore(pauseTimer);
			continuePause();
		}, 20L);
	}


	private void continuePause() {
		BukkitScheduler scheduler = diamondRush.getPlugin().getScheduler();
		taskId = scheduler.scheduleSyncDelayedTask(diamondRush.getPlugin(), () -> {
			pauseTimer++;
			Objective sidebar = diamondRush.getScoreboard().getObjective("sidebar");
			if (sidebar == null) {
				return;
			}
			String pauseName = diamondRush.getMessagesConfig().getString("messages.phases.pause.name");
			if (pauseName == null) {
				diamondRush.missingMessage("messages.phases.pause.name");
				return;
			}
			Score timer = sidebar.getScore(pauseName);
			timer.setScore(pauseTimer);
			continuePause();
		}, 20L);
	}


	public void resume() {
		BukkitScheduler scheduler = diamondRush.getPlugin().getScheduler();
		scheduler.cancelTask(taskId);
		diamondRush.getGame().setPhase(diamondRush.getGame().getNextPhase());
		GamePhase currentPhase = diamondRush.getGame().getPhase();
		// Reset pause timer
		Objective sidebar = diamondRush.getScoreboard().getObjective("sidebar");
		if (sidebar == null) {
			return;
		}
		String pauseName = diamondRush.getMessagesConfig().getString("messages.phases.pause.name");
		if (pauseName == null) {
			diamondRush.missingMessage("messages.phases.pause.name");
			return;
		}
		Score timer = sidebar.getScore(pauseName);
		timer.resetScore();
		// Set next phase
		if (currentPhase.equals(GamePhase.COMBAT)) {
			diamondRush.getGame().setNextPhase(GamePhase.EXPLORATION);
		}
		else if (currentPhase.equals(GamePhase.EXPLORATION)) {
			diamondRush.getGame().setNextPhase(GamePhase.COMBAT);
		}
		run();
	}


	public void cancel() {
		if (taskId != -1) {
			diamondRush.getPlugin().getScheduler().cancelTask(taskId);
			taskId = -1;
		}
	}
}
