package manners.cowardly.abpromoter.utilities;

import java.util.Optional;

import net.md_5.bungee.api.ChatColor;

public class Utilities {
    public static int inventoryIndex(int row, int col) {
        return (row - 1) * 9 + (col - 1);
    }

    public static <T extends Enum<T>> Optional<T> enumFromString(Class<T> type, String str) {
        try {
            return Optional.of(Enum.valueOf(type, str.toUpperCase()));
        } catch (IllegalArgumentException | NullPointerException e) {
            return Optional.empty();
        }
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
}
