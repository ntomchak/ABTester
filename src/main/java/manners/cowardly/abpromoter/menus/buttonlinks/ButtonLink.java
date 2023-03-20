package manners.cowardly.abpromoter.menus.buttonlinks;

public abstract class ButtonLink {
    private String name;

    public ButtonLink(String name) {
        this.name = name;
    }

    public String getButtonName() {
        return name;
    }
}
