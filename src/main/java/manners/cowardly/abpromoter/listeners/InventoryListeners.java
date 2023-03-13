package manners.cowardly.abpromoter.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;

import manners.cowardly.abpromoter.menus.MenuInventories;

public class InventoryListeners implements Listener {

    private MenuInventories menuInventories;

    public InventoryListeners(MenuInventories menuInventories) {
        this.menuInventories = menuInventories;
    }

    @EventHandler
    public void close(InventoryCloseEvent e) {
        menuInventories.close(e.getPlayer().getUniqueId(), e.getInventory());
    }

    @EventHandler
    public void click(InventoryClickEvent e) {
        if (e.getWhoClicked() instanceof Player)
            if (menuInventories.click((Player) e.getWhoClicked(), e.getClickedInventory(), e.getSlot()))
                e.setCancelled(true);
    }
}
