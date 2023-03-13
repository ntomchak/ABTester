package manners.cowardly.abpromoter.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;
import java.util.UUID;

import manners.cowardly.abpromoter.ABPromoter;
import manners.cowardly.abpromoter.database.connect.ConnectionPool;

public class GetPlayerABGroups {
    private ConnectionPool pool;

    public GetPlayerABGroups(ConnectionPool pool) {
        this.pool = pool;
    }

    public Optional<ABGroupNames> getGroups(UUID uuid) {
        try (Connection c = pool.getConnection()) {
            PreparedStatement s = c.prepareStatement("SELECT announcer_ab_groups.name, menu_ab_groups.name FROM users "
                    + "INNER JOIN announcer_ab_groups ON users.announcer_ab_group = announcer_ab_groups.id "
                    + "INNER JOIN menu_ab_groups ON users.menu_ab_group = menu_ab_groups.id WHERE mc_uuid=?");
            s.setString(1, uuid.toString());
            ResultSet r = s.executeQuery();
            if (r.next()) {
                String announcer = r.getString("announcer_ab_groups.name");
                String menu = r.getString("menu_ab_groups.name");
                r.close();
                s.close();
                return Optional.of(new ABGroupNames(announcer, menu));
            } else {
                r.close();
                s.close();
                return Optional.empty();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            ABPromoter.getInstance().getLogger().severe("Could not get ab groups for " + uuid);
            return Optional.empty();
        }
    }

    public class ABGroupNames {
        public ABGroupNames(String announcer, String menu) {
            this.announcer = announcer;
            this.menu = menu;
        }

        public String announcer;
        public String menu;

    }
}
