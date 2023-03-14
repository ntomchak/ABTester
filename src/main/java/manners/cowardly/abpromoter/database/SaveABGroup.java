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

    public void saveGroup(String groupName, String tableName) {
        try (Connection c = pool.getConnection()) {
            PreparedStatement s = c.prepareStatement("SELECT * FROM " + tableName + " WHERE name=?");
            s.setString(1, groupName);
            ResultSet r = s.executeQuery();
            if (!r.next()) {
                r.close();
                s.close();
                insert(c, groupName, tableName);
            } else {
                r.close();
                s.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            ABPromoter.getInstance().getLogger()
                    .severe("Could not save ab group " + groupName + " in table " + tableName + " of database.");
        }
    }

    public void insert(String groupName, String tableName) {
        try (Connection c = pool.getConnection()) {
            insert(c, groupName, tableName);
        } catch (SQLException e) {
            e.printStackTrace();
            ABPromoter.getInstance().getLogger()
                    .severe("Could not get connection to insert ab group " + groupName + " into table " + tableName);
        }
    }

    private void insert(Connection c, String name, String tableName) {
        try (PreparedStatement s = c.prepareStatement("INSERT INTO " + tableName + " (name) VALUES (?)")) {
            s.setString(1, name);
            s.execute();
        } catch (SQLException e) {
            e.printStackTrace();
            ABPromoter.getInstance().getLogger()
                    .severe("Could not insert ab group " + name + " into table " + tableName);
        }

    }
}
