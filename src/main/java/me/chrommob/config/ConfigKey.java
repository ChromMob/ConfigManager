package me.chrommob.config;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConfigKey {
    private String key;
    private Object value;
    private final Map<String, ConfigKey> children = new HashMap<>();

    /**
     * @param key The key of the entry in config
     * @param value The value of the entry. Can be value such as int, double or can be another list of ConfigKeys.
     */
    public ConfigKey(String key, Object value) {
        this.key = key;
        if (value instanceof List<?>) {
            List<?> list = (List<?>) value;
            this.value = null;
            if (!list.isEmpty()) {
                Object first = list.get(0);
                if (first instanceof ConfigKey) {
                    list.forEach(child -> {
                        ConfigKey configKey = (ConfigKey) child;
                        children.put(configKey.get(), configKey);
                    });
                }
            }
        } else {
            this.value = value;
        }
    }

    /**
     * This is useful if you have the same key for many things like checks in anti-cheat, so you can just clone them.
     * This is used inside the library of you have keys of same name too.
     * @param newName The key of the cloned ConfigKey
     * @return Cloned ConfigKey
     */
    public ConfigKey clone(String newName) {
        ConfigKey key = new ConfigKey(newName, value);
        children.forEach((name, child) -> key.addChild(child.clone(name)));
        return key;
    }

    /**
     * Mostly for internal use
     */
    public void addChild(ConfigKey child) {
        children.put(child.get(), child);
    }

    /**
     * Mostly for internal use
     */
    public Map<String, ConfigKey> getChildren() {
        return children;
    }

    /**
     * Get key if the ConfigKeys value is a List
     */
    public ConfigKey getKey(String key) {
        return children.get(key);
    }

    /**
     * Modifies the config value, the file gets modified too, not just the memory value.
     * @param value New value of the key
     */
    public void setValue(Object value) {
        if (value instanceof Map) {
            Map<String, Object> map = (Map<String, Object>) value;
            map.forEach((name, childValue) -> {
                ConfigKey child = children.get(name);
                if (child != null) {
                    child.setValue(childValue);
                }
            });
        } else {
            this.value = value;
        }
    }

    /**
     * @return The key of the ConfigKey
     */
    public String get() {
        return key;
    }

    /**
     * @return Raw Object value for later casting manually
     */
    public Object getAsObject() {
        return value;
    }

    /**
     * @return String representation of the Object or String itself if the value is String
     */
    public String getAsString() {
        if (value instanceof String) {
            return (String) value;
        }
        return value.toString();
    }

    /**
     * @return The value parsed as int.
     * @throws ClassCastException If Object cant be cast.
     */
    public int getAsInt() {
        if (value instanceof Integer) {
            return (int) value;
        }
        if (value instanceof String) {
            return Integer.parseInt((String) value);
        }
        throw new ClassCastException("Cannot cast " + value.getClass().getName() + " to int");
    }

    /**
     * @return The value parsed as double.
     * @throws ClassCastException If Object cant be cast.
     */
    public double getAsDouble() {
        if (value instanceof Double) {
            return (double) value;
        }
        if (value instanceof String) {
            return Double.parseDouble((String) value);
        }
        throw new ClassCastException("Cannot cast " + value.getClass().getName() + " to double");
    }

    /**
     * @return The value parsed as float.
     * @throws ClassCastException If Object cant be cast.
     */
    public float getAsFloat() {
        if (value instanceof Float) {
            return (float) value;
        }
        if (value instanceof String) {
            return Float.parseFloat((String) value);
        }
        throw new ClassCastException("Cannot cast " + value.getClass().getName() + " to float");
    }

    /**
     * @return Simple cast of the object to provided type.
     */
    public <T> T getAsType(Class<T> type) throws ClassCastException {
        return type.cast(value);
    }

    /**
     * @return The value parsed as boolean.
     * @throws ClassCastException If Object cant be cast.
     */
    public boolean getAsBoolean() {
        if (value instanceof Boolean) {
            return (boolean) value;
        }
        if (value instanceof String) {
            return Boolean.parseBoolean((String) value);
        }
        throw new ClassCastException("Cannot cast " + value.getClass().getName() + " to boolean");
    }
}
