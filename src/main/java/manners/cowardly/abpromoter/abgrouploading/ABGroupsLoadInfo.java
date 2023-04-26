package manners.cowardly.abpromoter.abgrouploading;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;

import manners.cowardly.abpromoter.ABPromoter;
import manners.cowardly.abpromoter.database.GetABGroupsWithMembers;
import manners.cowardly.abpromoter.database.SaveABGroup;
import manners.cowardly.abpromoter.utilities.DiscreteProbabilityDistribution;

public class ABGroupsLoadInfo<T extends ABGroup> {

    private DiscreteProbabilityDistribution<T> groupsProbabilities = new DiscreteProbabilityDistribution<T>();
    private Map<String, T> groupNames = new HashMap<String, T>();

    public ABGroupsLoadInfo(ABGroupConstructor<T> constructor, GetABGroupsWithMembers getDbABGroups,
            SaveABGroup saveGroupDb, String directoryName, String abGroupsDbTableName, String usersTableColumnName,
            String... defaultGroupNames) {
        ABGroupsFilesLoader filesLoader = new ABGroupsFilesLoader(getDbABGroups, saveGroupDb, directoryName,
                abGroupsDbTableName, usersTableColumnName, defaultGroupNames);
        if (filesLoader.successful()) {
            saveGroups(constructor, filesLoader.getGroupConfigs(), filesLoader.getWeights());
        } else {
            ABPromoter.getInstance().getLogger()
                    .severe("Due to the plugin being loaded unsuccessfully, the plugin will be disabled.");
            Bukkit.getPluginManager().disablePlugin(ABPromoter.getInstance());
        }
    }

    public DiscreteProbabilityDistribution<T> groupsProbabilities() {
        return groupsProbabilities;
    }

    public Map<String, T> groupNames() {
        return groupNames;
    }

    private void saveGroups(ABGroupConstructor<T> constructor, Map<String, ConfigurationSection> groups,
            Map<String, Integer> weights) {
        for (Entry<String, ConfigurationSection> entry : groups.entrySet()) {
            T group = constructor.constructor(entry.getValue(), entry.getKey());
            groupNames.put(entry.getKey(), group);

            Integer weight = weights.get(entry.getKey());
            
            if (weight != null) {
                groupsProbabilities.add(group, weight.doubleValue());
            }
        }
    }
}
