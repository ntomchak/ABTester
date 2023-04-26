package manners.cowardly.abpromoter.announcer.abgroup.components.messages.pieces;

import java.util.List;

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;

public class NewLinePiece implements MessagePiece {

    @Override
    public void appendComponents(List<BaseComponent> list) {
        list.add(new TextComponent("\n"));
    }

}
