package me.chrommob.config;

import java.lang.reflect.Field;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;

public class ConfigKey<T> {
    private final String key;
    private Object value;
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
                children.put(child.get(), child);
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
        if (value instanceof List) {
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
                    this.children.put(configKey.get(), configKey);
                }
                return;
            }
        }
        this.serializer = serializer;
        this.deserializer = deserializer;
        this.comments = comments;
        this.field = field;
        this.key = key;
        if (serializer != null) {
            this.value = serializer.apply(value);
        } else {
            this.value = value;
        }
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

    private static final Function<String, Integer> deserializeInteger = (final String s) -> Integer.parseInt(deleteIf(s, c -> !Character.isDigit(c)));

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

    private static final Function<String, Double> deserializeDouble = (final String s) -> Double.parseDouble(removeDoubleDots(filterOut(s)));

    public ConfigKey(String key, Double value) {
        this(key, value, null);
    }

    public ConfigKey(String key, Long value, List<String> comments) {
        this(key, ((T) value), comments, null,  (Function<String, T>) deserializeLong, null);
    }

    private static final Function<String, Long> deserializeLong = (final String s) -> Long.parseLong(deleteIf(s, c -> !Character.isDigit(c)));

    public ConfigKey(String key, Long value) {
        this(key, value, null);
    }

    public ConfigKey(String key, Float value, List<String> comments) {
        this(key, ((T) value), comments, null, (Function<String, T>) deserializeFloat, null);
    }

    private static final Function<String, Float> deserializeFloat = (final String s) -> Float.parseFloat(removeDoubleDots(filterOut(s)));

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
        children.put(child.get(), child);
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
     * Modifies the value of the ConfigKey.
     * <p>
     * This method is used to update the value of the ConfigKey. It updates the value of the current ConfigKey.
     *
     * @param value The new value of the ConfigKey.
     */
    public void setValue(T value) {
        if (value instanceof Map) {
            Map<?, ?> rawCast = (Map<?, ?>) value;
            if (rawCast.isEmpty()) {
                return;
            }
            rawCast.entrySet().stream().findFirst().ifPresent(entry -> {
                if  (entry.getKey() instanceof String) {
                    Map<String, Object> casted = (Map<String, Object>) value;
                    for  (Map.Entry<String, Object> castedEntry : casted.entrySet()) {
                        ((ConfigKey<Object>) children.get(castedEntry.getKey())).setValue(castedEntry.getValue());
                    }
                }
            });

        }
        if (serializer == null) {
            this.value = value;
            return;
        }
        this.value = serializer.apply(value);
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
    public String get() {
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
        if (deserializer == null) {
            return (T) value;
        }
        return deserializer.apply(value.toString());
    }

    /**
     * @return String representation of the Object or String itself if the value is
     *         String
     */
    public String getValueAsString() {
        return value.toString();
    }

    public List<String> getComments() {
        return comments;
    }
}
