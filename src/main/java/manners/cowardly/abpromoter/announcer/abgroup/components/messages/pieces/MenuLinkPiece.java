package manners.cowardly.abpromoter.announcer.abgroup.components.messages.pieces;

import java.util.Map;

public class MenuLinkPiece extends TextPiece {

    private String[] pages;

    protected MenuLinkPiece(Map<String, String> attributes, String text) {
        super(attributes, text);
        setPages(attributes);
    }

    public String[] getPages() {
        return pages;
    }

    private void setPages(Map<String, String> attributes) {
        String pagesRaw = attributes.remove("pages");
        if (pagesRaw == null)
            pages = new String[0];
        else {
            pagesRaw = pagesRaw.replaceAll(" ", "");
            pages = pagesRaw.split(",");
        }
    }
}
