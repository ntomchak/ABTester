package manners.cowardly.abpromoter.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.UUID;

import org.bukkit.Bukkit;

import manners.cowardly.abpromoter.ABPromoter;
import manners.cowardly.abpromoter.announcer.abgroup.components.messages.MessageTemplate.DeliverableMessage.MessageLinkTokenInfo;
import manners.cowardly.abpromoter.database.connect.ConnectionPool;
import manners.cowardly.abpromoter.database.redis.Redis;
import manners.cowardly.abpromoter.database.translator.TokenIdTranslator;

public class AnnouncerDeliveries {

    private TokenIdTranslator tokenTranslator;
    private ConnectionPool pool;
    private Redis redis;

    public AnnouncerDeliveries(TokenIdTranslator tokenTranslator, ConnectionPool pool, Redis redis) {
        this.tokenTranslator = tokenTranslator;
        this.pool = pool;
        this.redis = redis;
    }

    /**
     * Records delivery of a message to a user and caches the id of the delivery to
     * which the relevant announcer message tokens correspond. from sync only
     * 
     * @param rawMessage
     * @param user
     * @param tokens
     */
    public void recordDelivery(String rawMessage, String messageGroup, UUID user, Collection<String> tokens, String ip,
            Collection<MessageLinkTokenInfo> linkTokens) {
        Runnable insertDeliveryAndRecordTokens = () -> {
            int deliveryId = insertDelivery(rawMessage, messageGroup, user, ip);
            recordTokens(user, tokens, deliveryId);
            redis.recordDeliveryLinks(deliveryId, linkTokens);
        };
        Bukkit.getScheduler().runTaskAsynchronously(ABPromoter.getInstance(), insertDeliveryAndRecordTokens);
    }

    // from async
    private void recordTokens(UUID user, Collection<String> tokens, int deliveryId) {
        Runnable recordTokens = () -> tokens.forEach(token -> tokenTranslator.addToken(token, deliveryId, user));
        Bukkit.getScheduler().runTask(ABPromoter.getInstance(), recordTokens);
    }

    // call from async only, returns id of delivery
    private int insertDelivery(String rawMessage, String messageGroup, UUID user, String ip) {
        try (Connection c = pool.getConnection()) {
            PreparedStatement s = c.prepareStatement(
                    "INSERT INTO announcer_deliveries SET message=(SELECT id FROM announcer_messages WHERE text_hash_code=?), user="
                            + QueryConstants.SELECT_USER + ", "
                            + "message_group=(SELECT id FROM announcer_message_groups WHERE name=?), ip_address="
                            + QueryConstants.SELECT_IP,
                    Statement.RETURN_GENERATED_KEYS);
            s.setInt(1, rawMessage.hashCode());
            s.setString(2, user.toString());
            s.setString(3, messageGroup);
            s.setString(4, ip);
            s.executeUpdate();
            ResultSet r = s.getGeneratedKeys();
            r.next();
            int deliveryId = r.getInt(1);
            r.close();
            s.close();
            return deliveryId;
        } catch (SQLException e) {
            e.printStackTrace();
            ABPromoter.getInstance().getLogger().severe("Could not insert delivery of message \"" + rawMessage
                    + "\" to user " + user.toString() + " into the database.");
            return -1;
        }
    }
}