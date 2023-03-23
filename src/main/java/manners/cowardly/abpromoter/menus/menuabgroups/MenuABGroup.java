package manners.cowardly.abpromoter.menus.menuabgroups;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import manners.cowardly.abpromoter.ABPromoter;
import manners.cowardly.abpromoter.abgrouploading.ABGroup;
import manners.cowardly.abpromoter.utilities.Utilities;

public class MenuABGroup implements ABGroup {
    private Map<String, MenuPage> pageNameToPage = new HashMap<String, MenuPage>();
    private String defaultPage;
    private String menuTitle;
    private int rows;
    private String name;

    public MenuABGroup(ConfigurationSection config, String name) {
        new LoadConfiguration(config);
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public Optional<MenuPage> getPage(String name) {
        return Optional.ofNullable(pageNameToPage.get(name));
    }

    /**
     * Returns the first valid page of the given pages, or, if none are valid, the
     * default page Returns empty optional if the default page is invalid
     * 
     * @param pageNames
     * @return
     */
    public Optional<String> firstValidPageOrDefault(String... pageNames) {
        for (String pageName : pageNames) {
            MenuPage page = pageNameToPage.get(pageName);
            if (page != null)
                return Optional.of(pageName);
        }
        return pageNameToPage.containsKey(defaultPage) ? Optional.of(defaultPage) : Optional.empty();
    }

    public Optional<String> firstValidPageOrDefault(List<String> pageNames) {
        for (String pageName : pageNames) {
            MenuPage page = pageNameToPage.get(pageName);
            if (page != null)
                return Optional.of(pageName);
        }
        return pageNameToPage.containsKey(defaultPage) ? Optional.of(defaultPage) : Optional.empty();
    }

    public String defaultPageName() {
        return defaultPage;
    }

    public String menuTitle() {
        return menuTitle;
    }

    public int getRows() {
        return rows;
    }

    public void reload(ConfigurationSection config) {
        pageNameToPage = new HashMap<String, MenuPage>();
        new LoadConfiguration(config);
    }

    private class LoadConfiguration {

        public LoadConfiguration(ConfigurationSection section) {
            int rows = section.getInt("rows", -1);
            if (rows < 1 || rows > 6) {
                ABPromoter.getInstance().getLogger()
                        .warning("Invalid number of rows for menu (" + rows + "), defaulting to 6 rows");
                rows = 6;
            }

            MenuABGroup.this.rows = rows;

            ItemStack[] filler = new ItemStack[rows * 9];

            loadFiller(section.getConfigurationSection("filler"), filler);

            defaultPage = section.getString("defaultPage", "");

            menuTitle = section.getString("menuTitle", "Store");

            Map<String, List<String>> buttonContents = loadButtonContents(
                    section.getConfigurationSection("buttonContent"));

            loadPages(section.getConfigurationSection("pages"), filler, buttonContents);
        }

        private Map<String, List<String>> loadButtonContents(ConfigurationSection buttonContentSection) {
            Map<String, List<String>> contents = new HashMap<String, List<String>>();
            if (buttonContentSection != null) {
                Collection<String> keys = buttonContentSection.getKeys(false);
                keys.forEach(key -> contents.put(key, buttonContentSection.getStringList(key)));
            }
            return contents;
        }

        private void loadPages(ConfigurationSection pagesSection, ItemStack[] filler,
                Map<String, List<String>> buttonContent) {
            pagesSection.getKeys(false)
                    .forEach(key -> loadPage(pagesSection.getConfigurationSection(key), key, filler, buttonContent));
        }

        private void loadPage(ConfigurationSection pageSection, String pageName, ItemStack[] filler,
                Map<String, List<String>> buttonContent) {
            MenuPage page = new MenuPage(pageSection, filler, buttonContent);
            pageNameToPage.put(pageName, page);
        }

        private void loadFiller(ConfigurationSection fillerSection, ItemStack[] stacks) {
            fillerSection.getKeys(false)
                    .forEach(key -> loadFillerItem(fillerSection.getConfigurationSection(key), stacks));
        }

        private void loadFillerItem(ConfigurationSection itemSection, ItemStack[] stacks) {
            Optional<ItemStack> stack = fillerItemStack(itemSection);
            if (stack.isPresent())
                fillFiller(itemSection, stacks, stack.get());
        }

        private void fillFiller(ConfigurationSection itemSection, ItemStack[] stacks, ItemStack stack) {
            fillFillerSlots(itemSection, stacks, stack);
            fillFillerRows(itemSection, stacks, stack);
        }

        private void fillFillerSlots(ConfigurationSection itemSection, ItemStack[] stacks, ItemStack stack) {
            List<Integer> slots = itemSection.getIntegerList("slots");
            for (Integer slot : slots)
                if (slot.intValue() < stacks.length && slot.intValue() >= 0)
                    stacks[slot.intValue()] = stack;
        }

        private void fillFillerRows(ConfigurationSection itemSection, ItemStack[] stacks, ItemStack stack) {
            List<Integer> rows = itemSection.getIntegerList("rows");
            for (Integer row : rows) {
                int start = Utilities.inventoryIndex(row, 1);
                int end = Utilities.inventoryIndex(row, 9);
                for (int i = start; i <= end; i++)
                    stacks[i] = stack;
            }
        }

        private Optional<ItemStack> fillerItemStack(ConfigurationSection itemSection) {
            String materialStr = itemSection.getString("material");
            Optional<Material> material = Utilities.enumFromString(Material.class, materialStr);
            if (material.isEmpty()) {
                ABPromoter.getInstance().getLogger()
                        .warning("Invalid material for filler item (" + materialStr + "), disregarding filler item");
                return Optional.empty();
            }

            int amount = itemSection.getInt("stackAmount");
            if (amount < 1 || amount > 64) {
                ABPromoter.getInstance().getLogger()
                        .warning("Invalid stack amount for filler item (" + amount + "), disregarding filler item");
                return Optional.empty();
            }

            String name = itemSection.getString("name", "");

            List<String> lore = itemSection.getStringList("lore");

            ItemStack stack = new ItemStack(material.get(), amount);
            ItemMeta meta = stack.getItemMeta();
            meta.setLore(lore);
            meta.setDisplayName(name);
            stack.setItemMeta(meta);
            return Optional.of(stack);
        }
    }
}
