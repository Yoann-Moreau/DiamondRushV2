package fr.ethilvan.diamondrushv2.game;

import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;

public enum TeamColor {

	BLUE("BLUE", NamedTextColor.BLUE, Material.BLUE_WOOL),
	RED("RED", NamedTextColor.DARK_RED, Material.RED_WOOL),
	GREEN("GREEN", NamedTextColor.DARK_GREEN, Material.RED_WOOL),
	ORANGE("ORANGE", NamedTextColor.GOLD, Material.ORANGE_WOOL),
	PURPLE("PURPLE", NamedTextColor.DARK_PURPLE, Material.PURPLE_WOOL),
	WHITE("WHITE", NamedTextColor.WHITE, Material.WHITE_WOOL),
	GRAY("GRAY", NamedTextColor.DARK_GRAY, Material.GRAY_WOOL),
	AQUA("AQUA", NamedTextColor.AQUA, Material.CYAN_WOOL),
	LIGHT_GREEN("LIGHT_GREEN", NamedTextColor.GREEN, Material.LIME_WOOL),
	BLACK("BLACK", NamedTextColor.BLACK, Material.BLACK_WOOL),
	YELLOW("YELLOW", NamedTextColor.YELLOW, Material.YELLOW_WOOL);


	private final String colorName;
	private final NamedTextColor chatColor;
	private final Material material;


	TeamColor(String colorName, NamedTextColor chatColor, Material material) {
		this.colorName = colorName;
		this.chatColor = chatColor;
		this.material = material;
	}


	public String getColorName() {
		return colorName;
	}

	public NamedTextColor getChatColor() {
		return chatColor;
	}

	public Material getMaterial() {
		return material;
	}
}
