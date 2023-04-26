package manners.cowardly.abpromoter.menus;

import java.util.HashMap;
import java.util.Map;

import manners.cowardly.abpromoter.abgrouploading.ABGroupsLoadInfo;
import manners.cowardly.abpromoter.abgrouploading.ABGroupsReloadInfo;
import manners.cowardly.abpromoter.database.GetABGroupsWithMembers;
import manners.cowardly.abpromoter.database.SaveABGroup;
import manners.cowardly.abpromoter.menus.menuabgroups.MenuABGroup;
import manners.cowardly.abpromoter.utilities.DiscreteProbabilityDistribution;

public class MenuABGroups {
    private DiscreteProbabilityDistribution<MenuABGroup> groupsProbabilities = new DiscreteProbabilityDistribution<MenuABGroup>();
    private Map<String, MenuABGroup> groupNames = new HashMap<String, MenuABGroup>();

    public MenuABGroups(GetABGroupsWithMembers getDbABGroups, SaveABGroup saveGroupDb) {
        ABGroupsLoadInfo<MenuABGroup> load = new ABGroupsLoadInfo<MenuABGroup>(MenuABGroup::new, getDbABGroups,
                saveGroupDb, "menus", "menu_ab_groups", "menu_ab_group", "menus1", "menus2");
        groupsProbabilities = load.groupsProbabilities();
        groupNames = load.groupNames();
    }

    public ABGroupsReloadInfo<MenuABGroup> reloader(GetABGroupsWithMembers getDbABGroups, SaveABGroup saveGroupDb) {
        return new ABGroupsReloadInfo<MenuABGroup>(MenuABGroup::new, getDbABGroups, saveGroupDb, groupsProbabilities,
                groupNames, "menus", "menu_ab_groups", "menu_ab_group", "menus1", "menus2");
    }

    public MenuABGroup selectRandomGroup() {
        return groupsProbabilities.sample();
    }

    public MenuABGroup getGroup(String name) {
        return groupNames.get(name);
    }
}
