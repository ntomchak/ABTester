package manners.cowardly.abpromoter.announcer.abgroup.components;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.bukkit.configuration.ConfigurationSection;

import manners.cowardly.abpromoter.ABPromoter;
import manners.cowardly.abpromoter.announcer.abgroup.components.messages.MessageBuilder;
import manners.cowardly.abpromoter.utilities.WeightedProbabilities;

/**
 * 1 per announcer ab group
 *
 */
public class MessageLists {

    // list name to list
    private Map<String, MessageList> lists;

    public MessageLists(ConfigurationSection msgListSection, Messages messages) {
        lists = new HashMap<String, MessageList>();
        for (String key : msgListSection.getKeys(false)) {
            ConfigurationSection listSection = msgListSection.getConfigurationSection(key);
            lists.put(key, new MessageList(listSection, messages, key));
        }
    }

    /**
     * Returns the message list of the specified name if it exists. If not, return
     * empty optional
     * 
     * @param name
     * @return
     */
    public Optional<MessageList> getMessageList(String name) {
        return Optional.ofNullable(lists.get(name));
    }

    /**
     * multiple per announcer ab group
     * 
     * @author nstom
     *
     */
    public class MessageList {
        private WeightedProbabilities<MessageBuilder> messages = new WeightedProbabilities<MessageBuilder>();
        private String name;

        public MessageList(ConfigurationSection listSection, Messages messages, String name) {
            Set<String> keys = listSection.getKeys(false);
            keys.forEach(key -> loadMessage(messages, key, listSection.getInt(key)));
            this.name = name;
        }

        private void loadMessage(Messages messages, String name, int weight) {
            if (weight > 0) {
                Optional<MessageBuilder> msg = messages.getMessage(name);
                if (msg.isPresent()) {
                    this.messages.add(msg.get(), weight);
                } else {
                    ABPromoter.getInstance().getLogger().warning("There is no message named '" + name + "', ignoring.");
                }
            }
        }

        public MessageBuilder getRandomMessage() {
            if (messages.isEmpty()) {
                ABPromoter.getInstance().getLogger().severe(name + " message list is empty.");
                return null;
            }
            return messages.sample();
        }
    }
}
