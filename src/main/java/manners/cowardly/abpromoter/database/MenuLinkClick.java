package manners.cowardly.abpromoter.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Collection;

import org.bukkit.Bukkit;

import manners.cowardly.abpromoter.ABPromoter;
import manners.cowardly.abpromoter.announcer.abgroup.components.messages.MessageTemplate.DeliverableMessage.MessageLinkTokenInfo;
import manners.cowardly.abpromoter.database.connect.ConnectionPool;
import manners.cowardly.abpromoter.database.redis.Redis;
import manners.cowardly.abpromoter.database.translator.StringIdTranslator;

public class MenuLinkClick {
    private ConnectionPool pool;
    private StringIdTranslator buttonNameTranslator;
    private Redis redis;

    public MenuLinkClick(ConnectionPool pool, StringIdTranslator buttonNameTranslator, Redis redis) {
        this.pool = pool;
        this.buttonNameTranslator = buttonNameTranslator;
        this.redis = redis;
    }

    // from sync
    public void recordMenuLinkClick(int openId, String buttonName, Collection<MessageLinkTokenInfo> tokens) {
        Bukkit.getScheduler().runTaskAsynchronously(ABPromoter.getInstance(),
                () -> recordMenuLinkClickAsync(openId, buttonName, tokens));
    }

    // from async
    private void recordMenuLinkClickAsync(int openId, String buttonName, Collection<MessageLinkTokenInfo> tokens) {
        redis.recordMenuLinks(openId, tokens);
        try (Connection c = pool.getConnection()) {
            int buttonId = buttonNameTranslator.idOfString(c, buttonName);
            PreparedStatement s = c.prepareStatement("INSERT INTO menu_link_click (open, menu_button) VALUES (?, ?)");
            s.setInt(1, openId);
            s.setInt(2, buttonId);
            s.execute();
            s.close();
        } catch (SQLException e) {
            e.printStackTrace();
            ABPromoter.getInstance().getLogger()
                    .severe("Unable to record menu link click. Open id: " + openId + ", buttonName: " + buttonName);
        }
    }
}
