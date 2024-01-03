package me.chrommob.config.test;

import me.chrommob.config.ConfigKey;
import me.chrommob.config.ConfigWrapper;
import java.util.List;

public class CustomWrapper extends ConfigWrapper {
    public CustomWrapper(String name) {
        super(name);
    }

    public CustomWrapper(String name, List<ConfigKey> keys) {
        super(name, keys);
    }

    public Settings getSettings(String check) {
        ConfigKey data = getKey("checks").getKey(check);
        boolean enabled = data.getKey("enabled").getAsBoolean();
        int value = data.getKey("value").getAsInt();
        String command = data.getKey("command").getAsString();
        return new Settings(enabled, value, command);
    }
}

class Settings {
    private boolean enabled;
    private int value;
    private String command;

    public Settings(boolean enabled, int value, String command) {
        this.enabled = enabled;
        this.value = value;
        this.command = command;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public int getValue() {
        return value;
    }

    public String getCommand() {
        return command;
    }
}