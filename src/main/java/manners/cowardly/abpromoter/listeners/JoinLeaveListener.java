package manners.cowardly.abpromoter.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import manners.cowardly.abpromoter.PlayerABGroups;

public class JoinLeaveListener implements Listener{
    
    private PlayerABGroups playerGroups;

    @EventHandler
    public void join(PlayerJoinEvent e) {
        playerGroups.login(e.getPlayer());
    }
    
    @EventHandler
    public void leave(PlayerQuitEvent e) {
        playerGroups.logout(e.getPlayer());
    }
}
