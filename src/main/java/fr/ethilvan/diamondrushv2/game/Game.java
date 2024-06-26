package fr.ethilvan.diamondrushv2.game;

import fr.ethilvan.diamondrushv2.DiamondRush;
import fr.ethilvan.diamondrushv2.region.Region;
import fr.ethilvan.diamondrushv2.tools.ScoreboardTimer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Criteria;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;

import java.util.*;


public class Game {

	private final DiamondRush diamondRush;
	private final World world;
	private final World netherWorld;
	private final Location spawn;
	private final Map<String, Team> teams;
	private final Map<String, Region> regions;
	private GamePhase phase = GamePhase.CREATION;
	private GamePhase nextPhase;
	private int cycle = 1;
	private int defeatedTeams = 0;
	private ScoreboardTimer gameTimer = null;


	public Game(DiamondRush diamondRush, World world, Location spawn) {
		this.diamondRush = diamondRush;
		this.world = world;
		this.spawn = spawn;
		this.teams = new HashMap<>();
		this.regions = new HashMap<>();

		String netherName = world.getName() + "_nether";
		this.netherWorld = Bukkit.getWorld(netherName);

		setupScoreboard();
	}


	public World getWorld() {
		return world;
	}


	public World getNetherWorld() {
		return netherWorld;
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

	public Team getTeam(UUID playerUuid) {
		for (Map.Entry<String, Team> teamEntry : getTeams().entrySet()) {
			if (teamEntry.getValue().getPlayerUUIDs().contains(playerUuid)) {
				return teamEntry.getValue();
			}
		}
		return null;
	}

	public void addTeam(String teamName, Team team) {
		teams.put(teamName, team);
	}

	public void removeTeam(String teamName) {
		teams.remove(teamName);
	}


	public Map<String, Region> getRegions() {
		return regions;
	}

	public Region getRegion(String regionName) {
		return regions.get(regionName);
	}

	public void addRegion(String regionName, Region region) {
		regions.put(regionName, region);
	}


	public GamePhase getPhase() {
		return phase;
	}

	public void setPhase(GamePhase phase) {
		this.phase = phase;
	}


	public GamePhase getNextPhase() {
		return nextPhase;
	}

	public void setNextPhase(GamePhase nextPhase) {
		this.nextPhase = nextPhase;
	}


	public int getCycle() {
		return cycle;
	}

	public void setCycle(int cycle) {
		this.cycle = cycle;
	}


	public int getDefeatedTeams() {
		return defeatedTeams;
	}

	public void setDefeatedTeams(int defeatedTeams) {
		this.defeatedTeams = defeatedTeams;
	}


	public ScoreboardTimer getGameTimer() {
		return gameTimer;
	}

	public void setGameTimer(ScoreboardTimer gameTimer) {
		this.gameTimer = gameTimer;
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


	private void setupScoreboard() {
		String displayNamePath = "messages.scoreboard.name";
		String displayName = diamondRush.getMessagesConfig().getString(displayNamePath);
		if (displayName == null) {
			diamondRush.missingMessage(displayNamePath);
			return;
		}
		Component component = Component.text(displayName, NamedTextColor.GOLD);
		Objective sidebar = diamondRush.getScoreboard().registerNewObjective("sidebar", Criteria.DUMMY, component);
		sidebar.setDisplaySlot(DisplaySlot.SIDEBAR);
	}
}
