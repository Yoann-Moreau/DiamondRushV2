package fr.ethilvan.diamondrushv2.command;

import fr.ethilvan.diamondrushv2.DiamondRush;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;
import java.util.Optional;

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
			missingMessage(commandSender, messagePath);
			return;
		}
		commandSender.sendRichMessage(message);
	}


	protected void sendMessage(CommandSender commandSender, String messagePath, Map<String, String> placeholders) {
		String message = diamondRush.getMessagesConfig().getString(messagePath);
		if (message == null) {
			missingMessage(commandSender, messagePath);
			return;
		}

		TextComponent textComponent = (TextComponent) replacePlaceholders(
				PlainTextComponentSerializer.plainText().deserialize(message),
				placeholders
		);
		commandSender.sendRichMessage(textComponent.content());
	}


	private void missingMessage(CommandSender sender, String messagePath) {
		sender.sendRichMessage("<red>Message missing from configuration: '" + messagePath + "'.");
	}


	private Component replacePlaceholders(Component component, Map<String, String> placeholders) {
		TextComponent textComponent = (TextComponent) component;
		Optional<String> nextKey = placeholders.keySet().stream().findFirst();
		if (nextKey.isPresent()) {
			String key = nextKey.get();
			String value = placeholders.get(key);
			if (placeholders.size() > 1) {
				placeholders.remove(key);
				return replacePlaceholders(
						MiniMessage.miniMessage().deserialize(textComponent.content(), Placeholder.unparsed(key, value)),
						placeholders
				);
			}

			return MiniMessage.miniMessage().deserialize(textComponent.content(), Placeholder.unparsed(key, value));
		}

		return component;
	}
}
