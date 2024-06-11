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
		if (block.getY() < center.getY() - 1 || block.getY() > center.getY() + height) {
			return false;
		}
		if (block.getX() < center.getX() - radius || block.getX() > center.getX() + radius) {
			return false;
		}
		if (block.getZ() < center.getZ() - radius || block.getZ() > center.getZ() + radius) {
			return false;
		}
		double deltaX = Math.abs(block.getX() - center.getX());
		double deltaZ = Math.abs(block.getZ() - center.getZ());
		double distance = Math.sqrt(Math.pow(deltaX, 2) + Math.pow(deltaZ, 2));
		if (distance > (radius + Math.sqrt(2) - 1)) {
			return false;
		}
		return true;
	}

	@Override
	public void clear() {
		for (int y = center.getY() + height; y >= center.getY() - 1; y--) {
			for (int i = 0; i < 360; i++) {
				double angle = i * Math.PI / 180;
				for (int r = 0; r <= radius; r++) {
					int x = (int) Math.round(center.getX() + r * Math.cos(angle));
					int z = (int) Math.round(center.getZ() + r * Math.sin(angle));
					if (!world.getBlockAt(x, y, z).getType().equals(Material.AIR)) {
						world.getBlockAt(x, y, z).setType(Material.AIR);
					}
				}
			}
		}
	}
}
