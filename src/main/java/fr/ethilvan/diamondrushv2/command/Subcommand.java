package fr.ethilvan.diamondrushv2.command;

import fr.ethilvan.diamondrushv2.DiamondRush;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.List;

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


	public List<String> getAutoCompleteChoices(String[] args) {
		return List.of();
	}


	protected void sendMessage(CommandSender commandSender, String messagePath) {
		String message = diamondRush.getMessagesConfig().getString(messagePath);
		if (message == null) {
			commandSender.sendRichMessage("<red>Message missing from configuration: '" + messagePath + "'.");
			return;
		}
		commandSender.sendRichMessage(message);
	}
}
