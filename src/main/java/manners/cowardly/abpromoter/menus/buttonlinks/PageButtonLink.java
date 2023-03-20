package manners.cowardly.abpromoter.menus.buttonlinks;

import java.util.Collections;
import java.util.List;

public class PageButtonLink extends ButtonLink {
    private List<String> pages;

    public PageButtonLink(String name, List<String> pages) {
        super(name);
        this.pages = pages;
    }

    public List<String> getPages() {
        return Collections.unmodifiableList(pages);
    }
}
