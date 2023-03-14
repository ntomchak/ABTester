package manners.cowardly.abpromoter.announcer.abgroup.components.messages.pieces;

import java.awt.Color;
import java.util.List;
import java.util.Map;

import manners.cowardly.abpromoter.ABPromoter;
import manners.cowardly.abpromoter.utilities.Utilities;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;

public class TextPiece extends MessagePiece {

    protected TextComponent component;

    protected TextPiece(Map<String, String> attributes, String text) {
        super();
        component = new TextComponent(text.replaceAll("`", "\n"));
        setColor(component, attributes);
        setFormatting(component, attributes);
        setHoverText(component, attributes);
    }

    @Override
    public void addComponents(List<BaseComponent> list) {
        list.add(component);
    }

    private void setHoverText(BaseComponent component, Map<String, String> attributes) {
        String raw = attributes.remove("hover_text");
        if (raw == null)
            return;
        HoverEvent hoverEvent = new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                new Text(TextComponent.fromLegacyText(raw)));
        component.setHoverEvent(hoverEvent);
    }

    private void setFormatting(BaseComponent component, Map<String, String> attributes) {
        String formattingStr = attributes.remove("formatting");
        if (formattingStr == null)
            return;
        formattingStr = formattingStr.replaceAll(" ", "");
        for (String option : formattingStr.split(","))
            setFormatting(component, option);
    }

    /**
     * ChatColor enum or hex code (#000000-#FFFFFF) White if none or invalid color
     * 
     * @param attributes
     * @return
     */
    private void setColor(BaseComponent component, Map<String, String> attributes) {
        String colorStr = attributes.remove("color");
        if (colorStr == null)
            component.setColor(ChatColor.WHITE);
        else if (colorStr.startsWith("#"))
            component.setColor(colorFromHex(colorStr));
        else if (colorStr.startsWith("&") && colorStr.length() == 2)
            component.setColor(colorFromCode(colorStr));
        else
            component.setColor(Utilities.colorFromName(colorStr));
    }

    private void setFormatting(BaseComponent component, String option) {
        switch (option.toLowerCase()) {
        case "underline":
            component.setUnderlined(true);
            break;
        case "magic":
            component.setObfuscated(true);
            break;
        case "strikethrough":
            component.setStrikethrough(true);
            break;
        case "bold":
            component.setBold(true);
            break;
        case "italic":
            component.setItalic(true);
            break;
        default:
            ABPromoter.getInstance().getLogger()
                    .warning("'" + option + "' is not a valid formatting option, ignoring this.");
        }
    }

    private ChatColor colorFromCode(String colorStr) {
        ChatColor color = ChatColor.getByChar(colorStr.charAt(1));
        if (color == null || color == ChatColor.UNDERLINE || color == ChatColor.STRIKETHROUGH
                || color == ChatColor.RESET || color == ChatColor.MAGIC || color == ChatColor.ITALIC
                || color == ChatColor.BOLD)
            return ChatColor.WHITE;
        return color;
    }

    /**
     * from (#000000-#FFFFFF)
     * 
     * @return
     */
    private ChatColor colorFromHex(String colorStr) {
        try {
            Color color = Color.decode(colorStr);
            return ChatColor.of(color);
        } catch (NumberFormatException e) {
            ABPromoter.getInstance().getLogger()
                    .warning(colorStr + "  is not a valid hex color. Will default to white color.");
            return ChatColor.WHITE;
        }
    }
}
