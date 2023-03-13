package manners.cowardly.abpromoter.announcer.abgroup.components;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.bukkit.Statistic;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import manners.cowardly.abpromoter.ABPromoter;
import manners.cowardly.abpromoter.announcer.abgroup.components.MessageLists.MessageList;
import manners.cowardly.abpromoter.announcer.abgroup.components.messages.MessageBuilder;
import manners.cowardly.abpromoter.utilities.WeightedProbabilities;
import manners.cowardly.abpromoter.utilities.WeightedProbabilities.Probability;

/**
 * New players, vip players, etc. multiple per announcer ab group
 *
 */
public class MessageGroup {
    private WeightedProbabilities<MessageList> messageLists;
    private CheckPlayerEligibility playerCheck;
    private String messageGroupName;
    private int secondsAfterLogin;
    private int every;

    public MessageGroup(ConfigurationSection section, MessageLists msgLists, String messageGroupName) {
        new LoadConfiguration(section, msgLists, messageGroupName);
        this.messageGroupName = messageGroupName;
    }

    public int secondsAfterLogin() {
        return secondsAfterLogin;
    }

    public int deliverEvery() {
        return every;
    }

    public String getName() {
        return messageGroupName;
    }

    public boolean belongsInGroup(Player p) {
        return playerCheck.belongsInGroup(p);
    }

    public MessageBuilder sampleMessage() {
        return messageLists.sample().getRandomMessage();
    }

    private class LoadConfiguration {
        public LoadConfiguration(ConfigurationSection section, MessageLists lists, String messageGroupName) {
            loadMessageLists(section.getConfigurationSection("messageLists"), lists, messageGroupName);
            loadPlayerEligibilityCheck(section);
            loadTimer(section.getConfigurationSection("timer"));
        }

        private void loadTimer(ConfigurationSection timerSection) {
            secondsAfterLogin = timerSection.getInt("afterLogin");
            every = timerSection.getInt("every");
        }

        private void loadPlayerEligibilityCheck(ConfigurationSection groupSection) {
            ConfigurationSection section = groupSection.getConfigurationSection("eligibility");
            List<String> orGroups = section.getStringList("orGroups");
            List<String> andGroups = section.getStringList("andGroups");
            List<String> notGroups = section.getStringList("notGroups");
            double playTimeMinimum = section.getDouble("playTimeMinimum", -1);
            double playTimeMaximum = section.getDouble("playTimeMinimum", Integer.MAX_VALUE);
            playerCheck = new CheckPlayerEligibility(orGroups, andGroups, notGroups, playTimeMinimum, playTimeMaximum);
        }

        private void loadMessageLists(ConfigurationSection messageListsSection, MessageLists lists,
                String messageGroupName) {
            List<Probability<MessageList>> probabilities = new ArrayList<Probability<MessageList>>();
            messageListsSection.getKeys(false).forEach(
                    key -> loadMessageListWeight(messageListsSection, key, lists, messageGroupName, probabilities));
            messageLists = new WeightedProbabilities<MessageList>(probabilities);
        }

        private void loadMessageListWeight(ConfigurationSection messageListsSection, String msgListName,
                MessageLists lists, String messageGroupName, List<Probability<MessageList>> probabilities) {
            int weight = messageListsSection.getInt(msgListName);
            Optional<MessageList> list = lists.getMessageList(msgListName);
            if (list.isEmpty()) {
                ABPromoter.getInstance().getLogger().warning("Invalid messageList name '" + msgListName
                        + "' in messageGroup '" + messageGroupName + "'. Ignoring as if it doesn't exist.");
            } else if (weight <= 0) {
                ABPromoter.getInstance().getLogger().warning("Invalid messageList weight for '" + msgListName
                        + "' in messageGroup '" + messageGroupName + "'. Ignoring as if it doesn't exist.");
            } else {
                Probability<MessageList> probability = new Probability<MessageList>(weight, list.get());
                probabilities.add(probability);
            }
        }
    }

    private class CheckPlayerEligibility {
        private List<String> orGroups;
        private List<String> andGroups;
        private List<String> notGroups;
        private double playTimeMinimum;
        private double playTimeMaximum;

        public CheckPlayerEligibility(List<String> orGroups, List<String> andGroups, List<String> notGroups,
                double playTimeMinimum, double playTimeMaximum) {
            this.orGroups = orGroups;
            this.andGroups = andGroups;
            this.notGroups = notGroups;
            this.playTimeMinimum = playTimeMinimum;
            this.playTimeMaximum = playTimeMaximum;
        }

        public boolean belongsInGroup(Player p) {
            return playtimeEligible(p) && andGroups(p) && orGroups(p) && notGroups(p);
        }

        private boolean playtimeEligible(Player p) {
            int ticksPlayed = p.getStatistic(Statistic.PLAY_ONE_MINUTE);
            double hoursPlayed = ticksPlayed / 72000.0;
            return hoursPlayed >= playTimeMinimum && hoursPlayed <= playTimeMaximum;
        }

        /**
         * Returns true if the player is in at least one of the orGroups
         * 
         * @param p
         * @return
         */
        private boolean orGroups(Player p) {
            for (String group : orGroups)
                if (ABPromoter.getPerms().playerInGroup(p, group))
                    return true;
            return false;
        }

        /**
         * Returns true if the player is in all of the andGroups
         * 
         * @param p
         * @return
         */
        private boolean andGroups(Player p) {
            for (String group : andGroups)
                if (!ABPromoter.getPerms().playerInGroup(p, group))
                    return false;
            return true;
        }

        /**
         * Returns true if the player is in none of the permissions groups in the
         * notGroups list
         * 
         * @param p
         * @return
         */
        private boolean notGroups(Player p) {
            for (String group : notGroups)
                if (ABPromoter.getPerms().playerInGroup(p, group))
                    return false;
            return true;
        }
    }
}
