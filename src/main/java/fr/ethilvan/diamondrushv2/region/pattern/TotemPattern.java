package fr.ethilvan.diamondrushv2.region.pattern;

import fr.ethilvan.diamondrushv2.region.CuboidRegion;
import fr.ethilvan.diamondrushv2.region.Region;
import org.bukkit.Material;
import org.bukkit.block.Block;

public class TotemPattern extends Pattern {

	private final int totemHeight;


	public TotemPattern(Region region, int totemHeight) {
		super(region);
		this.totemHeight = totemHeight;
	}


	@Override
	public void create() {
		if (!(region instanceof CuboidRegion cuboidRegion)) {
			throw new RuntimeException("TotemFloorPattern must be applied to a CuboidRegion.");
		}
		Block center = cuboidRegion.getCenter();
		for (int y = center.getY(); y < center.getY() + totemHeight; y++) {
			// Place center blocks
			center.getWorld().getBlockAt(center.getX(), y, center.getZ()).setType(Material.OBSIDIAN);
			// Place east and west totem blocks
			if (y == center.getY() + totemHeight - 1) {
				center.getWorld().getBlockAt(center.getX() - 1, y, center.getZ()).setType(Material.OBSIDIAN);
				center.getWorld().getBlockAt(center.getX() + 1, y, center.getZ()).setType(Material.OBSIDIAN);
			}
		}
	}
}
