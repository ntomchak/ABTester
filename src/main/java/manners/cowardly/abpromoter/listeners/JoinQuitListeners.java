package manners.cowardly.abpromoter.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import manners.cowardly.abpromoter.PlayerABGroups;

public class JoinQuitListeners implements Listener {

    private PlayerABGroups playerGroups;

    public JoinQuitListeners(PlayerABGroups playerGroups) {
        this.playerGroups = playerGroups;
    }

    @EventHandler
    public void join(PlayerJoinEvent e) {
        playerGroups.login(e.getPlayer());
    }
    
    @EventHandler
    public void quit(PlayerQuitEvent e) {
        playerGroups.logout(e.getPlayer());
    }
}
