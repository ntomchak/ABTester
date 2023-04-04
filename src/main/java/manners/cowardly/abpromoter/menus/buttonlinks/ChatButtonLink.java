package manners.cowardly.abpromoter.menus.buttonlinks;

import java.util.List;

import manners.cowardly.abpromoter.announcer.abgroup.components.messages.MessageBuilder;
import manners.cowardly.abpromoter.announcer.abgroup.components.messages.MessageBuilder.DeliverableMessage;

public class ChatButtonLink extends ButtonLink {

    private MessageBuilder messageBuilder;

    public ChatButtonLink(String name, List<String> rawText) {
        super(name);
        this.messageBuilder = new MessageBuilder(rawText, false, "open");
    }

    public DeliverableMessage getMessage(String webServerHostName) {
        return messageBuilder.getMessage(webServerHostName);
    }
}
