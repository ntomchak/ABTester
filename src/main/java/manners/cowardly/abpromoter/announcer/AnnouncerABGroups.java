package manners.cowardly.abpromoter.announcer;

import java.util.HashMap;
import java.util.Map;

import manners.cowardly.abpromoter.abgrouploading.ABGroupsLoadInfo;
import manners.cowardly.abpromoter.abgrouploading.ABGroupsReloadInfo;
import manners.cowardly.abpromoter.announcer.abgroup.AnnouncerABGroup;
import manners.cowardly.abpromoter.database.GetABGroupsWithMembers;
import manners.cowardly.abpromoter.database.SaveABGroup;
import manners.cowardly.abpromoter.utilities.WeightedProbabilities;

public class AnnouncerABGroups {
    private WeightedProbabilities<AnnouncerABGroup> groupsProbabilities = new WeightedProbabilities<AnnouncerABGroup>();
    private Map<String, AnnouncerABGroup> groupNames = new HashMap<String, AnnouncerABGroup>();

    public AnnouncerABGroups(GetABGroupsWithMembers getDbABGroups, SaveABGroup saveGroupDb) {
        load(getDbABGroups, saveGroupDb);
    }

    public ABGroupsReloadInfo<AnnouncerABGroup> reloader(GetABGroupsWithMembers getDbABGroups,
            SaveABGroup saveGroupDb) {
        return new ABGroupsReloadInfo<AnnouncerABGroup>(AnnouncerABGroup::new, getDbABGroups, saveGroupDb,
                groupsProbabilities, groupNames, "announcer", "announcer_ab_groups", "announcer_ab_group", "announcer1",
                "announcer2");
    }

    private void load(GetABGroupsWithMembers getDbABGroups, SaveABGroup saveGroupDb) {

        ABGroupsLoadInfo<AnnouncerABGroup> load = new ABGroupsLoadInfo<AnnouncerABGroup>(AnnouncerABGroup::new,
                getDbABGroups, saveGroupDb, "announcer", "announcer_ab_groups", "announcer_ab_group", "announcer1",
                "announcer2");

        groupsProbabilities = load.groupsProbabilities();
        groupNames = load.groupNames();
    }

    public AnnouncerABGroup selectRandomGroup() {
        return groupsProbabilities.sample();
    }

    public AnnouncerABGroup getGroup(String name) {
        return groupNames.get(name);
    }
}
