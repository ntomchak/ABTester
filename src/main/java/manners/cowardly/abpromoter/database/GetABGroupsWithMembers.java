package manners.cowardly.abpromoter.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

import manners.cowardly.abpromoter.ABPromoter;
import manners.cowardly.abpromoter.database.connect.ConnectionPool;

public class GetABGroupsWithMembers {
    private ConnectionPool pool;

    public GetABGroupsWithMembers(ConnectionPool pool) {
        this.pool = pool;
    }

    // from async or sync during startup/reload
    public Set<String> abGroupsWithMembers(String tableName, String usersTableColumnName) {
        try (Connection c = pool.getConnection()) {
            Set<String> groupNames = new HashSet<String>();
            PreparedStatement s = c.prepareStatement(
                    "SELECT name FROM " + tableName + " WHERE EXISTS (SELECT 1 FROM users WHERE users."
                            + usersTableColumnName + " = " + tableName + ".id)");
            ResultSet r = s.executeQuery();
            while (r.next())
                groupNames.add(r.getString("name"));
            r.close();
            s.close();
            return groupNames;
        } catch (SQLException e) {
            e.printStackTrace();
            ABPromoter.getInstance().getLogger().severe("Unable to get the list of A/B groups in " + tableName);
            return null;
        }
    }
}
