package me.chrommob.config.test;

import me.chrommob.config.ConfigKey;
import me.chrommob.config.ConfigWrapper;
import java.util.List;

public class CustomWrapper extends ConfigWrapper {
    public CustomWrapper(String name) {
        super(name);
    }

    public CustomWrapper(String name, List<ConfigKey<?>> keys) {
        super(name, keys);
    }

    public Settings getSettings(String check) {
        ConfigKey<?> data = getKey("checks").getKey(check);
        boolean enabled = (boolean) data.getKey("enabled").getValue();
        int value = (int) data.getKey("value").getValue();
        String command = data.getKey("command").getValueAsString();
        return new Settings(enabled, value, command);
    }
}

