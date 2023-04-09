package manners.cowardly.abpromoter;

import org.bukkit.Bukkit;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import manners.cowardly.abpromoter.announcer.AnnouncerABGroups;
import manners.cowardly.abpromoter.announcer.AnnouncerTokenRecords;
import manners.cowardly.abpromoter.announcer.Deliverer;
import manners.cowardly.abpromoter.database.AnnouncerClick;
import manners.cowardly.abpromoter.database.AnnouncerDeliveries;
import manners.cowardly.abpromoter.database.GetABGroupsWithMembers;
import manners.cowardly.abpromoter.database.GetPlayerABGroups;
import manners.cowardly.abpromoter.database.InsertPlayerABGroups;
import manners.cowardly.abpromoter.database.MenuClose;
import manners.cowardly.abpromoter.database.MenuLinkClick;
import manners.cowardly.abpromoter.database.MenuOpen;
import manners.cowardly.abpromoter.database.MenuPageClick;
import manners.cowardly.abpromoter.database.SaveABGroup;
import manners.cowardly.abpromoter.database.UserIpAddress;
import manners.cowardly.abpromoter.database.connect.ConnectionPool;
import manners.cowardly.abpromoter.database.redis.Redis;
import manners.cowardly.abpromoter.database.translator.TokenIdTranslator;
import manners.cowardly.abpromoter.listeners.InventoryListeners;
import manners.cowardly.abpromoter.listeners.JoinQuitListeners;
import manners.cowardly.abpromoter.menus.MenuABGroups;
import manners.cowardly.abpromoter.menus.MenuInventories;
import manners.cowardly.abpromoter.menus.command.MenuPlayerCommand;
import manners.cowardly.abpromoter.menus.command.MenuReferralCommand;
import manners.cowardly.abpromoter.menus.command.MenuTokenCommand;
import net.milkbowl.vault.permission.Permission;

public class ABPromoter extends JavaPlugin {
    private static Permission perms = null;
    private static JavaPlugin instance;
    private ConnectionPool pool;
    private MenuInventories menuInventories;

    // TODO reload, disable
    public void onEnable() {
        if (!setupPermissions()) {
            getLogger().severe(
                    String.format("[%s] - Disabled due to no Vault dependency found!", getDescription().getName()));
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        instance = this;
        saveDefaultConfig();

        //
        pool = new ConnectionPool(getConfig().getConfigurationSection("database"));

        // translator
        TokenIdTranslator tokenTranslator = new TokenIdTranslator();

        // announcer tokens
        AnnouncerTokenRecords tokenRecords = new AnnouncerTokenRecords();

        // Redis
        String webHostName = getConfig().getString("externalLinks.other.webServerHostName");
        String serverName = getConfig().getString("externalLinks.other.serverName");
        Redis redis = new Redis(serverName, getConfig().getConfigurationSection("externalLinks.redis"));

        // database
        SaveABGroup saveGroupDb = new SaveABGroup(pool);
        GetPlayerABGroups getGroupsDb = new GetPlayerABGroups(pool);
        InsertPlayerABGroups recordGroupsDb = new InsertPlayerABGroups(pool);
        AnnouncerClick announcerClickDb = new AnnouncerClick(tokenTranslator, pool);
        AnnouncerDeliveries deliveriesDb = new AnnouncerDeliveries(tokenTranslator, pool, redis);
        MenuClose menuCloseDb = new MenuClose(pool);
        MenuLinkClick linkClickDb = new MenuLinkClick(pool, redis);
        MenuOpen menuOpenDb = new MenuOpen(pool, tokenTranslator);
        MenuPageClick pageClickDb = new MenuPageClick(pool);
        GetABGroupsWithMembers abGroupsWithMembers = new GetABGroupsWithMembers(pool);
        UserIpAddress userIps = new UserIpAddress(pool);

        // ab groups
        AnnouncerABGroups announcerGroups = new AnnouncerABGroups(abGroupsWithMembers, saveGroupDb);
        MenuABGroups menuGroups = new MenuABGroups(abGroupsWithMembers, saveGroupDb);

        // deliverer
        Deliverer deliverer = new Deliverer(deliveriesDb, tokenRecords, webHostName);

        // player ab groups
        PlayerABGroups playerGroups = new PlayerABGroups(getGroupsDb, recordGroupsDb, announcerGroups, menuGroups,
                deliverer);

        // Menu inventories
        menuInventories = new MenuInventories(playerGroups, menuOpenDb, tokenRecords, linkClickDb,
                pageClickDb, menuCloseDb, webHostName);

        // Reloader
        Reloader reloader = new Reloader(menuInventories, announcerGroups, menuGroups, abGroupsWithMembers, saveGroupDb,
                playerGroups);

        // Menu commands
        getCommand("buy").setExecutor(new MenuPlayerCommand(menuInventories));
        getCommand("abpmre").setExecutor(new MenuReferralCommand(menuInventories));
        getCommand("abpmto").setExecutor(new MenuTokenCommand(menuInventories, announcerClickDb));
        getCommand("abpromoter").setExecutor(new AdminCommand(reloader, playerGroups, deliverer));

        // Listeners
        Bukkit.getPluginManager().registerEvents(new InventoryListeners(menuInventories), instance);
        Bukkit.getPluginManager().registerEvents(new JoinQuitListeners(playerGroups, userIps), instance);
    }

    public void onDisable() {
        menuInventories.closeAll();
        pool.close();
    }

    public static JavaPlugin getInstance() {
        return instance;
    }

    public static Permission getPerms() {
        return perms;
    }

    private boolean setupPermissions() {
        RegisteredServiceProvider<Permission> rsp = getServer().getServicesManager().getRegistration(Permission.class);
        perms = rsp.getProvider();
        return perms != null;
    }
}
