package manners.cowardly.abpromoter.announcer.abgroup.components.messages;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import manners.cowardly.abpromoter.utilities.RandomStringGenerator;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Content;
import net.md_5.bungee.api.chat.hover.content.Text;

public class MessageBuilder {
    private ArrayList<BaseComponent[]> lineComponents;
    private List<MenuPageLineIndexAndComponentIndex> clickableIndices = new ArrayList<MenuPageLineIndexAndComponentIndex>();
    private String menuCommand = "abpmto";
    private String rawMessage;

    private static Pattern findClickables = Pattern.compile("\\[(.*?)\\]\\((.*?)\\)\\{(.*?)\\}");

    public MessageBuilder(String rawText) {
        this.rawMessage = rawText;
        new BuildBuilder(rawText);
    }

    public DeliverableMessage getMessage() {
        return new DeliverableMessage();
    }

    private class BuildBuilder {

        private String marker = "@#*!-=--";

        public BuildBuilder(String rawText) {
            rawText.replace("`", "\n");
            String[] rawLines = rawText.split("`");
            ArrayList<BaseComponent[]> lines = new ArrayList<BaseComponent[]>();
            for (String rawLine : rawLines)
                lines.add(processComponentsOfLine(rawLine, lines.size()));
            lineComponents = lines;
        }

        private BaseComponent[] processComponentsOfLine(String rawLine, int lineIndex) {
            BaseComponent[] rawComponents = TextComponent.fromLegacyText(rawLine);

            List<BaseComponent> components = new ArrayList<BaseComponent>();
            for (BaseComponent component : rawComponents) {
                if (component instanceof TextComponent) {
                    processComponent((TextComponent) component, components, lineIndex);
                } else {
                    components.add(component);
                }
            }
            return components.toArray(new BaseComponent[components.size()]);
        }

        private void processComponent(TextComponent component, List<BaseComponent> components, int lineIndex) {
            String componentText = component.getText();
            List<ClickableInfo> rawClickables = rawClickables(componentText);
            // replace raw clickables with marker
            for (ClickableInfo rawClickable : rawClickables)
                componentText = componentText.replaceFirst(Pattern.quote(rawClickable.raw), marker);
            String[] split = componentText.split(marker);

            int rawClickableIndex = 0;
            for (String nonClickable : split) {
                TextComponent piece = new TextComponent(nonClickable);
                piece.setText(piece.getText().replace("\\n", "\n"));
                piece.copyFormatting(component);
                components.add(piece);
                if (rawClickableIndex < rawClickables.size())
                    components.add(clickableComponent(rawClickables.get(rawClickableIndex), components.size(),
                            lineIndex, component));
                rawClickableIndex++;
            }
        }

        private TextComponent clickableComponent(ClickableInfo rawClickable, int indexWithinLine, int lineIndex,
                TextComponent originalUnsplit) {
            TextComponent component = new TextComponent(rawClickable.text);
            if (rawClickable.hoverText.length() > 0) {
                component.setHoverEvent(
                        new HoverEvent(HoverEvent.Action.SHOW_TEXT, hoverContents(rawClickable.hoverText)));
                clickableIndices
                        .add(new MenuPageLineIndexAndComponentIndex(rawClickable.page, indexWithinLine, lineIndex));
            }
            component.copyFormatting(originalUnsplit);
            return component;
        }

        private List<Content> hoverContents(String rawHoverText) {
            String[] lines = rawHoverText.split("$n$");
            List<Content> contents = new ArrayList<Content>();
            for (String line : lines)
                contents.add(new Text(TextComponent.fromLegacyText(line)));
            return contents;
        }

        /**
         * Extract the raw clickable text
         * 
         * @param raw
         * @return
         */
        private List<ClickableInfo> rawClickables(String raw) {
            List<ClickableInfo> matchList = new ArrayList<ClickableInfo>();
            System.out.println("raw component " + raw);
            Matcher regexMatcher = findClickables.matcher(raw);

            while (regexMatcher.find()) {
                matchList.add(new ClickableInfo(regexMatcher.group(0), regexMatcher.group(1), regexMatcher.group(2),
                        regexMatcher.group(3)));
            }

            return matchList;
        }

        private class ClickableInfo {
            public ClickableInfo(String raw, String text, String page, String hoverText) {
                this.raw = raw;
                this.text = text;
                this.page = page;
                this.hoverText = hoverText;
            }

            private String raw;
            private String text;
            private String page;
            private String hoverText;
        }
    }

    public class DeliverableMessage {
        private List<BaseComponent[]> componentLines;
        private List<MessageTokenInfo> tokens;

        public DeliverableMessage() {
            this.componentLines = copyLines(MessageBuilder.this.lineComponents);
            tokens = new ArrayList<MessageTokenInfo>();
            setClickableComponents();
        }

        private List<BaseComponent[]> copyLines(List<BaseComponent[]> toCopy) {
            List<BaseComponent[]> copy = new ArrayList<BaseComponent[]>(toCopy.size());
            toCopy.forEach(line -> copy.add(Arrays.copyOf(line, line.length)));
            return copy;
        }

        private void setClickableComponents() {
            for (MenuPageLineIndexAndComponentIndex tokenBuilder : clickableIndices) {
                MessageTokenInfo token = new MessageTokenInfo(tokenBuilder.menuPage);
                tokens.add(token);

                String command = menuCommand + " " + token.getToken();
                BaseComponent[] line = componentLines.get(tokenBuilder.lineIndex);
                BaseComponent replacement = line[tokenBuilder.componentIndex].duplicate();
                replacement.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, command));
                line[tokenBuilder.componentIndex] = replacement;
            }
        }

        public void deliver(Player p) {
            componentLines.forEach(line -> p.spigot().sendMessage(line));
            System.out.println("Message: ");
            componentLines.forEach(line -> Bukkit.getConsoleSender().spigot().sendMessage(line));
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

            public MessageTokenInfo(String menuPageString) {
                this.menuPages = menuPageString.split(",");
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

    private class MenuPageLineIndexAndComponentIndex {
        public MenuPageLineIndexAndComponentIndex(String menuPage, int componentIndex, int lineIndex) {
            this.menuPage = menuPage;
            this.componentIndex = componentIndex;
            this.lineIndex = lineIndex;
        }

        private int lineIndex;
        private String menuPage;
        private int componentIndex;
    }
}
