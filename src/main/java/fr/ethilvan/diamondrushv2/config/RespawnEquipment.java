package fr.ethilvan.diamondrushv2.config;

import org.bukkit.Material;

import java.util.ArrayList;
import java.util.List;

public class RespawnEquipment {

	private final int startCycle;
	private final String armor;
	private final String weapon;
	private final String item;
	private final int itemQuantity;


	public RespawnEquipment(int startCycle, String armor, String weapon, String item, int itemQuantity) {
		this.startCycle = startCycle;
		this.armor = armor;
		this.weapon = weapon;
		this.item = item;
		this.itemQuantity = itemQuantity;

		if (!isStartCycleValid()) {
			throw new RuntimeException("The respawn equipment's start cycle must be superior to 0.");
		}

		if (!isArmorValid()) {
			throw new RuntimeException("The respawn equipment's armor must be one of the following: LEATHER, CHAINMAIL, GOLD, IRON, DIAMOND, NETHERITE.");
		}

		if (!isMaterialValid(weapon)) {
			throw new RuntimeException("The respawn equipment's weapon must be a valid Material.");
		}

		if (!isMaterialValid(item)) {
			throw new RuntimeException("The respawn equipment's item must be a valid Material.");
		}

		if (!isItemQuantityValid()) {
			throw new RuntimeException("The respawn equipment's item quantity must be superior to 0.");
		}
	}


	public int getStartCycle() {
		return startCycle;
	}

	public Material getWeaponMaterial() {
		return Material.getMaterial(weapon);
	}

	public Material getItemMaterial() {
		return Material.getMaterial(item);
	}

	public int getItemQuantity() {
		return itemQuantity;
	}


	public Material getHelmet() {
		return switch (armor) {
			case "LEATHER" -> Material.LEATHER_HELMET;
			case "CHAINMAIL" -> Material.CHAINMAIL_HELMET;
			case "GOLD" -> Material.GOLDEN_HELMET;
			case "IRON" -> Material.IRON_HELMET;
			case "DIAMOND" -> Material.DIAMOND_HELMET;
			case "NETHERITE" -> Material.NETHERITE_HELMET;
			default -> Material.AIR;
		};
	}

	public Material getChestplate() {
		return switch (armor) {
			case "LEATHER" -> Material.LEATHER_CHESTPLATE;
			case "CHAINMAIL" -> Material.CHAINMAIL_CHESTPLATE;
			case "GOLD" -> Material.GOLDEN_CHESTPLATE;
			case "IRON" -> Material.IRON_CHESTPLATE;
			case "DIAMOND" -> Material.DIAMOND_CHESTPLATE;
			case "NETHERITE" -> Material.NETHERITE_CHESTPLATE;
			default -> Material.AIR;
		};
	}

	public Material getLeggings() {
		return switch (armor) {
			case "LEATHER" -> Material.LEATHER_LEGGINGS;
			case "CHAINMAIL" -> Material.CHAINMAIL_LEGGINGS;
			case "GOLD" -> Material.GOLDEN_LEGGINGS;
			case "IRON" -> Material.IRON_LEGGINGS;
			case "DIAMOND" -> Material.DIAMOND_LEGGINGS;
			case "NETHERITE" -> Material.NETHERITE_LEGGINGS;
			default -> Material.AIR;
		};
	}

	public Material getBoots() {
		return switch (armor) {
			case "LEATHER" -> Material.LEATHER_BOOTS;
			case "CHAINMAIL" -> Material.CHAINMAIL_BOOTS;
			case "GOLD" -> Material.GOLDEN_BOOTS;
			case "IRON" -> Material.IRON_BOOTS;
			case "DIAMOND" -> Material.DIAMOND_BOOTS;
			case "NETHERITE" -> Material.NETHERITE_BOOTS;
			default -> Material.AIR;
		};
	}


	private boolean isStartCycleValid() {
		return startCycle > 0;
	}


	private boolean isArmorValid() {
		List<String> armors = new ArrayList<>();
		armors.add("NONE");
		armors.add("LEATHER");
		armors.add("CHAINMAIL");
		armors.add("GOLD");
		armors.add("IRON");
		armors.add("DIAMOND");
		armors.add("NETHERITE");

		return armors.contains(armor.toUpperCase());
	}


	private boolean isMaterialValid(String material) {
		return Material.getMaterial(material) != null;
	}


	private boolean isItemQuantityValid() {
		return itemQuantity > 0;
	}
}
