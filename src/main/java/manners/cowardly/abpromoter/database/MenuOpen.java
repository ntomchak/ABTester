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
import manners.cowardly.abpromoter.database.translator.StringIdTranslator;
import manners.cowardly.abpromoter.database.translator.TokenIdTranslator;
import manners.cowardly.abpromoter.menus.MenuInventories.MenuInventory;

public class MenuOpen {

    private StringIdTranslator menuPageTranslator;
    private StringIdTranslator referralIdTranslator;
    private TokenIdTranslator tokenTranslator;
    private StringIdTranslator ipIdTranslator;
    private ConnectionPool pool;

    public MenuOpen(ConnectionPool pool, StringIdTranslator menuPageTranslator, StringIdTranslator referralIdTranslator,
            TokenIdTranslator tokenTranslator, StringIdTranslator ipIdTranslator) {
        this.pool = pool;
        this.referralIdTranslator = referralIdTranslator;
        this.menuPageTranslator = menuPageTranslator;
        this.tokenTranslator = tokenTranslator;
        this.ipIdTranslator = ipIdTranslator;
    }

    // from sync
    public void fromReload(UUID user, String pageName, MenuInventory inventory, String ip) {
        Bukkit.getScheduler().runTaskAsynchronously(ABPromoter.getInstance(),
                () -> fromReloadAsync(user.toString(), pageName, inventory, ip));
    }

    private void fromReloadAsync(String user, String pageName, MenuInventory inventory, String ip) {
        try (Connection c = pool.getConnection()) {
            int pageId = menuPageTranslator.idOfString(c, pageName);
            int ipId = ipIdTranslator.idOfString(c, ip);
            PreparedStatement s = c.prepareStatement(
                    "INSERT INTO menu_opens SET user=(SELECT id FROM users WHERE mc_uuid=?), page=?, announcer_delivery=NULL, referral=NULL, from_reload=1, ip_address=?",
                    Statement.RETURN_GENERATED_KEYS);
            s.setString(1, user);
            s.setInt(2, pageId);
            s.setInt(3, ipId);
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
            int pageId = menuPageTranslator.idOfString(c, pageName);
            int ipId = ipIdTranslator.idOfString(c, ip);
            PreparedStatement s = c.prepareStatement(
                    "INSERT INTO menu_opens SET user=(SELECT id FROM users WHERE mc_uuid=?), page=?, announcer_delivery=?, referral=NULL, ip_address=?",
                    Statement.RETURN_GENERATED_KEYS);
            s.setString(1, user);
            s.setInt(2, pageId);
            s.setInt(3, deliveryId);
            s.setInt(4, ipId);
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
            int pageId = menuPageTranslator.idOfString(c, pageName);
            int ipId = ipIdTranslator.idOfString(c, ip);
            PreparedStatement s = c.prepareStatement(
                    "INSERT INTO menu_opens SET user=(SELECT id FROM users WHERE mc_uuid=?), page=?, announcer_delivery=NULL, referral=NULL, ip_address=?",
                    Statement.RETURN_GENERATED_KEYS);
            s.setString(1, user);
            s.setInt(2, pageId);
            s.setInt(3, ipId);
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
            int pageId = menuPageTranslator.idOfString(c, pageName);
            int referralId = referralIdTranslator.idOfString(c, referral);
            int ipId = ipIdTranslator.idOfString(c, ip);
            PreparedStatement s = c.prepareStatement(
                    "INSERT INTO menu_opens SET user=(SELECT id FROM users WHERE mc_uuid=?), page=?, announcer_delivery=NULL, referral=?, ip_address=?");
            s.setString(1, user);
            s.setInt(2, pageId);
            s.setInt(3, referralId);
            s.setInt(4, ipId);
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
