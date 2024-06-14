package fr.ethilvan.diamondrushv2.game;

import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.ScoreboardManager;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Team {

	private String name;
	private TeamColor teamColor;
	private org.bukkit.scoreboard.Team minecraftTeam;
	private final List<UUID> playerUUIDs;
	private UUID leaderUuid;
	private Block totemBlock = null;
	private Block spawnBlock = null;
	private int lives;

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

	public void addPlayerUuid(UUID uuid) {
		playerUUIDs.add(uuid);
		Player player = Bukkit.getPlayer(uuid);
		if (player != null) {
			minecraftTeam.addPlayer(player);
		}
	}

	public void removePlayerUuid(UUID uuid) {
		Player player = Bukkit.getPlayer(uuid);
		if (player != null) {
			minecraftTeam.removePlayer(player);
		}
		playerUUIDs.remove(uuid);
	}


	public UUID getLeaderUuid() {
		return leaderUuid;
	}

	public void setLeaderUuid(UUID leaderUuid) {
		this.leaderUuid = leaderUuid;
	}


	public Block getTotemBlock() {
		return totemBlock;
	}

	public void setTotemBlock(Block totemBlock) {
		this.totemBlock = totemBlock;
	}


	public Block getSpawnBlock() {
		return spawnBlock;
	}

	public void setSpawnBlock(Block spawnBlock) {
		this.spawnBlock = spawnBlock;
	}


	public int getLives() {
		return lives;
	}

	public void setLives(int lives) {
		this.lives = lives;
	}
}
