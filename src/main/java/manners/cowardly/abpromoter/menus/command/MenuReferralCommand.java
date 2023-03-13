package manners.cowardly.abpromoter.menus.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import manners.cowardly.abpromoter.menus.MenuInventories;

// "abpmre <referral> <pageName (optional)>"
public class MenuReferralCommand implements CommandExecutor {
    private MenuInventories menuInventories;

    public MenuReferralCommand(MenuInventories menuInventories) {
        this.menuInventories = menuInventories;
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
            menuInventories.openFromReferralDefaultPage(p, args[0]);
        } else if (args.length == 2) {
            menuInventories.openFromReferral(p, args[1], args[0]);
        }
    }

}
