package fr.ethilvan.diamondrushv2.region;

import org.bukkit.Material;
import org.bukkit.block.Block;

public class CylindricalRegion extends Region {

	private final int radius;
	private final int height;


	public CylindricalRegion(Block center, int radius, int height) {
		super(center);
		this.radius = radius;
		this.height = height;
	}


	@Override
	public boolean contains(Block block) {
		return false;
	}

	@Override
	public void clear() {
		for (int y = center.getY() - 1; y <= center.getY() + height; y++) {
			for (int i = 0; i < 360; i++) {
				double angle = i * Math.PI / 180;
				for (int r = 0; r <= radius; r++) {
					int x = (int) Math.round(center.getX() + r * Math.cos(angle));
					int z = (int) Math.round(center.getZ() + r * Math.sin(angle));
					world.getBlockAt(x, y, z).setType(Material.AIR);
				}
			}
		}
	}
}
