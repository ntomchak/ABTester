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
    private ConnectionPool pool;

    public MenuOpen(ConnectionPool pool, StringIdTranslator menuPageTranslator, StringIdTranslator referralIdTranslator,
            TokenIdTranslator tokenTranslator) {
        this.pool = pool;
        this.referralIdTranslator = referralIdTranslator;
        this.menuPageTranslator = menuPageTranslator;
        this.tokenTranslator = tokenTranslator;
    }

    // from sync
    public void fromDelivery(UUID user, String pageName, String token, MenuInventory inventory) {
        int deliveryId = tokenTranslator.idOfToken(token, user);
        Bukkit.getScheduler().runTaskAsynchronously(ABPromoter.getInstance(),
                () -> fromDeliveryAsync(user.toString(), pageName, deliveryId, inventory));
    }

    // from async
    private void fromDeliveryAsync(String user, String pageName, int deliveryId, MenuInventory inventory) {
        try (Connection c = pool.getConnection()) {
            int pageId = menuPageTranslator.idOfString(pageName);
            PreparedStatement s = c.prepareStatement(
                    "INSERT INTO menu_opens SET user=(SELECT id FROM users WHERE mc_uuid=?), page=?, announcer_delivery=?, referral=NULL",
                    Statement.RETURN_GENERATED_KEYS);
            s.setString(1, user);
            s.setInt(2, pageId);
            s.setInt(3, deliveryId);
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
    public void fromCommand(UUID user, String pageName, MenuInventory inventory) {
        Bukkit.getScheduler().runTaskAsynchronously(ABPromoter.getInstance(),
                () -> fromCommandAsync(user.toString(), pageName, inventory));
    }

    private void fromCommandAsync(String user, String pageName, MenuInventory inventory) {
        try (Connection c = pool.getConnection()) {
            int pageId = menuPageTranslator.idOfString(pageName);
            PreparedStatement s = c.prepareStatement(
                    "INSERT INTO menu_opens SET user=(SELECT id FROM users WHERE mc_uuid=?), page=?, announcer_delivery=NULL, referral=NULL",
                    Statement.RETURN_GENERATED_KEYS);
            s.setString(1, user);
            s.setInt(2, pageId);
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

    public void fromReferral(UUID user, String pageName, String referral, MenuInventory inventory) {
        Bukkit.getScheduler().runTaskAsynchronously(ABPromoter.getInstance(),
                () -> fromReferralAsync(user.toString(), pageName, referral, inventory));
    }

    private void fromReferralAsync(String user, String pageName, String referral, MenuInventory inventory) {
        try (Connection c = pool.getConnection()) {
            int pageId = menuPageTranslator.idOfString(pageName);
            int referralId = referralIdTranslator.idOfString(referral);
            PreparedStatement s = c.prepareStatement(
                    "INSERT INTO menu_opens SET user=(SELECT id FROM users WHERE mc_uuid=?), page=?, announcer_delivery=NULL, referral=?");
            s.setString(1, user);
            s.setInt(2, pageId);
            s.setInt(3, referralId);
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
