package fr.ethilvan.diamondrushv2.config;

import org.bukkit.Material;

public class ExplorationReward {

	private final String material;
	private final int quantity;
	private final String who;


	public ExplorationReward(String material, int quantity, String who) {
		this.material = material;
		this.quantity = quantity;
		this.who = who;

		if (!isMaterialValid()) {
			throw new RuntimeException("Exploration reward 'material' must be a valid Material.");
		}
		if (!isQuantityValid()) {
			throw new RuntimeException("Exploration reward 'quantity' must be superior to 0.");
		}
		if (!isWhoValid()) {
			throw new RuntimeException("Exploration reward 'who' must be equal to 'leader' or 'player'.");
		}

	}


	public boolean isMaterialValid() {
		return Material.getMaterial(material) != null;
	}


	public boolean isQuantityValid() {
		return (quantity > 0);
	}


	public boolean isWhoValid() {
		return (who.equals("leader") || who.equals("player"));
	}
}
