package manners.cowardly.abpromoter.utilities;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import manners.cowardly.abpromoter.ABPromoter;
import manners.cowardly.abpromoter.database.GetABGroupsWithMembers;
import manners.cowardly.abpromoter.database.SaveABGroup;

public class ABGroupsFilesLoader {

    private Map<String, ConfigurationSection> groupConfigs;
    private Map<String, Integer> groupWeights;
    private Set<String> groupsWithMembers;
    private boolean successful = false;

    public ABGroupsFilesLoader(GetABGroupsWithMembers getDbABGroups, SaveABGroup saveGroupDb, String directoryName,
            String abGroupsDbTableName, String usersTableColumnName, String... defaultGroupNames) {
        makeDirectoryIfNotExists(directoryName);
        groupWeights = weights(directoryName);
        groupConfigs = groups(directoryName, defaultGroupNames);
        groupsWithMembers = getDbABGroups.abGroupsWithMembers(abGroupsDbTableName, usersTableColumnName);
        checkWeightsForInvalidGroups(groupConfigs, groupWeights, directoryName);
        successful = checkForMissingGroups(groupsWithMembers, groupConfigs, directoryName);
        saveNewAbGroups(saveGroupDb, groupsWithMembers, groupConfigs.keySet());
    }

    public Map<String, ConfigurationSection> getGroupConfigs() {
        return groupConfigs;
    }

    public Map<String, Integer> getWeights() {
        return groupWeights;
    }

    public Set<String> groupsWithMembers() {
        return groupsWithMembers;
    }

    public boolean successful() {
        return successful;
    }

    private void saveNewAbGroups(SaveABGroup saveGroupDb, Set<String> groupsWithMembers, Set<String> groupNames) {
        groupNames.forEach(name -> insertGroupInDbIfNotPresent(saveGroupDb, groupsWithMembers, name));
    }

    private void insertGroupInDbIfNotPresent(SaveABGroup saveGroupDb, Set<String> groupsWithMembers, String name) {
        if (!groupsWithMembers.contains(name)) {
            saveGroupDb.saveGroup(name, "announcer_ab_groups");
        }
    }

    private boolean checkForMissingGroups(Collection<String> groupsWithMembers,
            Map<String, ConfigurationSection> groupConfigs, String directoryName) {
        boolean success = true;
        for (String dbGroupName : groupsWithMembers) {
            if (!groupConfigs.containsKey(dbGroupName)) {
                success = false;
                ABPromoter.getInstance().getLogger().severe("There are players who are a member of the " + directoryName
                        + " ab_group '" + dbGroupName
                        + "', however, there is no config file for this group. As a result, this load will be regarded as unsuccessful.");
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
        File directory = new File(ABPromoter.getInstance().getDataFolder() + "/" + directoryName);
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
        String groupsDirectoryName = ABPromoter.getInstance().getDataFolder() + "/" + directoryName + "/ab_groups";
        File groupsDirectory = new File(groupsDirectoryName);
        if (!groupsDirectory.exists()) {
            groupsDirectory.mkdir();
            saveDefaultGroups(groupsDirectoryName, defaultGroupNames);
        }
        return groupsDirectory.listFiles((dir, name) -> name.toLowerCase().endsWith(".yml"));
    }

    private void saveDefaultGroups(String groupsDirectoryName, String... defaultGroupNames) {
        for (String group : defaultGroupNames) {
            System.out.println(groupsDirectoryName + "/" + group + ".yml");
            ABPromoter.getInstance().saveResource(groupsDirectoryName + "/" + group + ".yml", false);
        }
    }

    private Map<String, Integer> weights(String directoryName) {
        String fileName = ABPromoter.getInstance().getDataFolder() + "/" + directoryName + '/' + "ab_group_weights.yml";
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
