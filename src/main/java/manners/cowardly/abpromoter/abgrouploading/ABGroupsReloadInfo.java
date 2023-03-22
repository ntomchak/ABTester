package manners.cowardly.abpromoter.abgrouploading;

import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.configuration.ConfigurationSection;

import manners.cowardly.abpromoter.ABPromoter;
import manners.cowardly.abpromoter.database.GetABGroupsWithMembers;
import manners.cowardly.abpromoter.database.SaveABGroup;
import manners.cowardly.abpromoter.utilities.WeightedProbabilities;

public class ABGroupsReloadInfo<T extends ABGroup> {
    private WeightedProbabilities<T> groupsProbabilities;
    private ABGroupsFilesLoader filesLoader;
    private Map<String, T> groupNames;
    private ABGroupConstructor<T> constructor;

    public ABGroupsReloadInfo(ABGroupConstructor<T> constructor, GetABGroupsWithMembers getDbABGroups,
            SaveABGroup saveGroupDb, WeightedProbabilities<T> groupsProbabilities, Map<String, T> groupNames,
            String directoryName, String abGroupsDbTableName, String usersTableColumnName,
            String... defaultGroupNames) {
        this.groupsProbabilities = groupsProbabilities;
        this.groupNames = groupNames;
        this.constructor = constructor;
        filesLoader = new ABGroupsFilesLoader(getDbABGroups, saveGroupDb, directoryName, abGroupsDbTableName,
                usersTableColumnName, defaultGroupNames);
    }

    /**
     * True if the ab group files were successfully loaded, with no groups that have
     * members missing configuration files
     * 
     * @return
     */
    public boolean canReload() {
        return filesLoader.successful();
    }

    /**
     * True if executed
     * 
     * @return
     */
    public boolean reload() {
        if (filesLoader.successful()) {
            updateCurrentGroups(filesLoader, groupNames);
            addNewGroupNames(constructor, filesLoader, groupNames);
            reloadWeights(filesLoader, groupNames);
            return true;
        } else {
            logUnsuccessful();
            return false;
        }
    }

    public WeightedProbabilities<T> newProbabilities() {
        return groupsProbabilities;
    }

    private void logUnsuccessful() {
        ABPromoter.getInstance().getLogger().severe("Due to the reload being unsuccessful, it will be cancelled.");
    }

    private void updateCurrentGroups(ABGroupsFilesLoader filesLoader, Map<String, T> groupNames) {
        Map<String, ConfigurationSection> groupConfigs = filesLoader.getGroupConfigs();
        for (Entry<String, T> entry : groupNames.entrySet())
            entry.getValue().reload(groupConfigs.get(entry.getKey()));
    }

    private void addNewGroupNames(ABGroupConstructor<T> constructor, ABGroupsFilesLoader filesLoader,
            Map<String, T> groupNames) {
        Map<String, ConfigurationSection> groupConfigs = filesLoader.getGroupConfigs();
        for (Entry<String, ConfigurationSection> entry : groupConfigs.entrySet())
            if (!groupNames.containsKey(entry.getKey()))
                groupNames.put(entry.getKey(), constructor.constructor(entry.getValue(), entry.getKey()));
    }

    private void reloadWeights(ABGroupsFilesLoader filesLoader, Map<String, T> groupNames) {
        Map<String, Integer> weights = filesLoader.getWeights();
        WeightedProbabilities<T> probabilities = new WeightedProbabilities<T>();
        weights.entrySet().forEach(entry -> probabilities.add(groupNames.get(entry.getKey()), entry.getValue()));
        groupsProbabilities.setContents(probabilities);
    }
}
