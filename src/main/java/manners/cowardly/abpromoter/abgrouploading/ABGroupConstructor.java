package manners.cowardly.abpromoter.abgrouploading;

import org.bukkit.configuration.ConfigurationSection;

@FunctionalInterface
public interface ABGroupConstructor<T extends ABGroup> {
    public T constructor(ConfigurationSection config, String name);
}
