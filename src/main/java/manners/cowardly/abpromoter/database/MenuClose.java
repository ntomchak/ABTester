package manners.cowardly.abpromoter.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.bukkit.Bukkit;

import manners.cowardly.abpromoter.ABPromoter;
import manners.cowardly.abpromoter.database.connect.ConnectionPool;

public class MenuClose {
    private ConnectionPool pool;
    
    public MenuClose(ConnectionPool pool) {
        this.pool = pool;
    }

    public void recordMenuClose(int openId) {
        Bukkit.getScheduler().runTaskAsynchronously(ABPromoter.getInstance(), () -> recordMenuCloseAsync(openId));
    }

    // from async
    private void recordMenuCloseAsync(int openId) {
        try (Connection c = pool.getConnection()) {
            PreparedStatement s = c.prepareStatement("INSERT INTO menu_closes (open) VALUES (?)");
            s.setInt(1, openId);
            s.execute();
            s.close();
        } catch (SQLException e) {
            e.printStackTrace();
            ABPromoter.getInstance().getLogger().severe("Could not record closing of menu for openId " + openId);
        }
    }
}
