package manners.cowardly.abpromoter.utilities;

import java.util.Optional;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.meta.ItemMeta;

import net.md_5.bungee.api.ChatColor;

public class Utilities {
    public static int inventoryIndex(int row, int col) {
        return (row - 1) * 9 + (col - 1);
    }
    
    public static String playerIp(Player p) {
        return p.getAddress().getAddress().getHostAddress();
    }

    public static <T extends Enum<T>> Optional<T> enumFromString(Class<T> type, String str) {
        try {
            return Optional.of(Enum.valueOf(type, str.toUpperCase()));
        } catch (IllegalArgumentException | NullPointerException e) {
            return Optional.empty();
        }
    }
    
    public static void hideFlags(ItemMeta meta) {
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        meta.addItemFlags(ItemFlag.HIDE_DESTROYS);
        meta.addItemFlags(ItemFlag.HIDE_DYE);
        meta.addItemFlags(ItemFlag.HIDE_PLACED_ON);
        meta.addItemFlags(ItemFlag.HIDE_POTION_EFFECTS);
        meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
    }

    public static ChatColor colorFromName(String str) {
        switch (str.toUpperCase()) {
        case "AQUA":
            return ChatColor.AQUA;
        case "BLACK":
            return ChatColor.BLACK;
        case "BLUE":
            return ChatColor.BLUE;
        case "WHITE":
            return ChatColor.WHITE;
        case "DARK_AQUA":
            return ChatColor.DARK_AQUA;
        case "DARK_BLUE":
            return ChatColor.DARK_BLUE;
        case "DARK_GRAY":
            return ChatColor.DARK_GRAY;
        case "DARK_GREEN":
            return ChatColor.DARK_GREEN;
        case "DARK_PURPLE":
            return ChatColor.DARK_PURPLE;
        case "DARK_RED":
            return ChatColor.DARK_RED;
        case "GOLD":
            return ChatColor.GOLD;
        case "GRAY":
            return ChatColor.GRAY;
        case "GREEN":
            return ChatColor.GREEN;
        case "LIGHT_PURPLE":
            return ChatColor.LIGHT_PURPLE;
        case "RED":
            return ChatColor.RED;
        case "YELLOW":
            return ChatColor.YELLOW;
        default:
            return ChatColor.WHITE;
        }
    }

    public static String timeAgoString(long secondsAgo) {
        long timeAgo = secondsAgo;
        if (timeAgo < 60)
            return "less than 1 minute";
        // minutes
        timeAgo /= 60;
        if (timeAgo == 1)
            return "1 minute";
        if (timeAgo < 120)
            return timeAgo + " minutes";

        // hours
        timeAgo /= 60;
        if (timeAgo < 48)
            return timeAgo + " hours";

        // days
        timeAgo /= 24;
        if (timeAgo == 1)
            return "1 day";
        return timeAgo + " days";
    }
}
