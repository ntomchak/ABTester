package manners.cowardly.abpromoter.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

import org.bukkit.Bukkit;

import manners.cowardly.abpromoter.ABPromoter;
import manners.cowardly.abpromoter.database.connect.ConnectionPool;

public class UserIpAddress {
    private ConnectionPool pool;

    public UserIpAddress(ConnectionPool pool) {
        this.pool = pool;
    }

    // from sync only
    public void recordAddress(String ipAddress, UUID player) {
        Bukkit.getScheduler().runTaskAsynchronously(ABPromoter.getInstance(),
                () -> recordAddressAsync(ipAddress, player.toString()));
    }

    // from async only
    public void recordAddressAsync(String ipAddress, String uuid) {
        try (Connection c = pool.getConnection()) {
            if (!checkIfUserIpRecordExists(c, ipAddress, uuid)) {
                PreparedStatement s = c.prepareStatement("INSERT INTO user_ip_addresses SET ip_address="
                        + Constants.SELECT_IP + ", user=(SELECT id FROM users WHERE mc_uuid=?)");
                s.setString(1, ipAddress);
                s.setString(2, uuid);
                s.execute();
                s.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            ABPromoter.getInstance().getLogger()
                    .severe("Could not record the ip address for player " + uuid + ", ip address: " + ipAddress);
        }
    }

    /**
     * return user if if exists, -1 if no
     * 
     * @param id
     * @param uuid
     * @return
     */
    private boolean checkIfUserIpRecordExists(Connection c, String ip, String uuid) {
        try {
            PreparedStatement s = c.prepareStatement("SELECT user FROM user_ip_addresses WHERE ip_address="
                    + Constants.SELECT_IP + " AND user=(SELECT id FROM users WHERE mc_uuid=?)");
            s.setString(1, ip);
            s.setString(2, uuid);
            ResultSet r = s.executeQuery();
            if (r.next()) {
                r.close();
                s.close();
                return true;
            } else {
                r.close();
                s.close();
                return false;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            ABPromoter.getInstance().getLogger()
                    .severe("Could not check if this ip-user pair is already in the database. uuid: " + uuid
                            + ", ip address: " + ip);
            return false;
        }
    }
}
