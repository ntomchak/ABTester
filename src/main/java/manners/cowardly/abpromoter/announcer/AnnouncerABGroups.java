package manners.cowardly.abpromoter.announcer;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.configuration.file.YamlConfiguration;

import manners.cowardly.abpromoter.ABPromoter;
import manners.cowardly.abpromoter.announcer.abgroup.AnnouncerABGroup;
import manners.cowardly.abpromoter.database.SaveABGroup;
import manners.cowardly.abpromoter.utilities.WeightedProbabilities;

public class AnnouncerABGroups {
    private WeightedProbabilities<AnnouncerABGroup> groups = new WeightedProbabilities<AnnouncerABGroup>();
    private Map<String, AnnouncerABGroup> groupNames = new HashMap<String, AnnouncerABGroup>();

    public AnnouncerABGroups(SaveABGroup saveGroupDb) {
        new LoadConfiguration(saveGroupDb);
    }

    public AnnouncerABGroup selectRandomGroup() {
        return groups.sample();
    }

    public AnnouncerABGroup getGroup(String name) {
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
                            .warning("Invalid weight for announcer ab group '" + key + "', ignoring this group.");
                } else {
                    weights.put(key, weight);
                }
            }
            return weights;
        }

        private YamlConfiguration weightsConfig() {
            ABPromoter.getInstance().saveResource("announcer/ab_group_weights.yml", false);
            File weightsFile = new File(ABPromoter.getInstance().getDataFolder() + "/announcer/ab_group_weights.yml");
            return YamlConfiguration.loadConfiguration(weightsFile);
        }

        private void loadABGroups(SaveABGroup saveGroupDb, Map<String, Integer> groupToWeight) {
            File directory = abGroupsDirectory();

            for (Entry<String, Integer> entry : groupToWeight.entrySet()) {
                File groupFile = new File(directory + "/" + entry.getKey() + ".yml");
                if (!groupFile.exists()) {
                    ABPromoter.getInstance().getLogger()
                            .warning(directory + "/" + entry.getKey() + ".yml for announcer ab_group '" + entry.getKey()
                                    + "', which is listed in the group weights, does not exist. Not loading this group.");
                } else {
                    String name = entry.getKey();
                    YamlConfiguration groupConfig = YamlConfiguration.loadConfiguration(groupFile);
                    AnnouncerABGroup group = new AnnouncerABGroup(groupConfig, name);
                    AnnouncerABGroups.this.groups.add(group, entry.getValue());
                    AnnouncerABGroups.this.groupNames.put(name, group);
                    saveGroupDb.saveAnnouncerGroup(name);
                }
            }
        }

        private File abGroupsDirectory() {
            File directory = new File(ABPromoter.getInstance().getDataFolder().toString() + "/announcer/ab_groups");
            if (!directory.exists()) {
                directory.mkdir();
                ABPromoter.getInstance().saveResource("announcer/ab_groups/announcer1.yml", false);
                ABPromoter.getInstance().saveResource("announcer/ab_groups/announcer2.yml", false);
            }
            return directory;
        }
    }
}
