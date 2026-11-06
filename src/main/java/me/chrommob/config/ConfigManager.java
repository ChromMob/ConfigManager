package me.chrommob.config;

import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public class ConfigManager {
    private final File configPath;
    private final Logger logger;
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
        this(configPath, Logger.getLogger("ConfigManager"));
    }

    /**
     * Creates new instance of ConfigManager
     *
     * @param configPath Path to store all the configs created by this
     *                   ConfigManager.
     * @param logger     Where problems reading or writing the configs are reported.
     */
    public ConfigManager(File configPath, Logger logger) {
        this.configPath = configPath;
        this.logger = logger;
        if (!configPath.exists()) {
            configPath.mkdirs();
        }
        DumperOptions dumperOptions = new DumperOptions();
        dumperOptions.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        dumperOptions.setIndent(2);
        dumperOptions.setSplitLines(false);
        yaml = new Yaml(dumperOptions);
    }

    /**
     * Add your config to ConfigManager
     * 
     * @param configWrapper The config you want to add.
     * @return false when its file could not be read (see {@link #loadConfig}); it is then left
     *         as it is instead of being saved.
     */
    public boolean addConfig(ConfigWrapper configWrapper) {
        configWrappers.put(configWrapper.getName(), configWrapper);
        configClasses.put(configWrapper.getName(), configWrapper.getClass());
        if (!loadConfig(configWrapper.getName())) {
            return false;
        }
        saveConfig(configWrapper.getName());
        return true;
    }

    /**
     * @param configClass is the class of the config you want to get that uses annotations.
     * @return false when its file could not be read; see {@link #addConfig}.
     */
    public boolean addAnnotatedConfig(Class<?> configClass) {
        String name = configClass.getAnnotation(ConfigNameAnnotation.class).value();
        List<ConfigKey<?>> keys = new ArrayList<>();
        for (Field field : configClass.getDeclaredFields()) {
            if (!Modifier.isStatic(field.getModifiers()) || !Modifier.isPublic(field.getModifiers())) {
                continue;
            }
            Object value = null;
            try {
                value = field.get(null);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
            ConfigFieldAnnotation annotation = field.getAnnotation(ConfigFieldAnnotation.class);
            if (annotation == null) {
                continue;
            }
            String path = annotation.path();
            String comment = annotation.comment();
            List<String> comments = new ArrayList<>();
            comments.add(comment);
            if (path.isEmpty()) {
                path = field.getName();
            }
            if (value == null) {
                value = field.getType().isPrimitive() ? 0 : null;
            }
            String[] pathParts = path.split("\\.");
            ConfigKey<?> parent = null;
            for (int i = 0; i < pathParts.length; i++) {
                ConfigKey<?> key;
                int finalI = i;
                if (i == 0 && keys.stream().noneMatch(k -> k.getKey().equals(pathParts[finalI]))) {
                    key = new ConfigKey<>(pathParts[i], value, pathParts.length == 1 ? comments : null, null, null, pathParts.length == 1 ? field : null);
                    keys.add(key);
                } else if (i == 0) {
                    key = keys.stream().filter(k -> k.getKey().equals(pathParts[finalI])).findFirst().get();
                } else if (i == pathParts.length - 1) {
                    key = new ConfigKey<>(pathParts[i], value, comments, null, null, field);
                    parent.addChild(key);
                } else {
                    key = new ConfigKey<>(pathParts[i], value, null);
                    parent.addChild(key);
                }
                parent = key;
            }
        }
        ConfigWrapper configWrapper = new ConfigWrapper(name, keys);
        return addConfig(configWrapper);
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

    /**
     * Reads the config's file, if there is one, into its keys.
     *
     * @param name Name of the config you want to load.
     * @return false when the file could not be read, or some of its values could not; the
     *         problem is logged and the keys it concerns keep their values. Such a file is
     *         never saved over by {@link #addConfig}, so nothing the user wrote is lost.
     */
    public boolean loadConfig(String name) {
        File file = new File(configPath, name + ".yml");
        ConfigWrapper configWrapper = getConfigWrapper(name);
        if (configWrapper == null || !file.exists()) {
            return true;
        }
        Object loadedConfig;
        try (Reader reader = new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8)) {
            loadedConfig = yaml.load(reader);
        } catch (IOException | RuntimeException e) {
            logger.warning("Could not read " + file + ", so it was left as it is: " + e.getMessage());
            return false;
        }
        if (loadedConfig == null) {
            return true;
        }
        if (!(loadedConfig instanceof Map)) {
            logger.warning("Could not read " + file + ", so it was left as it is: it must be a map of keys and values.");
            return false;
        }
        List<String> problems = configWrapper.setConfig((Map<?, ?>) loadedConfig);
        for (String problem : problems) {
            logger.warning("Invalid value in " + file + " at " + problem + ", so the file was left as it is.");
        }
        return problems.isEmpty();
    }

    /**
     * Writes the config's keys, with their comments, to its file. The file is replaced in one
     * step, so it is never left half written, and only when its content changes.
     *
     * @param name Name of the config you want to save.
     */
    public void saveConfig(String name) {
        File file = new File(configPath, name + ".yml");
        ConfigWrapper configWrapper = getConfigWrapper(name);
        if (configWrapper == null) {
            return;
        }
        Map<String, Object> config = configWrapper.getConfig();
        StringBuilder writer = new StringBuilder();
        String[] lines = yaml.dump(config).split("\n");
        Map<Integer, ConfigKey<?>> keys = new HashMap<>();
        for (String line : lines) {
            int spaces = 0;
            for (int i = 0; i < line.length(); i++) {
                if (line.charAt(i) == ' ') {
                    spaces++;
                } else {
                    break;
                }
            }
            ConfigKey<?> key = null;
            if (spaces == 0) {
                key = configWrapper.getKey(line.split(":")[0]);
            }
            if (spaces > 0) {
                ConfigKey<?> parent = keys.get(spaces - 2);
                if (parent != null) {
                    key = parent.getKey(line.split(":")[0].trim());
                }
            }
            if (key != null && key.getComments() != null) {
                for (String comment : key.getComments()) {
                    StringBuilder builder = new StringBuilder();
                    for (int i = 0; i < spaces; i++) {
                        builder.append(" ");
                    }
                    writer.append(builder).append("# ").append(comment).append("\n");
                }
            }
            keys.put(spaces, key);
            writer.append(line).append("\n");
        }
        byte[] content = writer.toString().getBytes(StandardCharsets.UTF_8);
        Path target = file.toPath();
        Path temporary = target.resolveSibling(target.getFileName() + ".tmp");
        try {
            if (Files.isRegularFile(target) && Arrays.equals(Files.readAllBytes(target), content)) {
                return;
            }
            Files.write(temporary, content);
            try {
                Files.move(temporary, target, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            } catch (AtomicMoveNotSupportedException e) {
                Files.move(temporary, target, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException e) {
            logger.warning("Could not write " + file + ": " + e.getMessage());
        }
    }

    /**
     * Reloads config of specified name.
     * 
     * @param name Name of the config you want to reload.
     * @return false when the file could not be read; see {@link #loadConfig}.
     */
    public boolean reloadConfig(String name) {
        return loadConfig(name);
    }
}
