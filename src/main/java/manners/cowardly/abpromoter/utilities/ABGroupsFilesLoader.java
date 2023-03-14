package manners.cowardly.abpromoter.utilities;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import manners.cowardly.abpromoter.ABPromoter;
import manners.cowardly.abpromoter.database.GetABGroupsWithMembers;

public class ABGroupsFilesLoader {

    private Map<String, ConfigurationSection> configs;
    private Map<String, Integer> weights;
    private boolean successful = false;

    public ABGroupsFilesLoader(GetABGroupsWithMembers getDbABGroups, String directoryName, String abGroupsDbTableName,
            String usersTableColumnName, String... defaultGroupNames) {
        makeDirectoryIfNotExists(directoryName);
        weights = weights(directoryName);
        configs = groups(directoryName, defaultGroupNames);
        Collection<String> groupsWithMembers = getDbABGroups.abGroupsWithMembers(abGroupsDbTableName,
                usersTableColumnName);
        checkWeightsForInvalidGroups(configs, weights, directoryName);
        successful = checkForMissingGroups(groupsWithMembers, configs, directoryName);
    }

    public Map<String, ConfigurationSection> getGroupConfigs() {
        return configs;
    }

    public Map<String, Integer> getWeights() {
        return weights;
    }

    public boolean successful() {
        return successful;
    }

    private boolean checkForMissingGroups(Collection<String> groupsWithMembers,
            Map<String, ConfigurationSection> groupConfigs, String directoryName) {
        boolean success = true;
        for (String dbGroupName : groupsWithMembers) {
            if (!groupConfigs.containsKey(dbGroupName)) {
                success = false;
                ABPromoter.getInstance().getLogger().severe("There are players who are a member of the " + directoryName
                        + " ab_group, however, there is no config file for this group. As a result, this load will be regarded as unsuccessful.");
            }
        }
        return success;
    }

    private void checkWeightsForInvalidGroups(Map<String, ConfigurationSection> configs, Map<String, Integer> weights,
            String directoryName) {
        Iterator<String> weightKeys = weights.keySet().iterator();
        while (weightKeys.hasNext()) {
            String weightKey = weightKeys.next();
            if (!configs.containsKey(weightKey)) {
                ABPromoter.getInstance().getLogger()
                        .warning(directoryName + " ab group is listed in weights, but has no config file: " + weightKey
                                + ". Ignoring this ab group.");
                weightKeys.remove();
            }
        }
    }

    private void makeDirectoryIfNotExists(String directoryName) {
        File directory = new File(directoryName);
        if (!directory.exists())
            directory.mkdir();
    }

    private Map<String, ConfigurationSection> groups(String directoryName, String... defaultGroupNames) {
        Map<String, ConfigurationSection> configs = new HashMap<String, ConfigurationSection>();
        File[] groupConfigFiles = groupConfigFiles(directoryName, defaultGroupNames);
        for (File file : groupConfigFiles)
            configs.put(groupNameOfFile(file), YamlConfiguration.loadConfiguration(file));
        return configs;
    }

    private String groupNameOfFile(File file) {
        String fileName = file.getName();
        return fileName.substring(0, fileName.length() - 4);
    }

    private File[] groupConfigFiles(String directoryName, String... defaultGroupNames) {
        String groupsDirectoryName = directoryName + "/ab_groups";
        File groupsDirectory = new File(groupsDirectoryName);
        if (!groupsDirectory.exists()) {
            groupsDirectory.mkdir();
            saveDefaultGroups(groupsDirectoryName, defaultGroupNames);
        }
        return groupsDirectory.listFiles((dir, name) -> name.toLowerCase().endsWith(".yml"));
    }

    private void saveDefaultGroups(String groupsDirectoryName, String... defaultGroupNames) {
        for (String group : defaultGroupNames)
            ABPromoter.getInstance().saveResource(groupsDirectoryName + "/" + group + ".yml", false);
    }

    private Map<String, Integer> weights(String directoryName) {
        String fileName = directoryName + '/' + "ab_group_weights.yml";
        File weightsFile = new File(fileName);
        if (!weightsFile.exists()) {
            ABPromoter.getInstance().saveResource(fileName, false);
            ABPromoter.getInstance().getLogger().info("Created new " + directoryName + '/' + "ab_group_weights.yml");
        }
        YamlConfiguration config = YamlConfiguration.loadConfiguration(weightsFile);
        return loadWeights(config);
    }

    private Map<String, Integer> loadWeights(ConfigurationSection weightsSection) {
        Map<String, Integer> weights = new HashMap<String, Integer>();
        Collection<String> keys = weightsSection.getKeys(false);
        keys.forEach(key -> loadWeight(weightsSection, key, weights));
        return weights;
    }

    private void loadWeight(ConfigurationSection weightsSection, String key, Map<String, Integer> weights) {
        int weight = weightsSection.getInt(key);
        if (weightsSection.getInt(key) > 0)
            weights.put(key, weight);
        else
            ABPromoter.getInstance().getLogger()
                    .warning("ab_group " + key + " has invalid weight (must be integer > 0). Ignoring.");
    }

}
