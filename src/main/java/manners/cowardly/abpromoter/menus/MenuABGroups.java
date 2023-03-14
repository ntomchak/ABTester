package manners.cowardly.abpromoter.menus;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.configuration.file.YamlConfiguration;

import manners.cowardly.abpromoter.ABPromoter;
import manners.cowardly.abpromoter.database.SaveABGroup;
import manners.cowardly.abpromoter.menus.menuabgroups.MenuABGroup;
import manners.cowardly.abpromoter.utilities.WeightedProbabilities;

public class MenuABGroups {
    private WeightedProbabilities<MenuABGroup> groups = new WeightedProbabilities<MenuABGroup>();
    private Map<String, MenuABGroup> groupNames = new HashMap<String, MenuABGroup>();

    public MenuABGroups(SaveABGroup saveGroupDb) {
        new LoadConfiguration(saveGroupDb);
    }

    public MenuABGroup selectRandomGroup() {
        return groups.sample();
    }

    public MenuABGroup getGroup(String name) {
        return groupNames.get(name);
    }

    private class LoadConfiguration {
        public LoadConfiguration(SaveABGroup saveGroupDb) {
            Map<String, Integer> groupToWeight = loadWeights();
            loadABGroups(saveGroupDb, groupToWeight);
        }

        private Map<String, Integer> loadWeights() {
            YamlConfiguration weightsConfig = weightsConfig();

            Map<String, Integer> weights = new HashMap<String, Integer>();
            Collection<String> keys = weightsConfig.getKeys(false);
            for (String key : keys) {
                int weight = weightsConfig.getInt(key, -1);
                if (weight < 1) {
                    ABPromoter.getInstance().getLogger()
                            .warning("Invalid weight for menu ab group '" + key + "', ignoring this group.");
                } else {
                    weights.put(key, weight);
                }
            }
            return weights;
        }

        private YamlConfiguration weightsConfig() {
            ABPromoter.getInstance().saveResource("menus/ab_group_weights.yml", false);
            File weightsFile = new File(ABPromoter.getInstance().getDataFolder() + "/menus/ab_group_weights.yml");
            return YamlConfiguration.loadConfiguration(weightsFile);
        }

        private void loadABGroups(SaveABGroup saveGroupDb, Map<String, Integer> groupToWeight) {
            File directory = abGroupsDirectory();

            for (Entry<String, Integer> entry : groupToWeight.entrySet()) {
                File groupFile = new File(directory + "/" + entry.getKey() + ".yml");
                if (!groupFile.exists()) {
                    ABPromoter.getInstance().getLogger()
                            .warning(directory + "/" + entry.getKey() + ".yml for menu ab_group '" + entry.getKey()
                                    + "', which is listed in the group weights, does not exist. Not loading this group.");
                } else {
                    YamlConfiguration groupConfig = YamlConfiguration.loadConfiguration(groupFile);
                    String name = entry.getKey();
                    MenuABGroup group = new MenuABGroup(groupConfig, name);
                    MenuABGroups.this.groups.add(group, entry.getValue());
                    MenuABGroups.this.groupNames.put(name, group);
                    saveGroupDb.saveMenuGroup(name);
                }
            }
        }

        private File abGroupsDirectory() {
            File directory = new File(ABPromoter.getInstance().getDataFolder().toString() + "/menus/ab_groups");
            if (!directory.exists()) {
                directory.mkdir();
                ABPromoter.getInstance().saveResource("menus/ab_groups/menus1.yml", false);
                ABPromoter.getInstance().saveResource("menus/ab_groups/menus2.yml", false);
            }
            return directory;
        }
    }
}
