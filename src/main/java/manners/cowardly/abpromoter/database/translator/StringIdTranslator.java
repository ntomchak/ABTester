package manners.cowardly.abpromoter.database.translator;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import manners.cowardly.abpromoter.ABPromoter;
import manners.cowardly.abpromoter.database.connect.ConnectionPool;

public class StringIdTranslator {
    private ConcurrentMap<String, Integer> menuPageNameToId = new ConcurrentHashMap<String, Integer>();
    private ConnectionPool pool;
    private String tableName;
    private String columnName;

    public StringIdTranslator(ConnectionPool pool, String tableName, String columnName) {
        this.pool = pool;
        this.tableName = tableName;
        this.columnName = columnName;
    }

    // from async only
    public int idOfString(String string) {
        Integer id = menuPageNameToId.get(string);
        if (id == null) {
            return fromDb(string);
        } else {
            int db = fromDb(string);
            menuPageNameToId.put(string, Integer.valueOf(db));
            return db;
        }
    }

    private int fromDb(String pageName) {
        try (Connection c = pool.getConnection()) {
            PreparedStatement s = c.prepareStatement("SELECT id FROM " + tableName + " WHERE " + columnName + "=?");
            s.setString(1, pageName);
            ResultSet r = s.executeQuery();
            if (r.next()) {
                int id = r.getInt("id");
                r.close();
                s.close();
                return id;
            } else {
                r.close();
                s.close();
                int id = insert(pageName, c);
                return id;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            ABPromoter.getInstance().getLogger().severe("Was unable to get id of \"" + pageName + "\" in column \""
                    + columnName + "\" of table \"" + tableName + "\" in database.");
            return -1;
        }
    }

    private int insert(String pageName, Connection c) {
        try {
            PreparedStatement s = c.prepareStatement("INSERT INTO " + tableName + " (" + columnName + ") VALUES (?)",
                    Statement.RETURN_GENERATED_KEYS);
            s.setString(1, pageName);
            s.executeUpdate();
            ResultSet r = s.getGeneratedKeys();
            r.next();
            int id = r.getInt(1);
            s.close();
            r.close();
            return id;
        } catch (SQLException e) {
            e.printStackTrace();
            ABPromoter.getInstance().getLogger().severe("Was unable to insert and get id of \"" + pageName
                    + "\" in column \"" + columnName + "\" of table \"" + tableName + "\" in database.");
            return -1;
        }
    }
}