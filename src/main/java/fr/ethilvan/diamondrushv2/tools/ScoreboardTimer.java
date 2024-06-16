package fr.ethilvan.diamondrushv2.tools;

import fr.ethilvan.diamondrushv2.DiamondRush;
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

		init();
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


	public void cancel() {
		if (taskId != -1) {
			diamondRush.getPlugin().getScheduler().cancelTask(taskId);
			taskId = -1;
		}
	}
}
