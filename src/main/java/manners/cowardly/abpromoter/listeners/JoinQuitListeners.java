package manners.cowardly.abpromoter.listeners;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import manners.cowardly.abpromoter.ABPromoter;
import manners.cowardly.abpromoter.PlayerABGroups;
import manners.cowardly.abpromoter.database.UserIpAddress;
import manners.cowardly.abpromoter.utilities.Utilities;

public class JoinQuitListeners implements Listener {

    private PlayerABGroups playerGroups;
    private UserIpAddress userIps;

    public JoinQuitListeners(PlayerABGroups playerGroups, UserIpAddress userIps) {
        this.playerGroups = playerGroups;
        this.userIps = userIps;
    }

    @EventHandler
    public void join(PlayerJoinEvent e) {
        Player p = e.getPlayer();
        playerGroups.login(p);
        Bukkit.getScheduler().runTaskLaterAsynchronously(ABPromoter.getInstance(),
                () -> userIps.recordAddressAsync(Utilities.playerIp(p), p.getUniqueId().toString()), 4 * 20);
    }

    @EventHandler
    public void quit(PlayerQuitEvent e) {
        playerGroups.logout(e.getPlayer());
    }
}
