package manners.cowardly.abpromoter;

import org.bukkit.command.CommandSender;

import manners.cowardly.abpromoter.abgrouploading.ABGroupsReloadInfo;
import manners.cowardly.abpromoter.announcer.AnnouncerABGroups;
import manners.cowardly.abpromoter.announcer.abgroup.AnnouncerABGroup;
import manners.cowardly.abpromoter.database.GetABGroupsWithMembers;
import manners.cowardly.abpromoter.database.SaveABGroup;
import manners.cowardly.abpromoter.menus.MenuABGroups;
import manners.cowardly.abpromoter.menus.MenuInventories;
import manners.cowardly.abpromoter.menus.menuabgroups.MenuABGroup;

public class Reloader {

    private MenuInventories menuInventories;
    private AnnouncerABGroups announcerGroups;
    private MenuABGroups menuGroups;
    private GetABGroupsWithMembers getDbABGroups;
    private SaveABGroup saveGroupDb;
    private PlayerABGroups playerABGroups;

    public Reloader(MenuInventories menuInventories, AnnouncerABGroups announcerGroups, MenuABGroups menuGroups,
            GetABGroupsWithMembers getDbABGroups, SaveABGroup saveGroupDb, PlayerABGroups playerABGroups) {
        this.menuInventories = menuInventories;
        this.announcerGroups = announcerGroups;
        this.menuGroups = menuGroups;
        this.getDbABGroups = getDbABGroups;
        this.saveGroupDb = saveGroupDb;
        this.playerABGroups = playerABGroups;
    }

    public void reload(CommandSender sender) {
        ABGroupsReloadInfo<AnnouncerABGroup> announcerReloader = announcerGroups.reloader(getDbABGroups, saveGroupDb);
        ABGroupsReloadInfo<MenuABGroup> menuReloader = menuGroups.reloader(getDbABGroups, saveGroupDb);
        if (announcerReloader.canReload() && menuReloader.canReload()) {
            executeReload(announcerReloader, menuReloader);
            sender.sendMessage("Reloaded.");
        } else {
            ABPromoter.getInstance().getLogger().severe("Failed to load ab groups. Reload will not execute.");
            sender.sendMessage("Failed to load ab groups. Reload will not execute.");
        }
    }

    private void executeReload(ABGroupsReloadInfo<AnnouncerABGroup> announcerReloader,
            ABGroupsReloadInfo<MenuABGroup> menuReloader) {
        menuInventories.reloadStart();
        announcerReloader.reload();
        menuReloader.reload();
        playerABGroups.reloadMessageGroupPlacements();
        menuInventories.reloadEnd();
    }
}
