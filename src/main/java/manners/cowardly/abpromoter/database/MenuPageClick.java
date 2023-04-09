package manners.cowardly.abpromoter.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.bukkit.Bukkit;

import manners.cowardly.abpromoter.ABPromoter;
import manners.cowardly.abpromoter.database.connect.ConnectionPool;

public class MenuPageClick {
    private ConnectionPool pool;

    public MenuPageClick(ConnectionPool pool) {
        this.pool = pool;
    }

    // from sync
    public void recordPageClick(int openId, String buttonName, String fromPage, String toPage) {
        Bukkit.getScheduler().runTaskAsynchronously(ABPromoter.getInstance(),
                () -> recordPageClickAsync(openId, buttonName, fromPage, toPage));
    }

    // from async
    private void recordPageClickAsync(int openId, String buttonName, String fromPage, String toPage) {
        try (Connection c = pool.getConnection()) {
            PreparedStatement s = c.prepareStatement(
                    "INSERT INTO menu_page_click SET open=?, menu_button=" + Constants.SELECT_MENU_BUTTON
                            + ", from_menu_page=" + Constants.SELECT_PAGE + ", to_menu_page=" + Constants.SELECT_PAGE);
            s.setInt(1, openId);
            s.setString(2, buttonName);
            s.setString(3, fromPage);
            s.setString(4, toPage);
            s.execute();
            s.close();
        } catch (SQLException e) {
            e.printStackTrace();
            ABPromoter.getInstance().getLogger().severe("Unable to record page click. Open id: " + openId
                    + ", buttonName: " + buttonName + ", fromPage: " + fromPage + ", toPage: " + toPage);
        }
    }
}
