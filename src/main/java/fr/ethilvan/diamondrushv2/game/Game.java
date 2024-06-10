package fr.ethilvan.diamondrushv2.game;

import fr.ethilvan.diamondrushv2.region.Region;
import org.bukkit.Location;
import org.bukkit.World;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Game {

	private final World world;
	private final Location spawn;
	private final Map<String, Team> teams;
	private final List<Region> regions;


	public Game(World world, Location spawn) {
		this.world = world;
		this.spawn = spawn;
		this.teams = new HashMap<>();
		this.regions = new ArrayList<>();
	}


	public World getWorld() {
		return world;
	}

	public Location getSpawn() {
		return spawn;
	}

	public Map<String, Team> getTeams() {
		return teams;
	}

	public Team getTeam(String teamName) {
		if (!teams.containsKey(teamName)) {
			return null;
		}
		return teams.get(teamName);
	}

	public void addTeam(String teamName, Team team) {
		teams.put(teamName, team);
	}

	public void removeTeam(String teamName) {
		teams.remove(teamName);
	}


	public List<Region> getRegions() {
		return regions;
	}

	public void addRegion(Region region) {
		regions.add(region);
	}
}
