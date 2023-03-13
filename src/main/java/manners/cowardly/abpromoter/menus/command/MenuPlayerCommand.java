package manners.cowardly.abpromoter.menus.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import manners.cowardly.abpromoter.menus.MenuInventories;

public class MenuPlayerCommand implements CommandExecutor {

    private MenuInventories menuInventories;

    public MenuPlayerCommand(MenuInventories menuInventories) {
        this.menuInventories = menuInventories;
    }

    // /buy
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            playerCommand((Player) sender, args);
        }
        return true;
    }

    private void playerCommand(Player p, String[] args) {
        if (args.length == 0)
            menuInventories.openFromCommandDefaultPage(p);
        else
            menuInventories.openFromCommand(p, args[0]);
    }
}
