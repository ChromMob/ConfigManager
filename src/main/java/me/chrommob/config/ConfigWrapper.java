package me.chrommob.config;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ConfigWrapper {
    private final String name;
    private final Map<String, ConfigKey<?>> keys = new LinkedHashMap<>();

    /**
     * Feel free to extend this class and add helper functions the API expects it.
     * Create new config with name and predefined keys
     * 
     * @param name file name of config (the library will create the file for you
     *             with that name)
     * @param keys list of config keys
     */
    public ConfigWrapper(String name, List<ConfigKey<?>> keys) {
        this.name = name;
        keys.forEach(this::addKey);
    }

    /**
     * Create new config with name and no keys
     * You are expected to add all the keys to the config later using addKey
     * 
     * @param name file name of config (the library will create the file for you
     *             with that name)
     */
    public ConfigWrapper(String name) {
        this.name = name;
    }

    /**
     * Adds ConfigKey to the config.
     * You can have duplicate names - the library handles it for you by modifying
     * the name to name_number
     * 
     * @param key - ConfigKey that gets added to the config
     */
    public void addKey(ConfigKey<?> key) {
        if (this.keys.containsKey(key.getKey())) {
            int numberOfSameKeys = 0;
            for (String keyName : this.keys.keySet()) {
                if (keyName.equals(key.getKey())) {
                    numberOfSameKeys++;
                }
            }
            String keyName = key.getKey() + "_" + numberOfSameKeys;
            this.keys.put(keyName, key.clone(keyName));
        } else
            this.keys.put(key.getKey(), key);
    }

    /**
     * Removes a key, for example one a newer version no longer uses, so the next save drops it
     * from the file.
     *
     * @param key The name of the key to remove
     * @return the removed key, or null if there was none
     */
    public ConfigKey<?> removeKey(String key) {
        return keys.remove(key);
    }

    /**
     * @return name of the config
     */
    public String getName() {
        return name;
    }

    /**
     * @param key The name of the key you want to get
     * @return ConfigKey representation of it
     */
    public ConfigKey<?> getKey(String key) {
        return keys.get(key);
    }

    /**
     * This should not be used by user.
     * Keys the code did not declare are kept as they are in the file; declared keys missing
     * from it go back to their defaults.
     *
     * @param loadedConfig Sets the raw yaml representation of the config to use.
     * @return the keys whose values could not be read (they keep their previous values).
     */
    protected List<String> setConfig(Map<?, ?> loadedConfig) {
        List<String> problems = new ArrayList<>();
        Set<String> names = new HashSet<>();
        for (Map.Entry<?, ?> entry : loadedConfig.entrySet()) {
            String name = String.valueOf(entry.getKey());
            names.add(name);
            ConfigKey<?> key = keys.get(name);
            if (key == null) {
                keys.put(name, ConfigKey.fromFile(name, entry.getValue()));
            } else {
                key.load(entry.getValue(), name, problems);
            }
        }
        keys.values().removeIf(key -> !key.isDeclared() && !names.contains(key.getKey()));
        for (ConfigKey<?> key : keys.values()) {
            if (!names.contains(key.getKey())) {
                key.reset();
            }
        }
        return problems;
    }

    /**
     * This should not be used by user.
     * 
     * @return The raw representation of the yaml file
     */
    public Map<String, Object> getConfig() {
        Map<String, Object> config = new LinkedHashMap<>();
        keys.forEach((name, key) -> config.putAll(read(key)));
        return config;
    }

    private Map<String, Object> read(ConfigKey<?> key) {
        Map<String, Object> config = new LinkedHashMap<>();
        Object value;
        if (!key.getChildren().isEmpty()) {
            value = new LinkedHashMap<>();
            key.getChildren().forEach((name, child) -> {
                Map<String, Object> childConfig = read(child);
                if (!childConfig.isEmpty()) {
                    childConfig.forEach(
                            (childName, childValue) -> ((Map<String, Object>) value).put(childName, childValue));
                }
            });
        } else {
            value = key.getSerializedIfNeeded();
        }
        config.put(key.getKey(), value);
        return config;
    }
}
