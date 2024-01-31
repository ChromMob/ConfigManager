package me.chrommob.config;

import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.Map;

public class ConfigManager {
    private final File configPath;
    private final Yaml yaml;
    private final Map<String, Class<?>> configClasses = new HashMap<>();
    private final Map<String, Object> configWrappers = new HashMap<>();

    /**
     * Creates new instance of ConfigManager
     * 
     * @param configPath Path to store all the configs created by this
     *                   ConfigManager.
     */
    public ConfigManager(File configPath) {
        this.configPath = configPath;
        if (!configPath.exists()) {
            configPath.mkdirs();
        }
        DumperOptions dumperOptions = new DumperOptions();
        dumperOptions.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        dumperOptions.setIndent(2);
        dumperOptions.setPrettyFlow(true);
        yaml = new Yaml(dumperOptions);
    }

    /**
     * Add your config to ConfigManager
     * 
     * @param configWrapper The config you want to add.
     */
    public void addConfig(ConfigWrapper configWrapper) {
        configWrappers.put(configWrapper.getName(), configWrapper);
        configClasses.put(configWrapper.getName(), configWrapper.getClass());
        loadConfig(configWrapper.getName());
        saveConfig(configWrapper.getName());
    }

    /**
     * @param name Name of the config you want to get
     * @return The actual form of ConfigWraper even if you extend it.
     */
    public <T extends ConfigWrapper> T getConfigWrapper(String name) {
        Object configWrapper = configWrappers.get(name);
        if (configWrapper != null) {
            return (T) configWrapper;
        } else {
            return null;
        }
    }

    private void loadConfig(String name) {
        File file = new File(configPath, name + ".yml");
        if (!file.exists()) {
            return;
        }
        try (FileReader reader = new FileReader(file)) {
            Map<String, Object> loadedConfig = yaml.load(reader);
            if (loadedConfig == null) {
                return;
            }
            ConfigWrapper configWrapper = getConfigWrapper(name);
            if (configWrapper == null) {
                return;
            }
            configWrapper.setConfig(loadedConfig);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void saveConfig(String name) {
        File file = new File(configPath, name + ".yml");
        ConfigWrapper configWrapper = getConfigWrapper(name);
        if (configWrapper == null) {
            return;
        }
        Map<String, Object> config = configWrapper.getConfig();
        try {
            if (!file.exists()) {
                file.createNewFile();
            }
            FileWriter writer = new FileWriter(file);
            String[] lines = yaml.dump(config).split("\n");
            Map<Integer, ConfigKey> keys = new HashMap<>();
            for (String line : lines) {
                int spaces = 0;
                for (int i = 0; i < line.length(); i++) {
                    if (line.charAt(i) == ' ') {
                        spaces++;
                    } else {
                        break;
                    }
                }
                ConfigKey key = null;
                if (spaces == 0) {
                    key = configWrapper.getKey(line.split(":")[0]);
                }
                if (spaces > 0) {
                    ConfigKey parent = keys.get(spaces - 2);
                    if (parent != null) {
                        key = parent.getKey(line.split(":")[0].trim());
                    }
                }
                if (key != null) {
                    if (key.getComments() != null) {
                        for (String comment : key.getComments()) {
                            StringBuilder builder = new StringBuilder();
                            for (int i = 0; i < spaces; i++) {
                                builder.append(" ");
                            }
                            writer.write(builder.toString() + "# " + comment + "\n");
                        }
                    }
                    keys.put(spaces, key);
                }
                writer.write(line + "\n");
            }
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Reloads config of specified name.
     * 
     * @param name Name of the config you want to reload.
     */
    public void reloadConfig(String name) {
        loadConfig(name);
    }
}
