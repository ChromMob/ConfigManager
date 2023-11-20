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

    public void addConfig(ConfigWrapper configWrapper) {
        configWrappers.put(configWrapper.getName(), configWrapper);
        configClasses.put(configWrapper.getName(), configWrapper.getClass());
        loadConfig(configWrapper.getName());
        saveConfig(configWrapper.getName());
    }

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
        FileReader reader;
        try {
            reader = new FileReader(file);
        } catch (Exception e) {
            return;
        }
        if (!file.exists()) {
            return;
        }
        Map<String, Object> loadedConfig = yaml.load(reader);
        if (loadedConfig == null) {
            return;
        }
        ConfigWrapper configWrapper = getConfigWrapper(name);
        if (configWrapper == null) {
            return;
        }
        configWrapper.setConfig(loadedConfig);
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
            yaml.dump(config, writer);
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void reloadConfig(String name) {
        loadConfig(name);
    }
}
