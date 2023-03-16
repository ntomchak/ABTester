package manners.cowardly.abpromoter.announcer.abgroup;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import manners.cowardly.abpromoter.ABPromoter;
import manners.cowardly.abpromoter.abgrouploading.ABGroup;
import manners.cowardly.abpromoter.announcer.abgroup.components.MessageGroup;
import manners.cowardly.abpromoter.announcer.abgroup.components.MessageLists;
import manners.cowardly.abpromoter.announcer.abgroup.components.Messages;

public class AnnouncerABGroup implements ABGroup {
    private List<MessageGroupEntry> messageGroups = new ArrayList<MessageGroupEntry>();
    private MessageGroup defaultMessageGroup;
    private String name;

    public AnnouncerABGroup(ConfigurationSection config, String name) {
        this.name = name;
        Messages messages = new Messages(config.getConfigurationSection("messages"));
        MessageLists msgLists = new MessageLists(config.getConfigurationSection("messageLists"), messages);
        new LoadConfiguration(config, msgLists);
        Bukkit.getScheduler().runTaskTimer(ABPromoter.getInstance(), this::sortMsgGroupsByFrequency, 12000, 24000);
    }

    public String getName() {
        return name;
    }

    public MessageGroup msgGroupOfPlayer(Player p) {
        for (MessageGroupEntry group : messageGroups) {
            if (group.group.belongsInGroup(p)) {
                group.frequency++;
                return group.group;
            }
        }
        ABPromoter.getInstance().getLogger().warning(p.getName()
                + " was placed in the default announcer message group due to being ineligible for any other group.");
        return defaultMessageGroup;
    }

    public void reload(ConfigurationSection config) {
        messageGroups = new ArrayList<MessageGroupEntry>();
        Messages messages = new Messages(config.getConfigurationSection("messages"));
        MessageLists msgLists = new MessageLists(config.getConfigurationSection("messageLists"), messages);
        new LoadConfiguration(config, msgLists);
    }

    private void sortMsgGroupsByFrequency() {
        Collections.sort(messageGroups);
    }

    private class MessageGroupEntry implements Comparable<MessageGroupEntry> {
        private MessageGroup group;
        private int frequency;

        public MessageGroupEntry(MessageGroup group, int frequency) {
            this.group = group;
            this.frequency = frequency;
        }

        @Override
        public int compareTo(MessageGroupEntry o) {
            return frequency - o.frequency;
        }

    }

    private class LoadConfiguration {
        public LoadConfiguration(ConfigurationSection config, MessageLists msgLists) {
            ConfigurationSection messageGroupsSection = config.getConfigurationSection("messageGroups");
            Set<String> keys = messageGroupsSection.getKeys(false);
            keys.forEach(key -> loadMsgGroup(key, messageGroupsSection.getConfigurationSection(key), msgLists));

            defaultMessageGroup = new MessageGroup(config.getConfigurationSection("defaultMessageGroup"), msgLists,
                    "DEFAULT MESSAGE GROUP");
        }

        private void loadMsgGroup(String name, ConfigurationSection msgGroupSection, MessageLists msgLists) {
            MessageGroup group = new MessageGroup(msgGroupSection, msgLists, name);
            messageGroups.add(new MessageGroupEntry(group, 0));
        }
    }
}
