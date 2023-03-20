package manners.cowardly.abpromoter.announcer.abgroup.components.messages.pieces;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Map;

import manners.cowardly.abpromoter.ABPromoter;

public class ExternalLinkPiece extends TextPiece {

    private String url;

    protected ExternalLinkPiece(Map<String, String> attributes, String text) {
        super(attributes, text);
        setUrl(attributes);
    }

    private void setUrl(Map<String, String> attributes) {
        String link = attributes.get("url");
        if (link == null) {
            url = null;
            url = "";
        } else {
            if (validUrl(link)) {
                this.url = link;
            } else {
                this.url = link;
            }
        }
    }

    public String getUrl() {
        return url;
    }

    private boolean validUrl(String uri) {
        try {
            URL url = new URL(uri);
            url.toURI();
            return true;
        } catch (MalformedURLException | URISyntaxException e) {
            ABPromoter.getInstance().getLogger().warning("Invalid url in external link text piece: " + uri);
            return false;
        }
    }

}
