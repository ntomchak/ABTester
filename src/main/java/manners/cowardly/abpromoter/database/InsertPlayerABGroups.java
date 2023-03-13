package manners.cowardly.abpromoter.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.UUID;

import org.bukkit.Bukkit;

import manners.cowardly.abpromoter.ABPromoter;
import manners.cowardly.abpromoter.database.connect.ConnectionPool;

public class InsertPlayerABGroups {

    private ConnectionPool pool;

    public InsertPlayerABGroups(ConnectionPool pool) {
        this.pool = pool;
    }

    // from sync
    public void insert(UUID uuid, String announcerGroup, String menuGroup) {
        Bukkit.getScheduler().runTaskAsynchronously(ABPromoter.getInstance(),
                () -> insertAsync(uuid, announcerGroup, menuGroup));
    }

    // from async only
    private void insertAsync(UUID uuid, String announcerGroup, String menuGroup) {
        try (Connection c = pool.getConnection()) {
            PreparedStatement s = c.prepareStatement("INSERT INTO users SET "
                    + "mc_uuid=?, announcer_ab_group=(SELECT id FROM announcer_ab_groups WHERE name=?)"
                    + ", menu_ab_group=(SELECT id FROM menu_ab_groups WHERE name=?)");
            s.setString(1, uuid.toString());
            s.setString(2, announcerGroup);
            s.setString(3, menuGroup);
            s.execute();
            s.close();
        } catch (SQLException e) {
            e.printStackTrace();
            ABPromoter.getInstance().getLogger().severe("Could not insert announcer (" + announcerGroup + ") and menu ("
                    + menuGroup + ") ab groups for " + uuid);
        }
    }
}
