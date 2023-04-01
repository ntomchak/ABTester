package manners.cowardly.abpromoter.announcer.abgroup.components.messages;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.bukkit.entity.Player;

import manners.cowardly.abpromoter.announcer.abgroup.components.messages.pieces.ExternalLinkPiece;
import manners.cowardly.abpromoter.announcer.abgroup.components.messages.pieces.MenuLinkPiece;
import manners.cowardly.abpromoter.announcer.abgroup.components.messages.pieces.MessagePiece;
import manners.cowardly.abpromoter.utilities.RandomStringGenerator;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;

public class MessageTemplate {
    private BaseComponent[] components;
    private List<MenuPageAndComponentIndex> clickableMenuIndices = new ArrayList<MenuPageAndComponentIndex>();
    private List<UrlAndComponentIndex> clickableLinkIndices = new ArrayList<UrlAndComponentIndex>();
    private String menuCommand = "/abpmto";
    private String rawMessage;
    private boolean allowMenuLinks;
    private int externalLinkTokenLength;

    /**
     * @param rawText
     * @param allowMenuLinks
     * @param type           "open" or "delivery"
     */

    public MessageTemplate(List<String> rawText, boolean allowMenuLinks, String type) {
        this.allowMenuLinks = allowMenuLinks;
        saveType(type);
        new Load(rawText);
    }

    private void saveType(String type) {
        if (type == "open") {
            externalLinkTokenLength = 8;
        } else if (type == "delivery") {
            externalLinkTokenLength = 11;
        }
    }

    public DeliverableMessage getMessage(String webServerHostName) {
        return new DeliverableMessage(webServerHostName);
    }

    public String toString() {
        return rawMessage;
    }

    private class Load {
        public Load(List<String> rawText) {
            combineRawText(rawText);
            loadPieces(rawText);
        }

        private void loadPieces(List<String> rawText) {
            List<BaseComponent> components = new ArrayList<BaseComponent>();
            rawText.forEach(rawPiece -> addPiece(rawPiece, components));
            MessageTemplate.this.components = components.toArray(BaseComponent[]::new);
        }

        private void addPiece(String rawPiece, List<BaseComponent> components) {
            MessagePiece piece = MessagePiece.fromRawText(rawPiece);
            piece.appendComponents(components);
            if (allowMenuLinks && piece instanceof MenuLinkPiece)
                saveClickableMenuPiece(components.size() - 1, (MenuLinkPiece) piece);
            else if (piece instanceof ExternalLinkPiece)
                saveClickableLinkPiece(components.size() - 1, (ExternalLinkPiece) piece);
        }

        private void saveClickableLinkPiece(int index, ExternalLinkPiece piece) {
            UrlAndComponentIndex clickableInfo = new UrlAndComponentIndex(piece.getUrl(), index);
            clickableLinkIndices.add(clickableInfo);
        }

        private void saveClickableMenuPiece(int index, MenuLinkPiece piece) {
            MenuPageAndComponentIndex clickableInfo = new MenuPageAndComponentIndex(((MenuLinkPiece) piece).getPages(),
                    index);
            clickableMenuIndices.add(clickableInfo);
        }

        private void combineRawText(List<String> rawText) {
            StringBuilder builder = new StringBuilder(100);
            rawText.forEach(raw -> builder.append(raw));
            rawMessage = builder.toString();
        }
    }

    public class DeliverableMessage {
        private BaseComponent[] components;
        private List<MessageMenuTokenInfo> menuTokens;
        private List<MessageLinkTokenInfo> linkTokens;

        public DeliverableMessage(String webServerHostName) {
            components = Arrays.copyOf(MessageTemplate.this.components, MessageTemplate.this.components.length);
            menuTokens = new ArrayList<MessageMenuTokenInfo>();
            linkTokens = new ArrayList<MessageLinkTokenInfo>();
            if (allowMenuLinks)
                setClickableMenuComponents();
            setClickableLinkComponents(webServerHostName);
        }

        private void setClickableLinkComponents(String webServerHostName) {
            for (UrlAndComponentIndex clickableInfo : clickableLinkIndices) {
                MessageLinkTokenInfo token = new MessageLinkTokenInfo(clickableInfo.url);
                linkTokens.add(token);

                String clickableUrl = clickableUrl(webServerHostName, token.getToken(), clickableInfo);
                BaseComponent copy = components[clickableInfo.componentIndex].duplicate();
                copy.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, clickableUrl));
                components[clickableInfo.componentIndex] = copy;
            }
        }

        private String clickableUrl(String webServerHostName, String token, UrlAndComponentIndex clickableInfo) {
            StringBuilder buildClickableUrl = new StringBuilder(40).append("http://").append(webServerHostName)
                    .append("/").append(token);
            return buildClickableUrl.toString();
        }

        private void setClickableMenuComponents() {
            for (MenuPageAndComponentIndex clickableInfo : clickableMenuIndices) {
                MessageMenuTokenInfo token = new MessageMenuTokenInfo(clickableInfo.menuPages);
                menuTokens.add(token);

                String command = menuCommand + " " + token.getToken();
                BaseComponent copy = components[clickableInfo.componentIndex].duplicate();
                copy.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, command));
                components[clickableInfo.componentIndex] = copy;
            }
        }

        public void deliver(Player p) {
            if (components.length == 0 || (components.length == 1 && components[0].toPlainText().equals("")))
                ;
            else
                p.spigot().sendMessage(components);
        }

        public String getRawText() {
            return MessageTemplate.this.rawMessage;
        }

        public List<MessageMenuTokenInfo> getMenuTokens() {
            return Collections.unmodifiableList(menuTokens);
        }

        public List<MessageLinkTokenInfo> getLinkTokens() {
            return Collections.unmodifiableList(linkTokens);
        }

        public class MessageLinkTokenInfo {
            private String token;
            private String url;

            public MessageLinkTokenInfo(String url) {
                token = RandomStringGenerator.getString(externalLinkTokenLength);
                this.url = url;
            }

            public String getToken() {
                return token;
            }

            public String getUrl() {
                return url;
            }
        }

        public class MessageMenuTokenInfo {
            private String token;
            private String[] menuPages;

            public MessageMenuTokenInfo(String[] menuPages) {
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

    private class UrlAndComponentIndex {
        public UrlAndComponentIndex(String url, int componentIndex) {
            this.url = url;
            this.componentIndex = componentIndex;
        }

        private String url;
        private int componentIndex;
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
