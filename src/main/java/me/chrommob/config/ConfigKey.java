package me.chrommob.config;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;

public class ConfigKey<T> {
    private final String key;
    private T value;
    private T defaultValue;
    /** False for keys found in the file that the code did not declare; they are kept as they are. */
    private boolean declared = true;
    private final Function<T, String> serializer;
    private final Function<String, T> deserializer;
    private final Field field;
    List<String> comments;
    private final Map<String, ConfigKey<?>> children = new LinkedHashMap<>();

    public ConfigKey(String key, List<ConfigKey<?>> value, List<String> comments) {
        this.serializer = null;
        this.deserializer = null;
        this.comments = comments;
        this.key = key;
        this.field = null;
        if (value.isEmpty()) {
            this.value = null;
            return;
        }
        Class<?> clazz = value.get(0).getClass();
        if (clazz != ConfigKey.class) {
            throw new ClassCastException("Cannot cast " + clazz.getName() + " to ConfigKey");
        }
        this.value = null;
        ConfigKey<?> first = value.get(0);
        if (first != null) {
            value.forEach(child -> {
                children.put(child.getKey(), child);
            });
        }
    }

    public ConfigKey(String key, List<ConfigKey<?>> value) {
        this(key, value, null);
    }

    /**
     * Constructs a new ConfigKey with the specified key and value.
     *
     * @param key   The key of the entry in the configuration.
     * @param value The value of the entry. It can be a primitive type (e.g., int,
     *              double) or a list of ConfigKeys.
     * @param field The field if using annotations.
     */
    protected ConfigKey(String key, T value, List<String> comments, Function<T, String> serializer, Function<String, T> deserializer, Field field) {
        if (value instanceof List && !((List<?>) value).isEmpty()) {
            List<?> list = (List<?>) value;
            Object first = ((List<?>) value).get(0);
            if (first instanceof ConfigKey) {
                this.serializer = null;
                this.deserializer = null;
                this.comments = comments;
                this.key = key;
                this.field = null;
                this.value = null;
                for (Object child : list) {
                    ConfigKey<?> configKey = (ConfigKey<?>) child;
                    this.children.put(configKey.getKey(), configKey);
                }
                return;
            }
        }
        if (value instanceof Boolean) {
            this.deserializer = (Function<String, T>) deserializeBoolean;
        } else if (value instanceof Long) {
            this.deserializer = (Function<String, T>) deserializeLong;
        } else if (value instanceof Integer) {
            this.deserializer = (Function<String, T>) deserializeInteger;
        } else if (value instanceof Float) {
            this.deserializer = (Function<String, T>) deserializeFloat;
        } else if (value instanceof Double) {
            this.deserializer = (Function<String, T>) deserializeDouble;
        } else {
            this.deserializer = deserializer;
        }
        this.serializer = serializer;
        this.comments = comments;
        this.field = field;
        this.key = key;
        this.value = value;
        this.defaultValue = value;
    }

    public ConfigKey(String key, T value, List<String> comments, Function<T, String> serializer, Function<String, T> deserializer) {
        this(key, value, comments, serializer, deserializer, null);
    }

    public ConfigKey(String key, T value, Function<T, String> serializer, Function<String, T> deserializer) {
        this(key, value, null, serializer, deserializer, null);
    }

    public ConfigKey(String key, T value, List<String> comments) {
        this(key, value, comments, null, null, null);
    }

    public ConfigKey(String key, T value) {
        this(key, value, null);
    }

    public ConfigKey(String key, Integer value, List<String> comments) {
        this(key, ((T) value), comments, null, (Function<String, T>) deserializeInteger, null);
    }

    private static final Function<String, Integer> deserializeInteger = s -> Integer.parseInt(deleteIf(s, c -> !Character.isDigit(c)));

    /**
     * Constructs a new ConfigKey with the specified key, value and comments.
     *
     * @param key      The key of the entry in the configuration.
     * @param value    The value of the entry. It can be a primitive type (e.g.,
     *                 int,
     *                 double) or a list of ConfigKeys.
     * @param comments The comments of the entry in the configuration.
     */
    public ConfigKey(String key, String value, List<String> comments) {
        this(key, (T) value, comments, null, null, null);
    }

    /**
     * Constructs a new ConfigKey with the specified key and value.
     *
     * @param key   The key of the entry in the configuration.
     * @param value The value of the entry. It can be a primitive type (e.g., int,
     *              double) or a list of ConfigKeys.
     */
    public ConfigKey(String key, String value) {
        this(key, value, null);
    }

    public ConfigKey(String key, Double value, List<String> comments) {
        this(key, ((T) value), comments, null, (Function<String, T>) deserializeDouble, null);
    }

