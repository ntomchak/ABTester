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

public class MenuLinkClick {
    private ConnectionPool pool;
    private Redis redis;

    public MenuLinkClick(ConnectionPool pool, Redis redis) {
        this.pool = pool;
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
            //PreparedStatement s = c.prepareStatement("INSERT INTO menu_link_click (open, menu_button) VALUES (?, ?)");
            PreparedStatement s = c.prepareStatement("INSERT INTO menu_link SET open=?, menu_button=" + Constants.SELECT_MENU_BUTTON);
            s.setInt(1, openId);
            s.setString(2, buttonName);
            s.execute();
            s.close();
        } catch (SQLException e) {
            e.printStackTrace();
            ABPromoter.getInstance().getLogger()
                    .severe("Unable to record menu link click. Open id: " + openId + ", buttonName: " + buttonName);
        }
    }
}
