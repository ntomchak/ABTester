package manners.cowardly.abpromoter.announcer;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import manners.cowardly.abpromoter.ABPromoter;
import manners.cowardly.abpromoter.announcer.abgroup.components.MessageGroup;
import manners.cowardly.abpromoter.announcer.abgroup.components.messages.MessageBuilder;
import manners.cowardly.abpromoter.announcer.abgroup.components.messages.MessageBuilder.DeliverableMessage;
import manners.cowardly.abpromoter.database.AnnouncerDeliveries;

/**
 * 1 per announcer ab group
 * 
 * @author nstom
 *
 */
public class Deliverer {
    private Map<UUID, MessageGroup> playerMessageGroups = new HashMap<UUID, MessageGroup>();
    private Scheduler scheduler = new Scheduler();
    private AnnouncerDeliveries deliveriesDb;
    private AnnouncerTokenRecords tokenRecords;

    public Deliverer(AnnouncerDeliveries deliveriesDb, AnnouncerTokenRecords tokenRecords) {
        this.deliveriesDb = deliveriesDb;
        this.tokenRecords = tokenRecords;
    }

    public void addPlayer(Player p, MessageGroup group) {
        playerMessageGroups.put(p.getUniqueId(), group);
        scheduler.scheduleNextDelivery(p, group.secondsAfterLogin());
    }
    
    
    public void updateMessageGroup(UUID player, MessageGroup group) {
        playerMessageGroups.put(player, group);   
    }

    /**
     * only for logging out
     * 
     * @param p
     */
    public void cancelDeliveries(Player p) {
        scheduler.cancelDeliveries(p);
        playerMessageGroups.remove(p.getUniqueId());
    }

    private void deliver(Player p) {
        if (p.isOnline()) {
            MessageGroup group = playerMessageGroups.get(p.getUniqueId());
            if (group == null) {
                ABPromoter.getInstance().getLogger().severe("Player \"" + p.getName()
                        + "\" does not have a message group in Deliverer! Skipping delivery this time. Will check again in 20 minutes.");
                scheduler.scheduleNextDelivery(p, 1200);
            } else {
                MessageBuilder msgBuilder = group.sampleMessage();
                if (msgBuilder != null) {
                    DeliverableMessage msg = msgBuilder.getMessage();
                    msg.deliver(p);

                    // store tokens from message
                    tokenRecords.storeTokens(p.getUniqueId(), msg.getTokens());

                    // record delivery in database
                    Collection<String> tokens = msg.getTokens().stream().map(tokenInfo -> tokenInfo.getToken())
                            .collect(Collectors.toList());
                    deliveriesDb.recordDelivery(msg.getRawText(), group.getName(), p.getUniqueId(), tokens);

                    // schedule next delivery
                } else {
                    ABPromoter.getInstance().getLogger()
                            .warning("Message List returned no message, therefore a message delivery to " + p.getName()
                                    + " was skipped.");
                }
                scheduler.scheduleNextDelivery(p, group.deliverEvery());
            }
        }
    }

    private class Scheduler {
        private Map<UUID, BukkitTask> scheduledDeliveries = new HashMap<UUID, BukkitTask>();

        public void scheduleNextDelivery(Player p, int seconds) {
            scheduledDeliveries.put(p.getUniqueId(), Bukkit.getScheduler()
                    .runTaskLaterAsynchronously(ABPromoter.getInstance(), () -> syncDeliver(p), seconds * 20));
        }

        public void cancelDeliveries(Player p) {
            BukkitTask task = scheduledDeliveries.remove(p.getUniqueId());
            if (task != null)
                task.cancel();
        }

        private void syncDeliver(Player p) {
            Bukkit.getScheduler().runTask(ABPromoter.getInstance(), () -> deliver(p));
        }
    }
}
