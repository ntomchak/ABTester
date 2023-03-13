package manners.cowardly.abpromoter.announcer.abgroup.components;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.bukkit.configuration.ConfigurationSection;

import manners.cowardly.abpromoter.announcer.abgroup.components.messages.MessageBuilder;

public class Messages {
    private Map<String, MessageBuilder> messages = new HashMap<String, MessageBuilder>();

    public Messages(ConfigurationSection messagesSection) {
        Set<String> keys = messagesSection.getKeys(false);
        keys.forEach(key -> loadMessage(key, messagesSection.getStringList(key)));
    }

    public Optional<MessageBuilder> getMessage(String name) {
        return Optional.ofNullable(messages.get(name));

    }

    private void loadMessage(String name, List<String> pieces) {
        if (pieces.isEmpty())
            pieces.add("");
        messages.put(name, new MessageBuilder(pieces));
    }
}
