package fr.ethilvan.diamondrushv2.region.pattern;

import fr.ethilvan.diamondrushv2.game.TeamColor;
import fr.ethilvan.diamondrushv2.region.CuboidRegion;
import fr.ethilvan.diamondrushv2.region.Region;
import org.bukkit.Material;
import org.bukkit.block.Block;

public class TotemFloorPattern extends Pattern {

	private final TeamColor teamColor;


	public TotemFloorPattern(Region region, TeamColor teamColor) {
		super(region);
		this.teamColor = teamColor;
	}


	@Override
	public void create() {
		if (!(region instanceof CuboidRegion cuboidRegion)) {
			throw new RuntimeException("TotemFloorPattern must be applied to a CuboidRegion.");
		}
		Block center = cuboidRegion.getCenter();
		// Floor
		int y = center.getY() - 1;
		Block min = cuboidRegion.getMin();
		Block max = cuboidRegion.getMax();
		Material material;
		for (int x = min.getX(); x <= max.getX(); x++) {
			for (int z = min.getZ(); z <= max.getZ(); z++) {
				// Corners
				if ((x == min.getX() && z == min.getZ()) || (x == min.getX() && z == max.getZ()) ||
						(x == max.getX() && z == min.getZ()) || (x == max.getX() && z == max.getZ())) {
					material = teamColor.getMaterial();
				}
				// Center block
				else if ((x == center.getX() && z == center.getZ()) || x == min.getX() || x == max.getX() ||
						z == min.getZ() || z == max.getZ()) {
					material = Material.CHISELED_STONE_BRICKS;
				}
				// Rest of floor
				else {
					material = Material.STONE_BRICKS;
				}
				cuboidRegion.getWorld().getBlockAt(x, y, z).setType(material);
			}
		}
		// Torches
		y = y + 1;
		for (int x = min.getX(); x <= max.getX(); x++) {
			for (int z = min.getZ(); z <= max.getZ(); z++) {
				if (x != min.getX() || x != max.getX()) {
					continue;
				}
				if (z != min.getZ() || z != max.getZ()) {
					continue;
				}
				cuboidRegion.getWorld().getBlockAt(x, y, z).setType(Material.TORCH);
			}
		}
	}
}
