package manners.cowardly.abpromoter;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import manners.cowardly.abpromoter.announcer.AnnouncerABGroups;
import manners.cowardly.abpromoter.announcer.Deliverer;
import manners.cowardly.abpromoter.announcer.abgroup.AnnouncerABGroup;
import manners.cowardly.abpromoter.announcer.abgroup.components.MessageGroup;
import manners.cowardly.abpromoter.database.GetPlayerABGroups;
import manners.cowardly.abpromoter.database.GetPlayerABGroups.ABGroupNames;
import manners.cowardly.abpromoter.database.InsertPlayerABGroups;
import manners.cowardly.abpromoter.menus.MenuABGroups;
import manners.cowardly.abpromoter.menus.menuabgroups.MenuABGroup;

public class PlayerABGroups {

    private Map<UUID, MenuABGroup> playerMenuGroups = new HashMap<UUID, MenuABGroup>();
    private Map<UUID, AnnouncerABGroup> playerAnnouncerGroups = new HashMap<UUID, AnnouncerABGroup>();
    private GetPlayerABGroups getGroupsDb;
    private InsertPlayerABGroups recordGroupsDb;
    private AnnouncerABGroups announcerGroups;
    private MenuABGroups menuGroups;
    private Deliverer deliverer;

    public PlayerABGroups(GetPlayerABGroups getGroupsDb, InsertPlayerABGroups recordGroupsDb,
            AnnouncerABGroups announcerGroups, MenuABGroups menuGroups, Deliverer deliverer) {
        this.getGroupsDb = getGroupsDb;
        this.recordGroupsDb = recordGroupsDb;
        this.announcerGroups = announcerGroups;
        this.menuGroups = menuGroups;
        this.deliverer = deliverer;
        
        // update message groups every 10 minutes
        Bukkit.getScheduler().runTaskTimer(ABPromoter.getInstance(), () -> reloadMessageGroupPlacements(), 12000, 12000);
    }

    public void reloadMessageGroupPlacements() {
        playerAnnouncerGroups.entrySet()
                .forEach(entry -> updatePlayerMessageGroupPlacement(entry.getKey(), entry.getValue()));
    }

    private void updatePlayerMessageGroupPlacement(UUID uuid, AnnouncerABGroup abGroup) {
        Player p = Bukkit.getPlayer(uuid);
        MessageGroup msgGroup = abGroup.msgGroupOfPlayer(p);
        deliverer.updateMessageGroup(uuid, msgGroup);
    }

    public MenuABGroup getPlayerMenuABGroup(UUID player) {
        return playerMenuGroups.get(player);
    }

    public AnnouncerABGroup getPlayerAnnouncerABGroup(UUID player) {
        return playerAnnouncerGroups.get(player);
    }

    public void login(Player p) {
        Bukkit.getScheduler().runTaskAsynchronously(ABPromoter.getInstance(), () -> loginAsync(p.getUniqueId()));
    }

    public void logout(Player p) {
        playerMenuGroups.remove(p.getUniqueId());
        playerAnnouncerGroups.remove(p.getUniqueId());
        deliverer.cancelDeliveries(p);
    }

    // from async
    private void loginAsync(UUID uuid) {
        Optional<ABGroupNames> abGroups = getGroupsDb.getGroups(uuid);
        if (abGroups.isPresent()) {
            Bukkit.getScheduler().runTask(ABPromoter.getInstance(), () -> loginSyncPutGroups(uuid, abGroups.get()));
        } else {
            Bukkit.getScheduler().runTask(ABPromoter.getInstance(), () -> loginSyncSelectGroups(uuid));
        }
    }

    private void loginSyncSelectGroups(UUID uuid) {
        Player p = Bukkit.getPlayer(uuid);
        if (p.isOnline()) {
            MenuABGroup menu = menuGroups.selectRandomGroup();
            AnnouncerABGroup announcer = announcerGroups.selectRandomGroup();
            playerMenuGroups.put(uuid, menu);
            playerAnnouncerGroups.put(uuid, announcer);
            recordGroupsDb.insert(uuid, announcer.getName(), menu.getName());
            addToDeliverer(p, announcer);
        }
    }

    private void loginSyncPutGroups(UUID uuid, ABGroupNames abGroups) {
        Player p = Bukkit.getPlayer(uuid);
        if (p.isOnline()) {
            playerMenuGroups.put(uuid, menuGroups.getGroup(abGroups.menu));
            AnnouncerABGroup announcer = announcerGroups.getGroup(abGroups.announcer);
            playerAnnouncerGroups.put(uuid, announcer);
            addToDeliverer(p, announcer);
        }
    }

    private void addToDeliverer(Player p, AnnouncerABGroup announcerGroup) {
        MessageGroup msgGroup = announcerGroup.msgGroupOfPlayer(p);
        deliverer.addPlayer(p, msgGroup);
    }
}
