package me.chrommob.config.test;

import me.chrommob.config.ConfigKey;
import me.chrommob.config.ConfigManager;
import me.chrommob.config.ConfigWrapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Handler;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Loading and saving files the user has edited: nothing they wrote may be lost. */
class LoadSaveTest {
    @TempDir
    Path folder;

    private final List<String> warnings = new ArrayList<>();

    @Test
    void sectionsKeepKeysTheCodeDidNotDeclare() throws IOException {
        write("servers:\n"
                + "  lobby:\n"
                + "    host: lobby.example.net\n"
                + "    port: 25565\n"
                + "enabled: false\n"
                + "texts:\n"
                + "  hello: Ahoj\n"
                + "  removed: Still here\n"
                + "legacy:\n"
                + "  nested: 1\n");
        ConfigManager manager = manager();
        manager.addConfig(config());
        ConfigWrapper config = manager.getConfigWrapper("config");

        assertEquals(false, config.getKey("enabled").getValue());
        assertEquals("Ahoj", config.getKey("texts").getKey("hello").getValue());
        Map<?, ?> servers = (Map<?, ?>) config.getKey("servers").getValue();
        assertEquals("lobby.example.net", ((Map<?, ?>) servers.get("lobby")).get("host"));
        assertTrue(warnings.isEmpty(), warnings.toString());

        String saved = read();
        assertTrue(saved.contains("  lobby:\n    host: lobby.example.net\n    port: 25565\n"), saved);
        assertTrue(saved.contains("  removed: Still here\n"), saved);
        assertTrue(saved.contains("legacy:\n  nested: 1\n"), saved);
        assertTrue(saved.contains("  goodbye: Bye\n"), "a key missing from the file is added: " + saved);
    }

    @Test
    void aFileThatCannotBeReadIsLeftAsItIs() throws IOException {
        String broken = "enabled: false\ntexts: [unclosed\n";
        write(broken);
        ConfigManager manager = manager();
        assertFalse(manager.addConfig(config()));

        assertEquals(broken, read());
        assertEquals(true, manager.getConfigWrapper("config").getKey("enabled").getValue());
        assertEquals(1, warnings.size(), warnings.toString());
        assertTrue(warnings.get(0).startsWith("Could not read "), warnings.get(0));
    }

    @Test
    void anInvalidValueIsReportedAndTheOthersStillLoad() throws IOException {
        String file = "enabled: false\nlimit: lots\n";
        write(file);
        ConfigManager manager = manager();
        manager.addConfig(config());
        ConfigWrapper config = manager.getConfigWrapper("config");

        assertEquals(false, config.getKey("enabled").getValue());
        assertEquals(10, config.getKey("limit").getValue());
        assertEquals(file, read(), "the file is not saved over");
        assertEquals(1, warnings.size(), warnings.toString());
        assertTrue(warnings.get(0).contains("at limit (lots is not a valid value)"), warnings.get(0));
    }

    @Test
    void reloadingFollowsTheFile() throws IOException {
        write("enabled: false\ntexts:\n  hello: Ahoj\n  removed: Old\nlegacy: 1\n");
        ConfigManager manager = manager();
        manager.addConfig(config());

        write("texts:\n  goodbye: Nashle\n");
        assertTrue(manager.reloadConfig("config"));
        ConfigWrapper config = manager.getConfigWrapper("config");
        assertEquals(true, config.getKey("enabled").getValue(), "a removed key is back to its default");
        assertEquals("Hello", config.getKey("texts").getKey("hello").getValue());
        assertEquals("Nashle", config.getKey("texts").getKey("goodbye").getValue());
        assertNull(config.getKey("texts").getKey("removed"), "a key gone from the file is gone");
        assertNull(config.getKey("legacy"));
    }

    @Test
    void keysANewerVersionNoLongerUsesCanBeRemoved() throws IOException {
        write("enabled: false\nlegacy: 1\ntexts:\n  hello: Ahoj\n  removed: Old\n");
        ConfigManager manager = manager();
        manager.addConfig(config());
        ConfigWrapper config = manager.getConfigWrapper("config");

        assertEquals(1, config.removeKey("legacy").getValue());
        assertEquals("Old", config.getKey("texts").removeChild("removed").getValue());
        manager.saveConfig("config");
        String saved = read();
        assertFalse(saved.contains("legacy"), saved);
        assertFalse(saved.contains("removed"), saved);
        assertTrue(saved.contains("enabled: false\n"), saved);
    }

    @Test
    void theFileIsTidyAndOnlyWrittenWhenItChanges() throws IOException {
        ConfigManager manager = manager();
        manager.addConfig(config());
        String saved = read();
        assertTrue(saved.startsWith("# Servers to connect to\nservers: {}\n"), saved);
        assertTrue(saved.contains("  long: " + longText() + "\n"), "long texts stay on one line: " + saved);
        assertTrue(saved.contains("  czech: Příliš žluťoučký kůň\n"), saved);

        File file = folder.resolve("config.yml").toFile();
        assertTrue(file.setLastModified(1_000_000_000L));
        manager.saveConfig("config");
        assertEquals(1_000_000_000L, file.lastModified());
        assertFalse(Files.exists(folder.resolve("config.yml.tmp")));
    }

    private ConfigManager manager() {
        Logger logger = Logger.getAnonymousLogger();
        logger.setUseParentHandlers(false);
        logger.addHandler(new Handler() {
            @Override
            public void publish(LogRecord record) {
                warnings.add(record.getMessage());
            }

            @Override
            public void flush() {
            }

            @Override
            public void close() {
            }
        });
        return new ConfigManager(folder.toFile(), logger);
    }

    private static ConfigWrapper config() {
        return new ConfigWrapper("config", Arrays.asList(
                new ConfigKey<>("servers", new LinkedHashMap<String, Object>(), Arrays.asList("Servers to connect to")),
                new ConfigKey<>("enabled", true),
                new ConfigKey<>("limit", 10),
                new ConfigKey<>("texts", Arrays.asList(
                        new ConfigKey<>("hello", "Hello"),
                        new ConfigKey<>("goodbye", "Bye"),
                        new ConfigKey<>("long", longText()),
                        new ConfigKey<>("czech", "Příliš žluťoučký kůň")))));
    }

    private static String longText() {
        StringBuilder text = new StringBuilder();
        for (int i = 0; i < 20; i++) {
            text.append("word").append(i).append(' ');
        }
        return text.append("end").toString();
    }

    private void write(String content) throws IOException {
        Files.write(folder.resolve("config.yml"), content.getBytes(StandardCharsets.UTF_8));
    }

    private String read() throws IOException {
        return new String(Files.readAllBytes(folder.resolve("config.yml")), StandardCharsets.UTF_8);
    }
}
