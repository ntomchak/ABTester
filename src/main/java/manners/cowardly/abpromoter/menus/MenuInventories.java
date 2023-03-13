package manners.cowardly.abpromoter.menus;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import manners.cowardly.abpromoter.ABPromoter;
import manners.cowardly.abpromoter.PlayerABGroups;
import manners.cowardly.abpromoter.announcer.AnnouncerTokenRecords;
import manners.cowardly.abpromoter.database.MenuClose;
import manners.cowardly.abpromoter.database.MenuLinkClick;
import manners.cowardly.abpromoter.database.MenuOpen;
import manners.cowardly.abpromoter.database.MenuPageClick;
import manners.cowardly.abpromoter.menus.menuabgroups.MenuABGroup;
import manners.cowardly.abpromoter.menus.menuabgroups.MenuPage;
import manners.cowardly.abpromoter.menus.menuabgroups.MenuPage.ButtonLink;

public class MenuInventories {

    private PlayerABGroups playerGroups;
    private Map<UUID, MenuInventory> menuInventories = new HashMap<UUID, MenuInventory>();
    private MenuOpen menuOpenDb;
    private AnnouncerTokenRecords playerTokens;
    private MenuLinkClick linkClickDb;
    private MenuPageClick pageClickDb;
    private MenuClose closeDb;

    public MenuInventories(PlayerABGroups playerGroups, MenuOpen menuOpenDb, AnnouncerTokenRecords playerTokens,
            MenuLinkClick linkClickDb, MenuPageClick pageClickDb, MenuClose closeDb) {
        this.playerGroups = playerGroups;
        this.menuOpenDb = menuOpenDb;
        this.playerTokens = playerTokens;
        this.linkClickDb = linkClickDb;
        this.pageClickDb = pageClickDb;
        this.closeDb = closeDb;
    }

    public void openFromDelivery(Player p, String token) {
        MenuABGroup playerGroup = playerGroups.getPlayerMenuABGroup(p.getUniqueId());

        // page name from token
        Optional<String> pageName = pageNameFromToken(p, token, playerGroup);
        if (pageName.isEmpty()) {
            ABPromoter.getInstance().getLogger()
                    .severe("There was no valid page of the given ones or the default for player " + p.getName());
            return;
        }

        // page
        Optional<MenuPage> page = playerGroup.getPage(pageName.get());
        if (page.isEmpty()) {
            ABPromoter.getInstance().getLogger().severe(
                    "There was no valid menu page of the name \"" + pageName.get() + "\" for player " + p.getName());
            return;
        }

        MenuInventory menu = createAndOpenInventory(p, page.get(), playerGroup.getRows(), pageName.get(),
                playerGroup.menuTitle());
        menuInventories.put(p.getUniqueId(), menu);
        menuOpenDb.fromDelivery(p.getUniqueId(), pageName.get(), token, menu);
    }

    public void openFromCommandDefaultPage(Player p) {
        MenuABGroup playerGroup = playerGroups.getPlayerMenuABGroup(p.getUniqueId());
        if (openDefaultNoToken(p, playerGroup))
            menuOpenDb.fromCommand(p.getUniqueId(), playerGroup.defaultPageName());
    }

    public void openFromCommand(Player p, String pageName) {
        if (openNoToken(p, pageName))
            menuOpenDb.fromCommand(p.getUniqueId(), pageName);
    }

    public void openFromReferral(Player p, String pageName, String referral) {
        if (openNoToken(p, pageName))
            menuOpenDb.fromReferral(p.getUniqueId(), pageName, referral);
    }

    public void openFromReferralDefaultPage(Player p, String referral) {
        MenuABGroup playerGroup = playerGroups.getPlayerMenuABGroup(p.getUniqueId());
        if (openDefaultNoToken(p, playerGroup))
            menuOpenDb.fromReferral(p.getUniqueId(), playerGroup.defaultPageName(), referral);
    }

    private boolean openDefaultNoToken(Player p, MenuABGroup playerGroup) {
        Optional<MenuPage> page = playerGroup.getPage(playerGroup.defaultPageName());
        if (page.isEmpty()) {
            ABPromoter.getInstance().getLogger().severe("No valid default page when player " + p.getName()
                    + " tried to open menu from referral or player command without specifying page.");
            return false;
        }

        MenuInventory menu = createAndOpenInventory(p, page.get(), playerGroup.getRows(), playerGroup.defaultPageName(),
                playerGroup.menuTitle());
        menuInventories.put(p.getUniqueId(), menu);
        return true;
    }

