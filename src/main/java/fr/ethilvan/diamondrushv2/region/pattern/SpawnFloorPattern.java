package fr.ethilvan.diamondrushv2.region.pattern;

import fr.ethilvan.diamondrushv2.region.CylindricalRegion;
import fr.ethilvan.diamondrushv2.region.Region;
import org.bukkit.Material;
import org.bukkit.block.Block;

public class SpawnFloorPattern extends Pattern {

	public SpawnFloorPattern(Region region) {
		super(region);
	}


	@Override
	public void create() {
		if (!(region instanceof CylindricalRegion cylindricalRegion)) {
			throw new RuntimeException("SpawnFloorPattern must be applied to a CylindricalRegion.");
		}
		Block center = cylindricalRegion.getCenter();
		int radius = cylindricalRegion.getRadius();
		int y = center.getY() - 1;
		// Center block
		cylindricalRegion.getWorld().getBlockAt(center.getX(), y, center.getZ()).setType(Material.CHISELED_STONE_BRICKS);
		// Rest of floor
		for (int i = 0; i < 360; i++) {
			double angle = i * Math.PI / 180;
			for (int r = 1; r <= radius; r++) {
				int x = (int) Math.round(center.getX() + r * Math.cos(angle));
				int z = (int) Math.round(center.getZ() + r * Math.sin(angle));
				if (!cylindricalRegion.getWorld().getBlockAt(x, y, z).getType().equals(Material.STONE_BRICKS)) {
					cylindricalRegion.getWorld().getBlockAt(x, y, z).setType(Material.STONE_BRICKS);
				}
			}
		}
		// Torches
		int x;
		int z;
		y = y + 1;
		for (double i = 22.5; i < 360.0; i += 45 ) {
			double angle = i * Math.PI / 180;
			x = (int) Math.round(center.getX() + radius * Math.cos(angle));
			z = (int) Math.round(center.getZ() + radius * Math.sin(angle));
			cylindricalRegion.getWorld().getBlockAt(x, y, z).setType(Material.TORCH);
		}
	}
}
