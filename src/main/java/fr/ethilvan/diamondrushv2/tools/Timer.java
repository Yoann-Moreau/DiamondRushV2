package fr.ethilvan.diamondrushv2.tools;

import com.google.common.base.Stopwatch;
import fr.ethilvan.diamondrushv2.DiamondRushV2;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class Timer implements Runnable {

	private final DiamondRushV2 plugin;
	private final Runnable runnable;
	private final String endMessagePath;

	private final int majorDuration = 30;
	private final int mediumDuration = 10;
	private final int minorDuration = 5;

	private int taskId;
	private int remainingTime;
	private int currentDelay;
	private long pauseDelay;

	private Stopwatch stopwatch;


	public Timer(DiamondRushV2 plugin, int duration, Runnable runnable, String endMessagePath) {
		this.plugin = plugin;
		this.endMessagePath = endMessagePath;

		this.taskId = -1;
		this.remainingTime = duration;
		this.currentDelay = 0;

		this.runnable = runnable == null ? () -> plugin.getDiamondRush().broadcastMessage(endMessagePath) : runnable;
	}

	@Override
	public void run() {
		remainingTime -= currentDelay;
		if (remainingTime >= majorDuration + mediumDuration) {
			scheduleAndBroadcastTime(majorDuration, "major");
		}
		else if (remainingTime >= mediumDuration + minorDuration) {
			scheduleAndBroadcastTime(mediumDuration, "medium");
		}
		else if (remainingTime > 0) {
			scheduleAndBroadcastTime(1, "minor");
		}
		else {
			plugin.getDiamondRush().broadcastMessage(endMessagePath);
			runnable.run();
		}
	}


	private void scheduleAndBroadcastTime(int delay, String delayCategory) {
		Map<String, String> placeholders = new HashMap<>();
		placeholders.put("\\{timer\\}", String.valueOf(remainingTime));
		plugin.getDiamondRush().broadcastMessage("messages.timer." + delayCategory, placeholders);
		schedule(delay);
	}


	private void schedule(int delay) {
		currentDelay = delay;
		stopwatch = Stopwatch.createStarted();
		taskId = plugin.getScheduler().scheduleSyncDelayedTask(plugin, this, delay * 20L);
	}


	public void cancel() {
		if (taskId != -1) {
			plugin.getScheduler().cancelTask(taskId);
			taskId = -1;
		}
	}


	public void pause() {
		cancel();
		stopwatch.stop();
		pauseDelay = stopwatch.elapsed(TimeUnit.SECONDS);
		remainingTime -= (int) pauseDelay;
	}


	public void resume() {
		int delay = (int) (currentDelay - pauseDelay);
		if (delay > majorDuration) {
			scheduleAndBroadcastTime(majorDuration, "major");
		}
		else if (delay > mediumDuration) {
			scheduleAndBroadcastTime(mediumDuration, "medium");
		}
		else if (delay > 0) {
			scheduleAndBroadcastTime(minorDuration, "minor");
		}
		else {
			runnable.run();
		}
	}
}
