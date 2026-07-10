package fr.ethilvan.diamondrushv2.command;

import fr.ethilvan.diamondrushv2.DiamondRush;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;


public abstract class Subcommand {

	protected DiamondRush diamondRush;


	public Subcommand(DiamondRush diamondRush) {
		this.diamondRush = diamondRush;
	}


	public abstract String getName();

	public abstract String getSyntax();

	public abstract String getDescription();

	public abstract String getPermission();

	public abstract void perform(CommandSender sender, @NotNull String[] args);


	public ArrayList<String> getAutoCompleteChoices(String[] args) {
		return new ArrayList<>();
	}
}
