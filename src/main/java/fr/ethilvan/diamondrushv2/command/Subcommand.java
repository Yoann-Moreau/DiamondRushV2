package fr.ethilvan.diamondrushv2.command;

import fr.ethilvan.diamondrushv2.DiamondRush;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

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


	protected void sendMessage(CommandSender commandSender, String messagePath) {
		String message = diamondRush.getMessagesConfig().getString(messagePath);
		if (message == null) {
			commandSender.sendRichMessage("<red>Message missing from configuration: '" + messagePath + "'.");
			return;
		}
		commandSender.sendRichMessage(message);
	}
}
