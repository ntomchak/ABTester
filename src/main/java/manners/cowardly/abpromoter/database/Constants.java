package manners.cowardly.abpromoter.database;

public class Constants {
    protected static final String SELECT_USER = "(SELECT id FROM users WHERE mc_uuid=?)";
    protected static final String SELECT_PAGE = "(SELECT id FROM menu_page_names WHERE name=?)";
    protected static final String SELECT_IP = "(SELECT id FROM ip_addresses WHERE ip_address=?)";
    protected static final String SELECT_REFERRAL = "(SELECT id FROM menu_referral WHERE name=?)";
    protected static final String SELECT_MENU_BUTTON = "(SELECT id FROM menu_button_names WHERE name=?)";
}
