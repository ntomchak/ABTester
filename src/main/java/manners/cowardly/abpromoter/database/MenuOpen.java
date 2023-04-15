package manners.cowardly.abpromoter.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.UUID;

import org.bukkit.Bukkit;

import manners.cowardly.abpromoter.ABPromoter;
import manners.cowardly.abpromoter.database.connect.ConnectionPool;
import manners.cowardly.abpromoter.database.translator.TokenIdTranslator;
import manners.cowardly.abpromoter.menus.MenuInventories.MenuInventory;

public class MenuOpen {

    private TokenIdTranslator tokenTranslator;
    private ConnectionPool pool;

    public MenuOpen(ConnectionPool pool, TokenIdTranslator tokenTranslator) {
        this.pool = pool;
        this.tokenTranslator = tokenTranslator;
    }

    // from sync
    public void fromReload(UUID user, String pageName, MenuInventory inventory, String ip) {
        Bukkit.getScheduler().runTaskAsynchronously(ABPromoter.getInstance(),
                () -> fromReloadAsync(user.toString(), pageName, inventory, ip));
    }

    private void fromReloadAsync(String user, String pageName, MenuInventory inventory, String ip) {
        try (Connection c = pool.getConnection()) {
            PreparedStatement s = c.prepareStatement("INSERT INTO menu_opens SET user=" + QueryConstants.SELECT_USER
                    + ", page=" + QueryConstants.SELECT_PAGE
                    + ", announcer_delivery=NULL, referral=NULL, from_reload=1, ip_address=" + QueryConstants.SELECT_IP,
                    Statement.RETURN_GENERATED_KEYS);
            s.setString(1, user);
            s.setString(2, pageName);
            s.setString(3, ip);
            s.executeUpdate();
            ResultSet r = s.getGeneratedKeys();
            r.next();
            int openId = r.getInt(1);
            inventory.setId(openId);
            s.close();
        } catch (SQLException e) {
            e.printStackTrace();
            ABPromoter.getInstance().getLogger().severe(
                    "Unable to insert into database the record of a menu open. The menu open happened as a result of a reload, the user is \""
                            + user + "\" and the pageName is \"" + pageName + "\".");
        }
    }

    // from sync
    public void fromDelivery(UUID user, String pageName, String token, MenuInventory inventory, String ip) {
        int deliveryId = tokenTranslator.idOfToken(token, user);
        Bukkit.getScheduler().runTaskAsynchronously(ABPromoter.getInstance(),
                () -> fromDeliveryAsync(user.toString(), pageName, deliveryId, inventory, ip));
    }

    // from async
    private void fromDeliveryAsync(String user, String pageName, int deliveryId, MenuInventory inventory, String ip) {
        try (Connection c = pool.getConnection()) {
            PreparedStatement s = c.prepareStatement(
                    "INSERT INTO menu_opens SET user=" + QueryConstants.SELECT_USER + ", page=" + QueryConstants.SELECT_PAGE
                            + ", announcer_delivery=?, referral=NULL, ip_address=" + QueryConstants.SELECT_IP,
                    Statement.RETURN_GENERATED_KEYS);
            s.setString(1, user);
            s.setString(2, pageName);
            s.setInt(3, deliveryId);
            s.setString(4, ip);
            s.executeUpdate();
            ResultSet r = s.getGeneratedKeys();
            r.next();
            int openId = r.getInt(1);
            inventory.setId(openId);
            r.close();
            s.close();
        } catch (SQLException e) {
            e.printStackTrace();
            ABPromoter.getInstance().getLogger().severe(
                    "Unable to insert into database the record of a menu open. The menu open happened as a result of a message delivery, the user is \""
                            + user + "\", the pageName is \"" + pageName + "\", and the delivery id is " + deliveryId);
        }
    }

    // from sync
    public void fromCommand(UUID user, String pageName, MenuInventory inventory, String ip) {
        Bukkit.getScheduler().runTaskAsynchronously(ABPromoter.getInstance(),
                () -> fromCommandAsync(user.toString(), pageName, inventory, ip));
    }

    private void fromCommandAsync(String user, String pageName, MenuInventory inventory, String ip) {
        try (Connection c = pool.getConnection()) {
            PreparedStatement s = c.prepareStatement(
                    "INSERT INTO menu_opens SET user=" + QueryConstants.SELECT_USER + ", page=" + QueryConstants.SELECT_PAGE
                            + ", announcer_delivery=NULL, referral=NULL, ip_address=" + QueryConstants.SELECT_IP,
                    Statement.RETURN_GENERATED_KEYS);
            s.setString(1, user);
            s.setString(2, pageName);
            s.setString(3, ip);
            s.executeUpdate();
            ResultSet r = s.getGeneratedKeys();
            r.next();
            int openId = r.getInt(1);
            inventory.setId(openId);
            s.close();
        } catch (SQLException e) {
            e.printStackTrace();
            ABPromoter.getInstance().getLogger().severe(
                    "Unable to insert into database the record of a menu open. The menu open happened as a result of a message delivery, the user is \""
                            + user + "\" and the pageName is \"" + pageName + "\".");
        }
    }

    public void fromReferral(UUID user, String pageName, String referral, MenuInventory inventory, String ip) {
        Bukkit.getScheduler().runTaskAsynchronously(ABPromoter.getInstance(),
                () -> fromReferralAsync(user.toString(), pageName, referral, inventory, ip));
    }

    private void fromReferralAsync(String user, String pageName, String referral, MenuInventory inventory, String ip) {
        try (Connection c = pool.getConnection()) {
            PreparedStatement s = c.prepareStatement("INSERT INTO menu_opens SET user=" + QueryConstants.SELECT_USER
                    + ", page=" + QueryConstants.SELECT_PAGE + ", announcer_delivery=NULL, referral="
                    + QueryConstants.SELECT_REFERRAL + ", ip_address=" + QueryConstants.SELECT_IP);
            s.setString(1, user);
            s.setString(2, pageName);
            s.setString(3, referral);
            s.setString(4, ip);
            s.executeUpdate();
            ResultSet r = s.getGeneratedKeys();
            r.next();
            int openId = r.getInt(1);
            inventory.setId(openId);
            s.close();
        } catch (SQLException e) {
            e.printStackTrace();
            ABPromoter.getInstance().getLogger().severe(
                    "Unable to insert into database the record of a menu open. The menu open happened as a result of a message delivery, the user is \""
                            + user + "\" and the pageName is \"" + pageName + "\".");
        }
    }
}
