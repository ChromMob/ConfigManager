package me.chrommob.config;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ConfigWrapper {
    private final String name;
    private final Map<String, ConfigKey> keys = new LinkedHashMap<>();

    public ConfigWrapper(String name, List<ConfigKey> keys) {
        this.name = name;
        keys.forEach(key -> {
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
        });
    }

    public String getName() {
        return name;
    }

    public ConfigKey getKey(String key) {
        return keys.get(key);
    }

    public void setConfig(Map<String, Object> loadedConfig) {
            loadedConfig.forEach((name, value) -> {
            ConfigKey key = keys.get(name);
            if (key != null) {
                key.setValue(value);
            }
        });
    }

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
                    childConfig.forEach((childName, childValue) -> ((Map<String, Object>) value).put(childName, childValue));
                }
            });
        } else {
            value = key.getAsObject();
        }
        config.put(key.get(), value);
        return config;
    }
}
