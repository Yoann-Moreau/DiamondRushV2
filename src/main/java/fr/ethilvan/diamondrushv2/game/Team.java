package fr.ethilvan.diamondrushv2.game;

public class Team {

	private String name;
	private TeamColor teamColor;
	private org.bukkit.scoreboard.Team minecraftTeam;


	public Team(String name, TeamColor teamColor) {
		this.name = name;
		this.teamColor = teamColor;
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
}
