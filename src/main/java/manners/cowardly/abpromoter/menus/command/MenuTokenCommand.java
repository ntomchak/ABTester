package manners.cowardly.abpromoter.menus.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import manners.cowardly.abpromoter.database.AnnouncerClick;
import manners.cowardly.abpromoter.menus.MenuInventories;

// "abpmto <token>" command
public class MenuTokenCommand implements CommandExecutor {

    private MenuInventories menuInventories;
    private AnnouncerClick clickDb;

    public MenuTokenCommand(MenuInventories menuInventories, AnnouncerClick clickDb) {
        this.menuInventories = menuInventories;
        this.clickDb = clickDb;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            playerCommand((Player) sender, args);
        }
        return true;
    }

    private void playerCommand(Player p, String[] args) {
        if (args.length == 1) {
            String token = args[0];
            clickDb.recordClick(token, p.getUniqueId());
            menuInventories.openFromDelivery(p, token);
        }
    }
}
