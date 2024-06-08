package fr.ethilvan.diamondrushv2.command;

import fr.ethilvan.diamondrushv2.DiamondRush;
import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public abstract class DiamondRushCommand implements BasicCommand {

	protected DiamondRush diamondRush;
	protected String commandName;


	public DiamondRushCommand(DiamondRush diamondRush, String commandName) {
		this.diamondRush = diamondRush;
		this.commandName = commandName;
	}


	@Override
	public void execute(@NotNull CommandSourceStack commandSourceStack, @NotNull String[] args) {
		if (args.length >= 1 && args[0].equalsIgnoreCase(commandName)) {
			perform(commandSourceStack.getSender(), args);
		}
	}

	@Override
	public @NotNull Collection<String> suggest(@NotNull CommandSourceStack commandSourceStack, @NotNull String[] args) {
		return BasicCommand.super.suggest(commandSourceStack, args);
	}


	protected abstract void perform(CommandSender sender, @NotNull String[] args);


	protected void sendMessage(CommandSender commandSender, String messagePath) {
		String message = diamondRush.getMessagesConfig().getString(messagePath);
		if (message == null) {
			commandSender.sendRichMessage("<red>Message missing from configuration: '" + messagePath + "'.");
			return;
		}
		commandSender.sendRichMessage(message);
	}
}
