package fr.ethilvan.diamondrushv2.command;

import fr.ethilvan.diamondrushv2.DiamondRush;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;


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


	protected void sendMessage(CommandSender commandSender, String messagePath) {
		String message = diamondRush.getMessagesConfig().getString(messagePath);
		if (message == null) {
			missingMessage(commandSender, messagePath);
			return;
		}
		commandSender.sendRichMessage(message);
	}


	protected void sendMessage(CommandSender commandSender, String messagePath, HashMap<String, String> placeholders) {
		String message = diamondRush.getMessagesConfig().getString(messagePath);
		if (message == null) {
			missingMessage(commandSender, messagePath);
			return;
		}

		for (HashMap.Entry<String, String> placeholder : placeholders.entrySet()) {
			message = message.replaceAll(placeholder.getKey(), placeholder.getValue());
		}
		commandSender.sendRichMessage(message);
	}


	private void missingMessage(CommandSender sender, String messagePath) {
		sender.sendRichMessage("<red>Message missing from configuration: '" + messagePath + "'.");
	}
}
