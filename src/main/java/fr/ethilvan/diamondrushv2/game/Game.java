package fr.ethilvan.diamondrushv2.game;

import org.bukkit.Location;
import org.bukkit.World;

import java.util.HashMap;
import java.util.Map;

public class Game {

	private final World world;
	private final Location spawn;
	private final Map<String, Team> teams;


	public Game(World world, Location spawn) {
		this.world = world;
		this.spawn = spawn;
		this.teams = new HashMap<>();
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
}
