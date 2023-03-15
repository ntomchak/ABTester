package manners.cowardly.abpromoter;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class AdminCommand implements CommandExecutor {

    private Reloader reloader;

    public AdminCommand(Reloader reloader) {
        this.reloader = reloader;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender.hasPermission("abpromoter.admin")) {
            if (args.length == 0) {
                help(sender);
            } else if (args[0].equalsIgnoreCase("reload")) {
                sender.sendMessage("Attempting to reload.");
                reloader.reload(sender);
            }
        }
        return true;
    }

    private void help(CommandSender sender) {
        sender.sendMessage("'/abpromoter reload' : reload");
    }
}
