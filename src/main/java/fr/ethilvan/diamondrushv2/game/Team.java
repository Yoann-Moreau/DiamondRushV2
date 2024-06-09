package fr.ethilvan.diamondrushv2.game;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.ScoreboardManager;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Team {

	private String name;
	private TeamColor teamColor;
	private org.bukkit.scoreboard.Team minecraftTeam;
	private List<UUID> playerUUIDs;

	private final ScoreboardManager scoreboardManager;


	public Team(String name, TeamColor teamColor) {
		this.name = name;
		this.teamColor = teamColor;
		playerUUIDs = new ArrayList<>();

		scoreboardManager = Bukkit.getScoreboardManager();

		minecraftTeam = scoreboardManager.getMainScoreboard().getTeam(name);
		if (minecraftTeam == null) {
			minecraftTeam = scoreboardManager.getMainScoreboard().registerNewTeam(name);
		}
		minecraftTeam.color(teamColor.getChatColor());
		minecraftTeam.setOption(
				org.bukkit.scoreboard.Team.Option.NAME_TAG_VISIBILITY,
				org.bukkit.scoreboard.Team.OptionStatus.FOR_OWN_TEAM
		);
	}


	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}


	public TeamColor getTeamColor() {
		return teamColor;
	}

	public void setTeamColor(TeamColor teamColor) {
		this.teamColor = teamColor;
	}


	public org.bukkit.scoreboard.Team getMinecraftTeam() {
		return minecraftTeam;
	}

	public void setMinecraftTeam(org.bukkit.scoreboard.Team minecraftTeam) {
		this.minecraftTeam = minecraftTeam;
	}

	public List<UUID> getPlayerUUIDs() {
		return playerUUIDs;
	}

	public void addPlayer(UUID uuid) {
		playerUUIDs.add(uuid);
		Player player = Bukkit.getPlayer(uuid);
		if (player != null) {
			minecraftTeam.addPlayer(player);
		}
	}

	public void removePlayer(UUID uuid) {
		Player player = Bukkit.getPlayer(uuid);
		if (player != null) {
			minecraftTeam.removePlayer(player);
		}
		playerUUIDs.remove(uuid);
	}
}
