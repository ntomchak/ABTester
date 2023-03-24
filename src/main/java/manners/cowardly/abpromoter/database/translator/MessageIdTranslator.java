package manners.cowardly.abpromoter.database.translator;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import manners.cowardly.abpromoter.ABPromoter;

public class MessageIdTranslator {
    private ConcurrentMap<String, Integer> rawMessageToId = new ConcurrentHashMap<String, Integer>();

    // from async only
    public int idOfString(Connection c, String rawMessage) {
        Integer id = rawMessageToId.get(rawMessage);
        if (id == null) {
            int hash = rawMessage.hashCode();
            int db = fromDb(c, hash, rawMessage);
            rawMessageToId.put(rawMessage, Integer.valueOf(db));
            return db;
        } else {
            return id;
        }
    }

    private int fromDb(Connection c, int hash, String rawMessage) {
        try {
            PreparedStatement s = c.prepareStatement("SELECT id FROM announcer_messages WHERE text_hash_code=?");
            s.setInt(1, hash);
            ResultSet r = s.executeQuery();
            if (r.next()) {
                int id = r.getInt("id");
                r.close();
                s.close();
                return id;
            } else {
                r.close();
                s.close();
                int id = insert(hash, rawMessage, c);
                return id;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            ABPromoter.getInstance().getLogger()
                    .severe("Was unable to get id of message: " + rawMessage + ", hash: " + hash);
            return -1;
        }
    }

    private int insert(int hash, String rawMessage, Connection c) {
        try {
            PreparedStatement s = c.prepareStatement(
                    "INSERT INTO announcer_messages (text_hash_code, raw_text) VALUES (?, ?)",
                    Statement.RETURN_GENERATED_KEYS);
            s.setInt(1, hash);
            s.setString(2, rawMessage);
            s.executeUpdate();
            ResultSet r = s.getGeneratedKeys();
            r.next();
            int id = r.getInt(1);
            s.close();
            r.close();
            return id;
        } catch (SQLException e) {
            e.printStackTrace();
            ABPromoter.getInstance().getLogger()
                    .severe("Was unable to insert and get id of message: " + rawMessage + ", hash: " + hash);
            return -1;
        }
    }
}
