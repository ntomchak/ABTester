package manners.cowardly.abpromoter.menus;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.configuration.ConfigurationSection;
import manners.cowardly.abpromoter.database.GetABGroupsWithMembers;
import manners.cowardly.abpromoter.database.SaveABGroup;
import manners.cowardly.abpromoter.menus.menuabgroups.MenuABGroup;
import manners.cowardly.abpromoter.utilities.ABGroupsFilesLoader;
import manners.cowardly.abpromoter.utilities.WeightedProbabilities;

public class MenuABGroups {
    private WeightedProbabilities<MenuABGroup> groupsProbabilities = new WeightedProbabilities<MenuABGroup>();
    private Map<String, MenuABGroup> groupNames = new HashMap<String, MenuABGroup>();

    public MenuABGroups(GetABGroupsWithMembers getDbABGroups, SaveABGroup saveGroupDb) {
        new LoadConfiguration(getDbABGroups, saveGroupDb);
    }

    public MenuABGroup selectRandomGroup() {
        return groupsProbabilities.sample();
    }

    public MenuABGroup getGroup(String name) {
        return groupNames.get(name);
    }

    private class LoadConfiguration {
        public LoadConfiguration(GetABGroupsWithMembers getDbABGroups, SaveABGroup saveGroupDb) {
            ABGroupsFilesLoader filesLoader = new ABGroupsFilesLoader(getDbABGroups, saveGroupDb, "menus",
                    "menu_ab_groups", "menu_ab_group", "menus1", "menus2");
            saveGroups(filesLoader.getGroupConfigs(), filesLoader.getWeights());
        }

        private void saveGroups(Map<String, ConfigurationSection> groups, Map<String, Integer> weights) {
            for (Entry<String, ConfigurationSection> entry : groups.entrySet()) {
                MenuABGroup group = new MenuABGroup(entry.getValue(), entry.getKey());
                groupNames.put(entry.getKey(), group);

                Integer weight = weights.get(entry.getKey());
                if (weight != null)
                    MenuABGroups.this.groupsProbabilities.add(group, weight.doubleValue());
            }
        }
    }
}
