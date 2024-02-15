package me.chrommob.config;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ConfigWrapper {
    private final String name;
    private final Map<String, ConfigKey> keys = new LinkedHashMap<>();

    /**
     * Feel free to extend this class and add helper functions the API expects it.
     * Create new config with name and predefined keys
     * 
     * @param name file name of config (the library will create the file for you
     *             with that name)
     * @param keys list of config keys
     */
    public ConfigWrapper(String name, List<ConfigKey> keys) {
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
    public void addKey(ConfigKey key) {
        if (this.keys.containsKey(key.get())) {
            int numberOfSameKeys = 0;
            for (String keyName : this.keys.keySet()) {
                if (keyName.equals(key.get())) {
                    numberOfSameKeys++;
                }
            }
            String keyName = key.get() + "_" + numberOfSameKeys;
            this.keys.put(keyName, key.clone(keyName));
        } else
            this.keys.put(key.get(), key);
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
    public ConfigKey getKey(String key) {
        return keys.get(key);
    }

    /**
     * This should not be used by user.
     * 
     * @param loadedConfig Sets the raw yaml representation of the config to use.
     */
    public void setConfig(Map<String, Object> loadedConfig) {
        loadedConfig.forEach((name, value) -> {
            ConfigKey key = keys.get(name);
            if (key != null) {
                key.setValue(value);
            } else {
                addKey(new ConfigKey(name, value));
            }
        });
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

    private Map<String, Object> read(ConfigKey key) {
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
            value = key.getAsObject();
        }
        config.put(key.get(), value);
        return config;
    }
}
