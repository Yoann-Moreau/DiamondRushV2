package fr.ethilvan.diamondrushv2.config;

import fr.ethilvan.diamondrushv2.DiamondRushV2;
import org.bukkit.Material;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Config {

	private final DiamondRushV2 plugin;

	// Phases duration
	private int totemPlacementDuration = 60;
	private int spawnPlacementDuration = 20;
	private int firstExplorationDuration = 600;
	private int firstCombatDuration = 60;
	private int transitionDuration = 10;
	// Duration changes
	private int explorationChange = -120;
	private int combatChange = 60;
	private int numberOfChanges = 4;
	// Totems
	private int totemHeight = 3;
	// Team spawns
	private int minDistanceFromTotem = 25;
	// Spot
	private int distanceToSpot = 25;
	// Kill rewards
	private int nextKillsThreshold = 3;
	private String firstKillsMaterial = "DIAMOND";
	private int firstKillsQuantity = 1;
	private String nextKillsMaterial = "IRON_INGOT";
	private int nextKillsQuantity = 3;
	// Exploration rewards
	private int rewardsStartCycle = 2;
	private List<ExplorationReward> explorationRewards;
	// Respawn equipments
	private List<RespawnEquipment> respawnEquipments;


	public Config(DiamondRushV2 plugin) {
		this.plugin = plugin;
		explorationRewards = new ArrayList<>();
		respawnEquipments = new ArrayList<>();
	}


	public int getTotemPlacementDuration() {
		return Math.max(5, totemPlacementDuration);
	}

	public int getSpawnPlacementDuration() {
		return Math.max(5, spawnPlacementDuration);
	}

	public int getFirstExplorationDuration() {
		return Math.max(10, firstExplorationDuration);
	}

	public int getFirstCombatDuration() {
		return Math.max(10, firstCombatDuration);
	}

	public int getTransitionDuration() {
		return Math.max(1, transitionDuration);
	}

	public int getExplorationChange() {
		return explorationChange;
	}

	public int getCombatChange() {
		return combatChange;
	}

	public int getNumberOfChanges() {
		return Math.max(0, numberOfChanges);
	}

	public int getTotemHeight() {
		return Math.max(1, totemHeight);
	}

	public int getMinDistanceFromTotem() {
		return Math.max(10, minDistanceFromTotem);
	}

	public int getDistanceToSpot() {
		return Math.max(5, distanceToSpot);
	}

	public int getNextKillsThreshold() {
		return Math.max(1, nextKillsThreshold);
	}

	public String getFirstKillsMaterial() {
		return firstKillsMaterial;
	}

	public int getFirstKillsQuantity() {
		return Math.max(1, firstKillsQuantity);
	}

	public String getNextKillsMaterial() {
		return nextKillsMaterial;
	}

	public int getNextKillsQuantity() {
		return Math.max(1, nextKillsQuantity);
	}

	public int getRewardsStartCycle() {
		return Math.max(1, rewardsStartCycle);
	}

	public List<ExplorationReward> getExplorationRewards() {
		return explorationRewards;
	}

	public List<RespawnEquipment> getRespawnEquipments() {
		return respawnEquipments;
	}


	public void load() {
		totemPlacementDuration = plugin.getConfig().getInt("phases.duration.totemPlacement");
		spawnPlacementDuration = plugin.getConfig().getInt("phases.duration.spawnPlacement");
		firstExplorationDuration = plugin.getConfig().getInt("phases.duration.firstExploration");
		firstCombatDuration = plugin.getConfig().getInt("phases.duration.firstCombat");
		transitionDuration = plugin.getConfig().getInt("phases.duration.transition");
		explorationChange = plugin.getConfig().getInt("phases.durationChange.exploration");
		combatChange = plugin.getConfig().getInt("phases.durationChange.combat");
		numberOfChanges = plugin.getConfig().getInt("phases.durationChange.number");
		totemHeight = plugin.getConfig().getInt("totems.height");
		minDistanceFromTotem = plugin.getConfig().getInt("teamSpawns.minDistanceFromTotem");
		distanceToSpot = plugin.getConfig().getInt("distanceToSpot");
		nextKillsThreshold = plugin.getConfig().getInt("killRewards.nextKillsThreshold");
		firstKillsMaterial = plugin.getConfig().getString("killRewards.firstKills.material");
		firstKillsQuantity = plugin.getConfig().getInt("killRewards.firstKills.quantity");
		nextKillsMaterial = plugin.getConfig().getString("killRewards.nextKills.material");
		nextKillsQuantity = plugin.getConfig().getInt("killRewards.nextKills.quantity");
		rewardsStartCycle = plugin.getConfig().getInt("explorationRewards.startCycle");

		explorationRewards = new ArrayList<>();
		List<Map<?, ?>> explorationRewardsList = plugin.getConfig().getMapList("explorationRewards.rewards");
		for (Map<?, ?> rewardMap : explorationRewardsList) {
			explorationRewards.add(new ExplorationReward(
					(String) rewardMap.get("material"),
					(int) rewardMap.get("quantity"),
					(String) rewardMap.get("who")
			));
		}

		respawnEquipments = new ArrayList<>();
		List<Map<?, ?>> respawnEquipmentsList = plugin.getConfig().getMapList("respawnEquipments");
		for (Map<?, ?> equipmentMap : respawnEquipmentsList) {
			respawnEquipments.add(new RespawnEquipment(
					(int) equipmentMap.get("startCycle"),
					(String) equipmentMap.get("armor"),
					(String) equipmentMap.get("weapon"),
					(String) equipmentMap.get("item"),
					(int) equipmentMap.get("itemQuantity")
			));
		}

		checkForErrors();
	}


	public void checkForErrors() {
		if (Material.getMaterial(firstKillsMaterial) == null) {
			throw new RuntimeException("The firstKillsMaterial must be a valid Material.");
		}
		if (Material.getMaterial(nextKillsMaterial) == null) {
			throw new RuntimeException("The nextKillsMaterial must be a valid Material.");
		}
		if (firstExplorationDuration + explorationChange * numberOfChanges < 0) {
			throw new RuntimeException("The exploration time can't be inferior to 0.");
		}
		if (firstCombatDuration + combatChange * numberOfChanges < 0) {
			throw new RuntimeException("The combat time can't be inferior to 0.");
		}
	}
}
