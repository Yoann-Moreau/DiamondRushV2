package fr.ethilvan.diamondrushv2.region;

import org.bukkit.Material;
import org.bukkit.block.Block;

public class CuboidRegion extends Region {

	private final int xDistanceFromCenter;
	private final int zDistanceFromCenter;
	private final int height;
	private final Block min;
	private final Block max;


	public CuboidRegion(Block center, int xDistanceFromCenter, int zDistanceFromCenter, int height) {
		super(center);
		this.xDistanceFromCenter = xDistanceFromCenter;
		this.zDistanceFromCenter = zDistanceFromCenter;
		this.height = height;

		int minX = center.getX() - xDistanceFromCenter;
		int maxX = center.getX() + xDistanceFromCenter;
		int minY = center.getY() - 1;
		int maxY = center.getY() + height;
		int minZ = center.getZ() - zDistanceFromCenter;
		int maxZ = center.getZ() + zDistanceFromCenter;

		this.min = world.getBlockAt(minX, minY, minZ);
		this.max = world.getBlockAt(maxX, maxY, maxZ);
	}


	public int getXDistanceFromCenter() {
		return xDistanceFromCenter;
	}

	public int getZDistanceFromCenter() {
		return zDistanceFromCenter;
	}

	public int getHeight() {
		return height;
	}

	public Block getMin() {
		return min;
	}

	public Block getMax() {
		return max;
	}


	@Override
	public boolean contains(Block block) {
		if (!block.getWorld().equals(world)) {
			return false;
		}
		if (block.getX() < min.getX() || block.getX() > max.getX()) {
			return false;
		}
		if (block.getZ() < min.getZ() || block.getZ() > max.getZ()) {
			return false;
		}
		if (block.getY() < min.getY() || block.getY() > max.getY()) {
			return false;
		}
		return true;
	}


	@Override
	public void clear() {
		for (int x = min.getX(); x <= max.getX(); x++) {
			for (int y = max.getY(); y >= min.getY(); y--) {
				for (int z = min.getZ(); z <= max.getZ(); z++) {
					if (!world.getBlockAt(x, y, z).getType().equals(Material.AIR)) {
						world.getBlockAt(x, y, z).setType(Material.AIR);
					}
				}
			}
		}
	}
}
