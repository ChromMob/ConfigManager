package me.chrommob.config;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConfigKey {
    private String key;
    private Object value;
    private final Map<String, ConfigKey> children = new HashMap<>();

    /**
     * Constructs a new ConfigKey with the specified key and value.
     *
     * @param key   The key of the entry in the configuration.
     * @param value The value of the entry. It can be a primitive type (e.g., int,
     *              double) or a list of ConfigKeys.
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
     * Creates a clone of the ConfigKey with a new name.
     *
     * This method is useful when you have multiple ConfigKeys with the same key and
     * you want to create a copy of the ConfigKey with a different name.
     *
     * @param newName The new name for the cloned ConfigKey.
     * @return A new ConfigKey instance with the specified name and the same value
     *         and children as the original ConfigKey.
     */
    public ConfigKey clone(String newName) {
        ConfigKey key = new ConfigKey(newName, value);
        children.forEach((name, child) -> key.addChild(child.clone(name)));
        return key;
    }

    /**
     * Adds a child ConfigKey to the current ConfigKey.
     *
     * This method is mostly for internal use and is used to add a child ConfigKey
     * to the current ConfigKey's collection of children.
     *
     * @param child The child ConfigKey to be added.
     */
    public void addChild(ConfigKey child) {
        children.put(child.get(), child);
    }

    /**
     * Retrieves the children of the current ConfigKey.
     *
     * This method returns a map of the child ConfigKey instances associated with
     * the current ConfigKey. The keys of the map represent the names of the child
     * ConfigKeys, and the values represent the corresponding child ConfigKey
     * objects.
     *
     * @return A map of the child ConfigKey instances associated with the current
     *         ConfigKey.
     */
    public Map<String, ConfigKey> getChildren() {
        return children;
    }

    /**
     * Retrieves the child ConfigKey with the specified key.
     *
     * This method is used to retrieve a specific child ConfigKey from the current
     * ConfigKey's collection of children. It is particularly useful when the value
     * of the current ConfigKey is a list of ConfigKeys, and you want to access a
     * specific ConfigKey within that list.
     *
     * @param key The key of the child ConfigKey to retrieve.
     * @return The child ConfigKey with the specified key, or null if no such child
     *         ConfigKey exists.
     */
    public ConfigKey getKey(String key) {
        return children.get(key);
    }

    /**
     * Modifies the value of the ConfigKey.
     *
     * This method is used to update the value of the ConfigKey. If the new value is
     * a map, it recursively updates the values of the child ConfigKeys as well. If
     * the new value is not a map, it updates the value of the current ConfigKey.
     *
     * @param value The new value of the ConfigKey.
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
     * Retrieves the key of the ConfigKey.
     *
     * This method returns the key associated with the current ConfigKey instance.
     * The key represents the identifier or name of the ConfigKey in the
     * configuration.
     *
     * @return The key of the ConfigKey.
     */
    public String get() {
        return key;
    }

    /**
     * Retrieves the raw object value of the ConfigKey.
     *
     * This method returns the raw object value associated with the current
     * ConfigKey instance. The raw object value can be of any type and may require
     * manual casting to the desired type.
     *
     * @return The raw object value of the ConfigKey.
     */
    public Object getAsObject() {
        return value;
    }

    /**
     * @return String representation of the Object or String itself if the value is
     *         String
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
