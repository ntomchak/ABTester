package manners.cowardly.abpromoter.menus.buttonlinks;

import java.util.List;

import manners.cowardly.abpromoter.announcer.abgroup.components.messages.MessageTemplate;
import manners.cowardly.abpromoter.announcer.abgroup.components.messages.MessageTemplate.DeliverableMessage;

public class ChatButtonLink extends ButtonLink {

    private MessageTemplate messageBuilder;

    public ChatButtonLink(String name, List<String> rawText) {
        super(name);
        this.messageBuilder = new MessageTemplate(rawText, false, "open");
    }

    public DeliverableMessage getMessage(String webServerHostName) {
        return messageBuilder.getMessage(webServerHostName);
    }
}
