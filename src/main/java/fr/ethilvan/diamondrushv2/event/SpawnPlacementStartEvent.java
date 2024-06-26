package fr.ethilvan.diamondrushv2.event;

import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class SpawnPlacementStartEvent extends Event implements Cancellable {

	private static final HandlerList HANDLER_LIST = new HandlerList();
	private boolean cancelled = false;
	private final boolean firstPlacement;


	public SpawnPlacementStartEvent(boolean firstPlacement) {
		this.firstPlacement = firstPlacement;
	}


	@Override
	public @NotNull HandlerList getHandlers() {
		return HANDLER_LIST;
	}

	public static HandlerList getHandlerList() {
		return HANDLER_LIST;
	}


	@Override
	public boolean isCancelled() {
		return cancelled;
	}

	@Override
	public void setCancelled(boolean cancelled) {
		this.cancelled = cancelled;
	}


	public boolean isFirstPlacement() {
		return firstPlacement;
	}
}