    private static final Function<String, Double> deserializeDouble = s -> Double.parseDouble(removeDoubleDots(filterOut(s)));

    public ConfigKey(String key, Double value) {
        this(key, value, null);
    }

    public ConfigKey(String key, Long value, List<String> comments) {
        this(key, ((T) value), comments, null,  (Function<String, T>) deserializeLong, null);
    }

    private static final Function<String, Long> deserializeLong  = s -> Long.parseLong(deleteIf(s, c -> !Character.isDigit(c)));

    public ConfigKey(String key, Long value) {
        this(key, value, null);
    }

    public ConfigKey(String key, Float value, List<String> comments) {
        this(key, ((T) value), comments, null, (Function<String, T>) deserializeFloat, null);
    }

    private static final Function<String, Float> deserializeFloat = s -> Float.parseFloat(removeDoubleDots(filterOut(s)));

    private static String removeDoubleDots(String toRemove) {
        boolean hadDot = false;
        StringBuilder removed = new StringBuilder();
        for (char c : toRemove.toCharArray()) {
            if (Character.isDigit(c)) {
                removed.append(c);
            }
            if (c == '.' && !hadDot) {
                hadDot = true;
                removed.append('.');
            }
        }
        return removed.toString();
    }

    private static String filterOut(String numberWithChars) {
        StringBuilder filtered = new StringBuilder();
        boolean decimalDotUsed = false;
        boolean decimalCommaUsed = false;
        for (int i = 0; i < numberWithChars.length(); i++) {
            char ch = numberWithChars.charAt(i);
            if (Character.isDigit(ch)) {
                filtered.append(ch);
            }
            if (ch == '.') {
                decimalDotUsed = true;
                filtered.append(ch);
            }
            if (ch  == ',') {
                decimalCommaUsed = true;
                filtered.append(ch);
            }
        }
        if (decimalDotUsed && decimalCommaUsed) {
            return deleteIf(filtered, c -> c == ',');
        }
        if (decimalCommaUsed) {
            return filtered.toString().replaceAll(",", ".");
        }
        return filtered.toString();
    }

    private static String deleteIf(StringBuilder filtered, Predicate<Character> predicate) {
        return deleteIf(filtered.toString(), predicate);
    }

    private static String deleteIf(String old, Predicate<Character> condition) {
        StringBuilder res = new StringBuilder();
        for (int i = 0; i < old.length(); i++) {
            char ch = old.charAt(i);
            if (condition.test(ch)) continue;
            res.append(ch);
        }
        return res.toString();
    }

    public ConfigKey(String key, Float value) {
        this(key, value, null);
    }

    public ConfigKey(String key, Boolean value, List<String> comments) {
        this(key, (T) value, comments, null, (Function<String, T>) deserializeBoolean, null);
    }

    private static final Function<String, Boolean> deserializeBoolean = o -> {
        int trueDistance = getDistance("true", o);
        int falseDistance = getDistance("false", o);
        return trueDistance >= falseDistance;
    };

    private static int getDistance(String expected, String real) {
        int number = 0;
        int location = 0;
        for (int i = 0; i < real.length(); i++) {
            if (expected.length() - 1 == location) {
                return number;
            }
            if (real.length() - 1 == location) {
                return number;
            }
            char expectedChar = expected.charAt(location);
            char realChar = real.charAt(location);
            if (expectedChar == realChar) {
                number += 1;
            }
            location += 1;
        }
        return number;
    }

    public ConfigKey(String key, Boolean value) {
        this(key, value, null);
    }

    public ConfigKey(String key, List<T> value, List<String> comments, Function<T, String> serializer, Function<String, T> deserializer, Field field) {
        this(key, ((T) value), comments, serializer, deserializer, field);
    }

    /**
     * Creates a clone of the ConfigKey with a new name.
     * <p>
     * This method is useful when you have multiple ConfigKeys with the same key and
     * you want to create a copy of the ConfigKey with a different name.
     *
     * @param newName The new name for the cloned ConfigKey.
     * @return A new ConfigKey instance with the specified name and the same value
     *         and children as the original ConfigKey.
     */
    public ConfigKey<T> clone(String newName) {
        ConfigKey<T> key = new ConfigKey<>(newName, (T) value, comments, serializer, deserializer, field);
        children.forEach((name, child) -> key.addChild(child.clone(name)));
        return key;
    }

    /**
     * Adds a child ConfigKey to the current ConfigKey.
     * <p>
     * This method is mostly for internal use and is used to add a child ConfigKey
     * to the current ConfigKey's collection of children.
     *
     * @param child The child ConfigKey to be added.
     */
    public void addChild(ConfigKey<?> child) {
        children.put(child.getKey(), child);
    }

