package manners.cowardly.abpromoter.announcer;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.configuration.ConfigurationSection;
import manners.cowardly.abpromoter.announcer.abgroup.AnnouncerABGroup;
import manners.cowardly.abpromoter.database.GetABGroupsWithMembers;
import manners.cowardly.abpromoter.database.SaveABGroup;
import manners.cowardly.abpromoter.utilities.ABGroupsFilesLoader;
import manners.cowardly.abpromoter.utilities.WeightedProbabilities;

public class AnnouncerABGroups {
    private WeightedProbabilities<AnnouncerABGroup> groupsProbabilities = new WeightedProbabilities<AnnouncerABGroup>();
    private Map<String, AnnouncerABGroup> groupNames = new HashMap<String, AnnouncerABGroup>();

    public AnnouncerABGroups(GetABGroupsWithMembers getDbABGroups, SaveABGroup saveGroupDb) {
        new LoadConfiguration(getDbABGroups, saveGroupDb);
    }

    public AnnouncerABGroup selectRandomGroup() {
        return groupsProbabilities.sample();
    }

    public AnnouncerABGroup getGroup(String name) {
        return groupNames.get(name);
    }

    private class LoadConfiguration {
        public LoadConfiguration(GetABGroupsWithMembers getDbABGroups, SaveABGroup saveGroupDb) {
            ABGroupsFilesLoader filesLoader = new ABGroupsFilesLoader(getDbABGroups, saveGroupDb, "announcer",
                    "announcer_ab_groups", "announcer_ab_group", "announcer1", "announcer2");
            saveGroups(filesLoader.getGroupConfigs(), filesLoader.getWeights());
        }

        private void saveGroups(Map<String, ConfigurationSection> groups, Map<String, Integer> weights) {
            for (Entry<String, ConfigurationSection> entry : groups.entrySet()) {
                AnnouncerABGroup group = new AnnouncerABGroup(entry.getValue(), entry.getKey());
                groupNames.put(entry.getKey(), group);

                Integer weight = weights.get(entry.getKey());
                if (weight != null)
                    AnnouncerABGroups.this.groupsProbabilities.add(group, weight.doubleValue());
            }
        }
    }
}
