package fr.ethilvan.diamondrushv2.region;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;

public abstract class Region {

	protected final Block center;
	protected final World world;
	protected Location teleportLocation;


	protected Region(Block center) {
		this.center = center;
		this.world = center.getWorld();
	}


	public Block getCenter() {
		return center;
	}


	public World getWorld() {
		return world;
	}


	public Location getTeleportLocation() {
		return teleportLocation;
	}

	public void setTeleportLocation(Location teleportLocation) {
		this.teleportLocation = teleportLocation;
	}


	public abstract boolean contains(Block block);

	public abstract void clear();
}
