package manners.cowardly.abpromoter.announcer.abgroup.components.messages;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.entity.Player;

import manners.cowardly.abpromoter.announcer.abgroup.components.messages.pieces.MenuLinkPiece;
import manners.cowardly.abpromoter.announcer.abgroup.components.messages.pieces.MessagePiece;
import manners.cowardly.abpromoter.utilities.RandomStringGenerator;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;

public class MessageBuilder {
    private BaseComponent[] components;
    private List<MenuPageAndComponentIndex> clickableIndices = new ArrayList<MenuPageAndComponentIndex>();
    private String menuCommand = "abpmto";
    private String rawMessage;

    public MessageBuilder(List<String> rawText) {
        new BuildBuilder(rawText);
    }

    public DeliverableMessage getMessage() {
        return new DeliverableMessage();
    }
    
    public String toString() {
        return rawMessage;
    }

    private class BuildBuilder {
        public BuildBuilder(List<String> rawText) {
            combineRawText(rawText);
            loadPieces(rawText);
        }

        private void loadPieces(List<String> rawText) {
            List<BaseComponent> components = new ArrayList<BaseComponent>();
            rawText.forEach(rawPiece -> addPiece(rawPiece, components));
            MessageBuilder.this.components = components.toArray(BaseComponent[]::new);
        }

        private void addPiece(String rawPiece, List<BaseComponent> components) {
            MessagePiece piece = MessagePiece.fromRawText(rawPiece);
            piece.appendComponents(components);
            if (piece instanceof MenuLinkPiece)
                saveClickablePiece(components.size() - 1, (MenuLinkPiece) piece);
        }

        private void saveClickablePiece(int index, MenuLinkPiece piece) {
            MenuPageAndComponentIndex clickableInfo = new MenuPageAndComponentIndex(((MenuLinkPiece) piece).getPages(),
                    index);
            clickableIndices.add(clickableInfo);
        }

        private void combineRawText(List<String> rawText) {
            StringBuilder builder = new StringBuilder(100);
            rawText.forEach(raw -> builder.append(raw));
            rawMessage = builder.toString();
        }
    }

    public class DeliverableMessage {
        private BaseComponent[] components;
        private List<MessageTokenInfo> tokens;

        public DeliverableMessage() {
            components = Arrays.copyOf(MessageBuilder.this.components, MessageBuilder.this.components.length);
            tokens = new ArrayList<MessageTokenInfo>();
            setClickableComponents();
        }

        private void setClickableComponents() {
            for (MenuPageAndComponentIndex clickableInfo : clickableIndices) {
                MessageTokenInfo token = new MessageTokenInfo(clickableInfo.menuPages);
                tokens.add(token);

                String command = menuCommand + " " + token.getToken();
                BaseComponent copy = components[clickableInfo.componentIndex].duplicate();
                copy.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, command));
                components[clickableInfo.componentIndex] = copy;
            }
        }

        public void deliver(Player p) {
            p.spigot().sendMessage(components);
        }

        public String getRawText() {
            return MessageBuilder.this.rawMessage;
        }

        public List<MessageTokenInfo> getTokens() {
            return tokens;
        }

        public class MessageTokenInfo {
            private String token;
            private String[] menuPages;

            public MessageTokenInfo(String[] menuPages) {
                this.menuPages = menuPages;
                token = RandomStringGenerator.getString(10);
            }

            public String getToken() {
                return token;
            }

            public String[] getMenuPages() {
                return menuPages;
            }
        }
    }

    private class MenuPageAndComponentIndex {
        public MenuPageAndComponentIndex(String[] menuPages, int componentIndex) {
            this.menuPages = menuPages;
            this.componentIndex = componentIndex;
        }

        private String[] menuPages;
        private int componentIndex;
    }
}
