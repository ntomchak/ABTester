package manners.cowardly.abpromoter.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.bukkit.Bukkit;

import manners.cowardly.abpromoter.ABPromoter;
import manners.cowardly.abpromoter.database.connect.ConnectionPool;
import manners.cowardly.abpromoter.database.translator.StringIdTranslator;

public class MenuLinkClick {
    private ConnectionPool pool;
    private StringIdTranslator buttonNameTranslator;

    public MenuLinkClick(ConnectionPool pool, StringIdTranslator buttonNameTranslator) {
        this.pool = pool;
        this.buttonNameTranslator = buttonNameTranslator;
    }

    // from sync
    public void recordMenuLinkClick(int openId, String buttonName) {
        Bukkit.getScheduler().runTaskAsynchronously(ABPromoter.getInstance(),
                () -> recordMenuLinkClickAsync(openId, buttonName));
    }

    // from sync
    private void recordMenuLinkClickAsync(int openId, String buttonName) {
        int buttonId = buttonNameTranslator.idOfString(buttonName);
        try (Connection c = pool.getConnection()) {
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
