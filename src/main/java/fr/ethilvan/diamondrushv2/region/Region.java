package fr.ethilvan.diamondrushv2.region;

import fr.ethilvan.diamondrushv2.region.pattern.Pattern;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;

import java.util.List;

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

	public void create(Pattern pattern) {
		clear();
		pattern.create();
	}

	public void create(List<Pattern> patterns) {
		clear();
		for (Pattern pattern : patterns) {
			pattern.create();
		}
	}

	public abstract void clear();
}
