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
import manners.cowardly.abpromoter.database.translator.MessageIdTranslator;
import manners.cowardly.abpromoter.database.translator.StringIdTranslator;
import manners.cowardly.abpromoter.database.translator.TokenIdTranslator;

public class AnnouncerDeliveries {

    private TokenIdTranslator tokenTranslator;
    private ConnectionPool pool;
    private StringIdTranslator messageGroupTranslator;
    private MessageIdTranslator messagesIdTranslator;
    private StringIdTranslator ipTranslator;
    private Redis redis;

    public AnnouncerDeliveries(TokenIdTranslator tokenTranslator, StringIdTranslator messageGroupTranslator,
            MessageIdTranslator messagesIdTranslator, ConnectionPool pool, StringIdTranslator ipTranslator,
            Redis redis) {
        this.tokenTranslator = tokenTranslator;
        this.messagesIdTranslator = messagesIdTranslator;
        this.messageGroupTranslator = messageGroupTranslator;
        this.pool = pool;
        this.ipTranslator = ipTranslator;
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
    public void recordDelivery(String rawMessage, String messageGroup, UUID user, Collection<String> tokens,
            String ip, Collection<MessageLinkTokenInfo> linkTokens) {
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
            int msgGroupId = messageGroupTranslator.idOfString(c, messageGroup);
            int msgId = messagesIdTranslator.idOfString(c, rawMessage);
            int ipId = ipTranslator.idOfString(c, ip);
            PreparedStatement s = c.prepareStatement(
                    "INSERT INTO announcer_deliveries SET message=?, user=(SELECT id FROM users WHERE mc_uuid=?), message_group=?, ip_address=?",
                    Statement.RETURN_GENERATED_KEYS);
            s.setInt(1, msgId);
            s.setString(2, user.toString());
            s.setInt(3, msgGroupId);
            s.setInt(4, ipId);
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