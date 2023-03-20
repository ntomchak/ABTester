package manners.cowardly.abpromoter.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.bukkit.Bukkit;

import manners.cowardly.abpromoter.ABPromoter;
import manners.cowardly.abpromoter.database.connect.ConnectionPool;
import manners.cowardly.abpromoter.database.translator.StringIdTranslator;

public class MenuPageClick {
    private ConnectionPool pool;
    private StringIdTranslator buttonNameTranslator;
    private StringIdTranslator menuPageTranslator;

    public MenuPageClick(ConnectionPool pool, StringIdTranslator menuPageTranslator,
            StringIdTranslator buttonNameTranslator) {
        this.pool = pool;
        this.buttonNameTranslator = buttonNameTranslator;
        this.menuPageTranslator = menuPageTranslator;
    }

    // from sync
    public void recordPageClick(int openId, String buttonName, String fromPage, String toPage) {
        Bukkit.getScheduler().runTaskAsynchronously(ABPromoter.getInstance(),
                () -> recordPageClickAsync(openId, buttonName, fromPage, toPage));
    }

    // from async
    private void recordPageClickAsync(int openId, String buttonName, String fromPage, String toPage) {
        try (Connection c = pool.getConnection()) {
            int fromPageId = menuPageTranslator.idOfString(c, fromPage);
            int toPageId = menuPageTranslator.idOfString(c, toPage);
            int buttonId = buttonNameTranslator.idOfString(c, buttonName);
            PreparedStatement s = c.prepareStatement(
                    "INSERT INTO menu_page_click (open, menu_button, from_menu_page, to_menu_page) VALUES (?, ?, ?, ?)");
            s.setInt(1, openId);
            s.setInt(2, buttonId);
            s.setInt(3, fromPageId);
            s.setInt(4, toPageId);
            s.execute();
            s.close();
        } catch (SQLException e) {
            e.printStackTrace();
            ABPromoter.getInstance().getLogger().severe("Unable to record page click. Open id: " + openId
                    + ", buttonName: " + buttonName + ", fromPage: " + fromPage + ", toPage: " + toPage);
        }
    }
}
