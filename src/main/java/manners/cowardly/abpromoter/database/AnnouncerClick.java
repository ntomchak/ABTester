package manners.cowardly.abpromoter.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.UUID;

import org.bukkit.Bukkit;

import manners.cowardly.abpromoter.ABPromoter;
import manners.cowardly.abpromoter.database.connect.ConnectionPool;
import manners.cowardly.abpromoter.database.translator.TokenIdTranslator;

public class AnnouncerClick {

    private TokenIdTranslator tokenTranslator;
    private ConnectionPool pool;

    public AnnouncerClick(TokenIdTranslator tokenTranslator, ConnectionPool pool) {
        this.tokenTranslator = tokenTranslator;
        this.pool = pool;
    }

    // from sync
    public void recordClick(String token, UUID user) {
        int delivery = tokenTranslator.idOfToken(token, user);
        if (delivery == -1) {
            ABPromoter.getInstance().getLogger().warning(
                    "Player used command of clicking announcer message but the token was not valid, either they typed it manually or the token is expired");
        } else {
            Bukkit.getScheduler().runTaskAsynchronously(ABPromoter.getInstance(), () -> insert(delivery));
        }
    }

    // from async
    private void insert(int id) {
        try (Connection c = pool.getConnection()) {
            PreparedStatement s = c.prepareStatement("INSERT INTO announcer_clicks (deliver) VALUES (?)");
            s.setInt(1, id);
            s.execute();
            s.close();
        } catch (SQLException e) {
            e.printStackTrace();
            ABPromoter.getInstance().getLogger().severe("Could not record click of delivery with id " + id);
        }
    }
}
