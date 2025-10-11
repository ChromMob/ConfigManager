package me.chrommob.config.test;

public class Settings {
    private final boolean enabled;
    private final int value;
    private final String command;

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
