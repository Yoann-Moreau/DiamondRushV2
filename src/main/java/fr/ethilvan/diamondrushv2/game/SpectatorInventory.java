package fr.ethilvan.diamondrushv2.game;

import fr.ethilvan.diamondrushv2.DiamondRush;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;


public class SpectatorInventory implements InventoryHolder {

	private final DiamondRush diamondRush;

	private final Inventory inventory;


	public SpectatorInventory(DiamondRush diamondRush, int numberOfTeams) {
		this.diamondRush = diamondRush;
		this.inventory = diamondRush.getPlugin().getServer().createInventory(
				this,
				numberOfTeams * 9
		);
	}


	@Override
	public @NotNull Inventory getInventory() {
		return inventory;
	}


	public void addItem(ItemStack item, int slot) {
		inventory.setItem(slot, item);
	}
}
