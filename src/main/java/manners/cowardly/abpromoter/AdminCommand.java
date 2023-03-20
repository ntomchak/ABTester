package manners.cowardly.abpromoter;

import org.bukkit.Bukkit;
import org.bukkit.Statistic;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import manners.cowardly.abpromoter.announcer.Deliverer;
import manners.cowardly.abpromoter.utilities.Utilities;

public class AdminCommand implements CommandExecutor {

    private Reloader reloader;
    private PlayerABGroups playerGroups;
    private Deliverer deliverer;

    public AdminCommand(Reloader reloader, PlayerABGroups playerGroups, Deliverer deliverer) {
        this.reloader = reloader;
        this.playerGroups = playerGroups;
        this.deliverer = deliverer;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender.hasPermission("abpromoter.admin")) {
            if (args.length == 0)
                help(sender);
            else if (args[0].equalsIgnoreCase("reload"))
                reload(sender);
            else if (args[0].equalsIgnoreCase("playtime"))
                playtime(sender);
            else if (args[0].equalsIgnoreCase("announcerabgroup") || args[0].equalsIgnoreCase("ang"))
                announcerAbGroup(sender);
            else if (args[0].equalsIgnoreCase("menuabgroup") || args[0].equalsIgnoreCase("meg"))
                menuAbGroup(sender);
            else if (args[0].equalsIgnoreCase("address"))
                address(sender);
        }
        return true;
    }

    private void address(CommandSender sender) {
        Bukkit.getOnlinePlayers().forEach(p -> sender.sendMessage(p.getName() + ": " + Utilities.playerIp(p)));
    }

    private void menuAbGroup(CommandSender sender) {
        Bukkit.getOnlinePlayers().forEach(p -> showPlayerMenuABGroup(p, sender));
    }

    private void announcerAbGroup(CommandSender sender) {
        Bukkit.getOnlinePlayers().forEach(p -> showPlayerAnnouncerABGroup(p, sender));
    }

    private void showPlayerMenuABGroup(Player p, CommandSender sender) {
        String groupName = playerGroups.getPlayerMenuABGroup(p.getUniqueId()).getName();
        sender.sendMessage("Menu AB group for '" + p.getName() + "': " + groupName);
    }

    private void showPlayerAnnouncerABGroup(Player p, CommandSender sender) {
        String abGroup = playerGroups.getPlayerAnnouncerABGroup(p.getUniqueId()).getName();
        String msgGroup = deliverer.playerMessageGroupName(p.getUniqueId());
        sender.sendMessage("Announcer AB group for '" + p.getName() + "': " + abGroup + ",  msg group: " + msgGroup);
    }

    private void playtime(CommandSender sender) {
        Bukkit.getOnlinePlayers().forEach(p -> showPlayerPlaytime(p, sender));
    }

    private void showPlayerPlaytime(Player p, CommandSender sender) {
        int ticksPlayed = p.getStatistic(Statistic.PLAY_ONE_MINUTE);
        int secondsPlayed = ticksPlayed / 20;
        double hoursPlayed = (double) (ticksPlayed) / 72000.0;
        String playtimeString = Utilities.timeAgoString(secondsPlayed);
        sender.sendMessage("Playtime for '" + p.getName() + "': " + playtimeString + " (" + hoursPlayed + " hours).");
    }

    private void reload(CommandSender sender) {
        sender.sendMessage("Attempting to reload.");
        reloader.reload(sender);
    }

    private void help(CommandSender sender) {
        sender.sendMessage("'/abpromoter reload' : reload");
        sender.sendMessage("'/abpromoter playtime' : lists the playtimes of all online players");
        sender.sendMessage(
                "'/abpromoter announcerabgroup/ang' : lists the announcer ab groups and message groups of all online players");
        sender.sendMessage("'/abpromoter menuabgroup/meg' : lists the menu ab groups of all online players");
        sender.sendMessage("'/abpromoter address' : lists ip address of all online players");
    }
}
