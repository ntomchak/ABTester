package manners.cowardly.abpromoter.announcer.abgroup.components.messages.pieces;

import java.util.List;

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;

public class LegacyTextPiece implements MessagePiece {

    private BaseComponent[] components;

    protected LegacyTextPiece(String rawText) {
        rawText = rawText.replaceAll("`", "\n");
        components = TextComponent.fromLegacyText(rawText);
    }

    @Override
    public void appendComponents(List<BaseComponent> list) {
        for (BaseComponent component : components)
            list.add(component);
    }

}