    /**
     * return false if no valid page
     * 
     * @param p
     * @param pageName
     * @return
     */
    private boolean openNoToken(Player p, String pageName) {
        MenuABGroup playerGroup = playerGroups.getPlayerMenuABGroup(p.getUniqueId());

        Optional<MenuPage> page = playerGroup.getPage(pageName);
        if (page.isEmpty()) {
            pageName = playerGroup.defaultPageName();
            page = playerGroup.getPage(pageName);
            ABPromoter.getInstance().getLogger()
                    .warning("When trying to open a menu from a command or referral, player " + p.getName()
                            + " had no valid pages, trying the default.");
            if (page.isEmpty()) {
                ABPromoter.getInstance().getLogger().severe("No valid default page.");
                return false;
            } else
                ABPromoter.getInstance().getLogger().warning("Default is ok.");
        }

        MenuInventory menu = createAndOpenInventory(p, page.get(), playerGroup.getRows(), pageName,
                playerGroup.menuTitle());
        menuInventories.put(p.getUniqueId(), menu);
        return true;
    }

    /**
     * Checks if a click is of a menu inventory of this plugin and if so, handles it
     * appropriately
     * 
     * @param p
     * @param inv
     * @param indexClicked
     * @return true if the click event should be cancelled
     */
    public boolean click(Player p, Inventory inv, int indexClicked) {
        MenuInventory menu = menuInventories.get(p.getUniqueId());
        if (menu != null && menu.getInventory().equals(inv)) {
            MenuABGroup playerGroup = playerGroups.getPlayerMenuABGroup(p.getUniqueId());
            MenuPage page = playerGroup.getPage(menu.getPageName()).get();
            Optional<ButtonLink> buttonLink = page.linkAt(indexClicked);
            if (buttonLink.isPresent()) {
                buttonLink(p, playerGroup, buttonLink.get(), menu);
            }
            return true;
        }
        return false;
    }

    public void close(UUID player, Inventory inv) {
        MenuInventory menu = menuInventories.get(player);
        if (menu.getInventory().equals(inv)) {
            menuInventories.remove(player);
            closeDb.recordMenuClose(menu.openId);
        }
    }

    private void buttonLink(Player p, MenuABGroup playerGroup, ButtonLink buttonLink, MenuInventory menu) {
        if (buttonLink.isChatLink()) {
            chatLink(p, buttonLink, menu);
        } else {
            pageLink(menu, playerGroup, buttonLink);
        }
    }

    private void pageLink(MenuInventory menu, MenuABGroup playerGroup, ButtonLink buttonLink) {
        Optional<String> pageName = playerGroup.firstValidPageOrDefault(buttonLink.getContents());
        if (pageName.isEmpty()) {
            ABPromoter.getInstance().getLogger().severe("Button name: " + buttonLink.getButtonName() + ", open id: "
                    + menu.openId + ", no valid pages or default page upon clicking a page link");
            return;
        }
        MenuPage page = playerGroup.getPage(pageName.get()).get();
        menu.getInventory().setContents(page.contents());
        pageClickDb.recordPageClick(menu.openId, buttonLink.getButtonName(), menu.pageName, pageName.get());
        menu.pageName = pageName.get();
    }

    private void chatLink(Player p, ButtonLink link, MenuInventory menu) {
        // TODO make sure this fires menu close event
        p.closeInventory();
        link.getContents().forEach(line -> p.sendMessage(line));
        linkClickDb.recordMenuLinkClick(menu.openId, link.getButtonName());
    }

    private Optional<String> pageNameFromToken(Player p, String token, MenuABGroup playerGroup) {
        Optional<String[]> pageNamesFromToken = pageNames(p, token);

        if (pageNamesFromToken.isPresent()) {
            return playerGroup.firstValidPageOrDefault(pageNamesFromToken.get());
        } else {
            ABPromoter.getInstance().getLogger()
                    .severe("Invalid/expired token for player " + p.getName() + ", token: " + token);
            return Optional.ofNullable(playerGroup.defaultPageName());
        }
    }

    private MenuInventory createAndOpenInventory(Player p, MenuPage page, int rows, String pageName, String title) {
        Inventory inventory = Bukkit.createInventory(p, rows * 9, title);
        inventory.setContents(page.contents());
        MenuInventory menu = new MenuInventory(inventory, pageName);
        p.openInventory(inventory);
        return menu;
    }

    private Optional<String[]> pageNames(Player p, String token) {
        Optional<String[]> pages = playerTokens.tokenData(p.getUniqueId(), token);
        if (pages.isEmpty()) {
            ABPromoter.getInstance().getLogger().warning("When trying to open a menu from a delivery, player "
                    + p.getName() + " and token " + token + " didn't have any associated pages.");
            return Optional.empty();
        }
        return pages;
    }

    public class MenuInventory {
        private Inventory inventory;
        private volatile int openId = -1;
        private String pageName;

        public MenuInventory(Inventory inventory, String pageName) {
            this.inventory = inventory;
        }

        public String getPageName() {
            return pageName;
        }

        public void setId(int id) {
            this.openId = id;
        }

        public int getId() {
            return openId;
        }

        public Inventory getInventory() {
            return inventory;
        }
    }
}
