package manners.cowardly.abpromoter.announcer.abgroup.components;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

import org.bukkit.configuration.ConfigurationSection;

import manners.cowardly.abpromoter.ABPromoter;
import manners.cowardly.abpromoter.announcer.abgroup.components.messages.MessageBuilder;

/**
 * 1 per announcer ab group
 *
 */
public class MessageLists {

    // list name to list
    private Map<String, MessageList> lists;

    public MessageLists(ConfigurationSection msgListSection) {
        lists = new HashMap<String, MessageList>();
        for (String key : msgListSection.getKeys(false)) {
            List<String> messages = msgListSection.getStringList(key);
            if (messages == null || messages.isEmpty())
                ABPromoter.getInstance().getLogger().warning("There is no message list at messageLists." + key
                        + ", ignoring this list as if it doesn't exist.");
            else {
                List<MessageBuilder> msgBuilders = messages.stream().map(raw -> new MessageBuilder(raw))
                        .collect(Collectors.toCollection(ArrayList::new));
                lists.put(key, new MessageList(msgBuilders));
            }
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
        private List<MessageBuilder> messages;

        public MessageList(Collection<MessageBuilder> messages) {
            this.messages = new ArrayList<MessageBuilder>(messages);
        }

        public MessageBuilder getRandomMessage() {
            return messages.get(ThreadLocalRandom.current().nextInt(messages.size()));
        }
    }
}