    /**
     * Removes a child, for example one a newer version no longer uses, so the next save drops it
     * from the file.
     *
     * @param key The name of the child to remove.
     * @return the removed child, or null if there was none.
     */
    public ConfigKey<?> removeChild(String key) {
        return children.remove(key);
    }

    /**
     * Retrieves the children of the current ConfigKey.
     * <p>
     * This method returns a map of the child ConfigKey instances associated with
     * the current ConfigKey. The keys of the map represent the names of the child
     * ConfigKeys, and the values represent the corresponding child ConfigKey
     * objects.
     *
     * @return A map of the child ConfigKey instances associated with the current
     * ConfigKey.
     */
    public Map<String, ConfigKey<?>> getChildren() {
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
    public <V> ConfigKey<V> getKey(String key) {
        ConfigKey<?> child = children.get(key);
        if (child == null) {
            return null;
        }
        if (child.getClass() != ConfigKey.class) {
            throw new ClassCastException("Cannot cast " + child.getClass().getName() + " to ConfigKey");
        }
        return (ConfigKey<V>) child;
    }

    /**
     * Loads the value read from the file. Keys inside a section that the code did not declare,
     * such as entries the user names themselves, are kept as they are, so saving never loses
     * them. An empty value, or a key missing from its section, goes back to its default.
     *
     * @throws IllegalArgumentException naming every key whose value could not be read; the
     *                                  other keys are still loaded.
     */
    public void loadValue(Object value) {
        List<String> problems = new ArrayList<>();
        load(value, key, problems);
        if (!problems.isEmpty()) {
            throw new IllegalArgumentException(String.join("; ", problems));
        }
    }

    void load(Object value, String path, List<String> problems) {
        if (value == null) {
            reset();
            return;
        }
        if (value instanceof Map) {
            Set<String> names = new HashSet<>();
            for (Map.Entry<?, ?> entry : ((Map<?, ?>) value).entrySet()) {
                String name = String.valueOf(entry.getKey());
                names.add(name);
                ConfigKey<?> child = children.get(name);
                if (child == null) {
                    children.put(name, fromFile(name, entry.getValue()));
                } else {
                    child.load(entry.getValue(), path + "." + name, problems);
                }
            }
            children.values().removeIf(child -> !child.declared && !names.contains(child.key));
            for (ConfigKey<?> child : children.values()) {
                if (!names.contains(child.key)) {
                    child.reset();
                }
            }
        }
        try {
            setValue(deserializer == null ? (T) value : deserializer.apply(value.toString()));
        } catch (RuntimeException e) {
            problems.add(path + " (" + value + " is not a valid value)");
        }
    }

    /** Puts the value, and those of the declared children, back to the default; drops undeclared children. */
    void reset() {
        setValue(defaultValue);
        children.values().removeIf(child -> !child.declared);
        children.values().forEach(ConfigKey::reset);
    }

    boolean isDeclared() {
        return declared;
    }

    /** A key found in the file that the code did not declare, kept with the value it has there. */
    static ConfigKey<Object> fromFile(String name, Object value) {
        ConfigKey<Object> key = new ConfigKey<>(name, value, null, null, null, null);
        key.declared = false;
        return key;
    }

        /**
     * Modifies the value of the ConfigKey.
     * <p>
     * This method is used to update the value of the ConfigKey. It updates the value of the current ConfigKey.
     *
     * @param value The new value of the ConfigKey.
     */
    public void setValue(T value) {
        this.value = value;
        if (field != null) {
            try {
                field.set(null, value);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Retrieves the key of the ConfigKey.
     * <p>
     * This method returns the key associated with the current ConfigKey instance.
     * The key represents the identifier or name of the ConfigKey in the
     * configuration.
     *
     * @return The key of the ConfigKey.
     */
    public String getKey() {
        return key;
    }

    /**
     * Retrieves the value of the ConfigKey.
     * <p>
     * This method returns the value associated with the current ConfigKey instance.
     *
     * @return The value of the ConfigKey.
     */
    public T getValue() {
        return value;
    }

    /**
     * @return String representation of the Object or String itself if the value is
     *         String
     */
    public String getValueAsString() {
        if (serializer == null) {
            if (value instanceof String) {
                return (String) value;
            }
            return value.toString();
        }
        return serializer.apply(value);
    }

    public Object getSerializedIfNeeded() {
        if  (serializer == null) {
            return value;
        }
        return serializer.apply(value);
    }

    public List<String> getComments() {
        return comments;
    }

    @Override
    public String toString() {
        return key + "=" + getValueAsString();
    }
}
