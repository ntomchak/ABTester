package manners.cowardly.abpromoter.menus.menuabgroups;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import manners.cowardly.abpromoter.ABPromoter;
import manners.cowardly.abpromoter.menus.buttonlinks.ButtonLink;
import manners.cowardly.abpromoter.menus.buttonlinks.ChatButtonLink;
import manners.cowardly.abpromoter.menus.buttonlinks.PageButtonLink;
import manners.cowardly.abpromoter.utilities.Utilities;

public class MenuPage {
    private ItemStack[] pageContents;
    // Will try to open the first page in the array, if the user's ab group does not
    // have this menu, will try to open the second, and so on
    private Map<Integer, ButtonLink> indexToLink = new HashMap<Integer, ButtonLink>();

    public MenuPage(ConfigurationSection pageSection, ItemStack[] filler) {
        pageContents = new ItemStack[filler.length];
        for (int i = 0; i < filler.length; i++)
            if (filler[i] != null)
                pageContents[i] = new ItemStack(filler[i]);
        new LoadConfiguration(pageSection);
    }

    public Optional<ButtonLink> linkAt(int index) {
        return Optional.ofNullable(indexToLink.get(index));
    }

    public ItemStack[] contents() {
        return pageContents;
    }

    private class LoadConfiguration {
        public LoadConfiguration(ConfigurationSection pageSection) {
            pageSection.getKeys(false).forEach(key -> loadButton(pageSection.getConfigurationSection(key), key));
        }

        private void loadButton(ConfigurationSection buttonSection, String buttonName) {
            int index = loadIndex(buttonSection);
            if (index < 0) {
                ABPromoter.getInstance().getLogger().warning("Not putting button in menu due to invalid index.");
                return;
            }

            loadItemStack(buttonSection, index);

            ConfigurationSection linkSection = buttonSection.getConfigurationSection("link");
            if (linkSection != null)
                loadLink(linkSection, index, buttonName);
        }

        private void loadLink(ConfigurationSection linkSection, int index, String buttonName) {
            String type = linkSection.getString("type");
            switch (type) {
            case "chat":
                loadChatLink(linkSection, index, buttonName);
                break;
            case "page":
                loadPageLink(linkSection, index, buttonName);
                break;
            default:
                ABPromoter.getInstance().getLogger().warning("Invalid link type for button \"" + buttonName + "\": \""
                        + type + "\", disregarding this link.");
                break;
            }
        }
        
        private void loadPageLink(ConfigurationSection linkSection, int index, String buttonName) {
            List<String> content = linkSection.getStringList("content");
            if (!content.isEmpty())
                indexToLink.put(index, new PageButtonLink(buttonName, content));
        }

        private void loadChatLink(ConfigurationSection linkSection, int index, String buttonName) {
            List<String> content = linkSection.getStringList("content");
            if (!content.isEmpty())
                indexToLink.put(index, new ChatButtonLink(buttonName, content));
        }

        // returns index
        private int loadItemStack(ConfigurationSection buttonSection, int index) {
            Material material = loadMaterial(buttonSection);
            String name = loadName(buttonSection);
            List<String> lore = buttonSection.getStringList("lore");
            int amount = buttonSection.getInt("stackAmount", 1);
            pageContents[index] = makeStack(material, name, lore, amount);
            return index;
        }

        private ItemStack makeStack(Material material, String name, List<String> lore, int amount) {
            ItemStack stack = new ItemStack(material, amount);
            ItemMeta meta = stack.getItemMeta();
            Utilities.hideFlags(meta);
            meta.setDisplayName(name);
            if (!lore.isEmpty())
                meta.setLore(lore);
            stack.setItemMeta(meta);
            return stack;
        }

        // -1 if invalid
        private int loadIndex(ConfigurationSection buttonSection) {
            int row = buttonSection.getInt("row");
            int column = buttonSection.getInt("column");
            int index = Utilities.inventoryIndex(row, column);
            if (index >= 0 && index < row * 9)
                return index;
            else {
                ABPromoter.getInstance().getLogger().warning(
                        "Invalid slot for item in menu, row: " + row + ", column: " + column + ", index: " + index);
                return -1;
            }
        }

        private String loadName(ConfigurationSection buttonSection) {
            String name = buttonSection.getString("name");
            if (name == null)
                name = "";
            return name;
        }

        private Material loadMaterial(ConfigurationSection buttonSection) {
            String materialString = buttonSection.getString("material");
            Optional<Material> material = Utilities.enumFromString(Material.class, materialString);
            if (material.isEmpty()) {
                ABPromoter.getInstance().getLogger()
                        .warning("Invalid material for page button: \"" + materialString + "\". Defaulting to stone.");
                return Material.STONE;
            } else
                return material.get();
        }
    }
}
