package manners.cowardly.abpromoter.announcer.abgroup.components.messages.pieces;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MessagePieceFactory {
    
    private static Pattern wholeStringPattern = Pattern.compile("<(.*?)(?: (\\[.*?=.*?\\]))??>(.*?)");
    private static Pattern attributesPattern = Pattern.compile("\\[(.*?)=(.*?)\\]");
    
    public static MessagePiece fromRawText(String rawText) {
        Matcher matcher = wholeStringPattern.matcher(rawText);
        if (matcher.matches()) {
            String tag = matcher.group(1);
            String attributesStr = matcher.group(2);
            String text = matcher.group(3);
            return fromParts(tag, attributesStr, text);
        } else {
            return new LegacyTextPiece(rawText);
        }
    }

    private static MessagePiece fromParts(String tag, String attributesStr, String text) {
        if (tag == null)
            tag = "";
        if (attributesStr == null)
            attributesStr = "";
        if (text == null)
            text = "";
        switch (tag) {
        case "text":
            return new TextPiece(attributes(attributesStr), text);
        case "menu_link":
            return new MenuLinkPiece(attributes(attributesStr), text);
        case "external_link":
            return new ExternalLinkPiece(attributes(attributesStr), text);
        case "new_line":
            return new NewLinePiece();
        case "legacy_text":
            return new LegacyTextPiece(text);
        default:
            return new LegacyTextPiece(text);
        }

    }

    private static Map<String, String> attributes(String attributesStr) {
        Map<String, String> attributes = new HashMap<String, String>();
        Matcher matcher = attributesPattern.matcher(attributesStr);
        while (matcher.find()) {
            String attribute = matcher.group(1);
            String value = matcher.group(2);
            if (attribute != null) {
                if (value == null)
                    attributes.put(attribute, "");
                else
                    attributes.put(attribute, value);
            }
        }
        return attributes;
    }
}
