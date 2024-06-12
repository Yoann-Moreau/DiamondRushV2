package fr.ethilvan.diamondrushv2.game;

import fr.ethilvan.diamondrushv2.region.Region;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.*;

public class Game {

	private final World world;
	private final Location spawn;
	private final Map<String, Team> teams;
	private final List<Region> regions;
	private GamePhase phase = GamePhase.CREATION;


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

	public GamePhase getPhase() {
		return phase;
	}

	public void setPhase(GamePhase phase) {
		this.phase = phase;
	}


	public void assignLeaders() {
		Random random = new Random();
		for (Map.Entry<String, Team> teamEntry : getTeams().entrySet()) {
			List<UUID> uuids = teamEntry.getValue().getPlayerUUIDs();
			UUID leaderUuid = uuids.get(random.nextInt(uuids.size()));
			teamEntry.getValue().setLeaderUuid(leaderUuid);
		}
	}


	public void resetPlayers() {
		for (Map.Entry<String, Team> teamEntry : getTeams().entrySet()) {
			for (UUID uuid : teamEntry.getValue().getPlayerUUIDs()) {
				Player player = Bukkit.getPlayer(uuid);
				if (player == null) {
					continue;
				}
				player.getInventory().clear();
				player.setHealth(20);
				player.setFoodLevel(20);
				player.setSaturation(5);
				player.setLevel(0);
				player.setExp(0);
				player.setGameMode(GameMode.SURVIVAL);
			}
		}
	}
}
