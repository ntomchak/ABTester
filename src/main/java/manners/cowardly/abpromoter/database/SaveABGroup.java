package manners.cowardly.abpromoter.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import manners.cowardly.abpromoter.ABPromoter;
import manners.cowardly.abpromoter.database.connect.ConnectionPool;

public class SaveABGroup {
    private ConnectionPool pool;

    public SaveABGroup(ConnectionPool pool) {
        this.pool = pool;
    }
    
    public void saveAnnouncerGroup(String name) {
        saveGroup(name, "announcer_ab_groups");
    }
    
    public void saveMenuGroup(String name) {
        saveGroup(name, "menu_ab_groups");
    }

    private void saveGroup(String name, String tableName) {
        try (Connection c = pool.getConnection()) {
            PreparedStatement s = c.prepareStatement("SELECT * FROM " + tableName + " WHERE name=?");
            s.setString(1, name);
            ResultSet r = s.executeQuery();
            if (!r.next()) {
                r.close();
                s.close();
                insert(c, name, tableName);
            } else {
                r.close();
                s.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            ABPromoter.getInstance().getLogger()
                    .severe("Could not save ab group " + name + " in table " + tableName + " of database.");
        }
    }

    private void insert(Connection c, String name, String tableName) {
        try (PreparedStatement s = c.prepareStatement("INSERT INTO " + tableName + " (name) VALUES (?)")) {
            s.setString(1, name);
            s.execute();
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }
}
