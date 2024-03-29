package me.chrommob.config.test;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import me.chrommob.config.ConfigKey;
import me.chrommob.config.ConfigManager;
import me.chrommob.config.ConfigWrapper;

public class ConfigTest {
    public static void main(String[] args) {
        File currentDir = new File(System.getProperty("user.dir"), "tests");
        currentDir.mkdirs();
        ConfigManager config = new ConfigManager(currentDir);
        config.addAnnotatedConfig(AnnotationTest.class);
        System.out.println("AnnotationTest enabled: " + AnnotationTest.enabled);
        System.out.println("AnnotationTest complicated: " + AnnotationTest.complicated);
        List<String> checks = new ArrayList<String>() {
            {
                add("AimA");
                add("AimB");
                add("AimC");
                add("KillAura");
                add("AntiBot");
                add("AntiAFK");
            }
        };
        CustomWrapper wrapper = new CustomWrapper("config", new ArrayList<ConfigKey>() {
            {
                add(new ConfigKey("version", "2.0", new ArrayList<String>() {
                    {
                        add("Major version number");
                        add("Incremented for major changes");
                        add("Backwards-incompatible changes");
                    }
                }));
                add(new ConfigKey("modules", new ArrayList<ConfigKey>() {
                    {
                        add(new ConfigKey("security", new ArrayList<ConfigKey>() {
                            {
                                add(new ConfigKey("encryption", "AES-256", new ArrayList<String>() {
                                    {
                                        add("Strong encryption algorithm");
                                        add("Ensures data security");
                                        add("AES-256 is widely accepted");
                                    }
                                }));
                                add(new ConfigKey("authentication", "JWT"));
                                add(new ConfigKey("firewall", "enabled", new ArrayList<String>() {
                                    {
                                        add("Protects against unauthorized access");
                                        add("Configurable firewall settings");
                                        add("Logs access attempts");
                                    }
                                }));
                            }
                        }));
                        add(new ConfigKey("database", new ArrayList<ConfigKey>() {
                            {
                                add(new ConfigKey("type", "MongoDB"));
                                add(new ConfigKey("connection", "mongodb://localhost:27017/mydb"));
                                add(new ConfigKey("indexes", new ArrayList<ConfigKey>() {
                                    {
                                        add(new ConfigKey("user", "username", new ArrayList<String>() {
                                            {
                                                add("Unique index for user data");
                                                add("Improves query performance");
                                                add("Prevents duplicate usernames");
                                            }
                                        }));
                                        add(new ConfigKey("logs", "timestamp"));
                                    }
                                }));
                            }
                        }));
                        add(new ConfigKey("logging", new ArrayList<ConfigKey>() {
                            {
                                add(new ConfigKey("level", "DEBUG"));
                                add(new ConfigKey("output", new ArrayList<String>() {
                                    {
                                        add("Logs to file and console");
                                        add("Configurable log output");
                                        add("Useful for debugging");
                                    }
                                }));
                            }
                        }));
                    }
                }));
                add(new ConfigKey("advanced_settings", new ArrayList<ConfigKey>() {
                    {
                        add(new ConfigKey("concurrency", "multi-threaded"));
                        add(new ConfigKey("caching", new ArrayList<ConfigKey>() {
                            {
                                add(new ConfigKey("enabled", "true"));
                                add(new ConfigKey("size", "512MB"));
                                add(new ConfigKey("strategy", "LRU", new ArrayList<String>() {
                                    {
                                        add("Least Recently Used (LRU) caching");
                                        add("Improves data access speed");
                                        add("Optimizes memory usage");
                                    }
                                }));
                            }
                        }));
                        add(new ConfigKey("performance_tuning", new ArrayList<String>() {
                            {
                                add("Fine-tune for specific use cases");
                                add("Optimize resource utilization");
                                add("Regularly monitor and adjust settings");
                            }
                        }));
                    }
                }));
                add(new ConfigKey("checks", new ArrayList<ConfigKey>() {
                    {
                        for (String check : checks) {
                            add(new ConfigKey(check, settingConfigKeys()));
                        }
                    }
                }, new ArrayList<String>() {
                    {
                        add("Checks to be executed");
                        add("Enable or disable checks");
                        add("Fine-tune check settings");
                    }
                }));
            }
        });

        List<ConfigKey> listList = new ArrayList<>();
        listList.add(new ConfigKey("enabled", true));
        List<String> keys = new ArrayList<>();
        keys.add("AimA");
        keys.add("AimB");
        listList.add(new ConfigKey("key", keys));
        ConfigWrapper wrapper2 = new ConfigWrapper("list", listList);
        config.addConfig(wrapper2);

        for (String check : checks) {
            Settings settings = wrapper.getSettings(check);
            if (settings.isEnabled()) {
                System.out.println(check + " is enabled with value " + settings.getValue());
                System.out.println("Command: " + settings.getCommand());
            } else {
                System.out.println(check + " is disabled");
            }
        }
    }

    public static List<ConfigKey> settingConfigKeys() {
        return new ArrayList<ConfigKey>() {
            {
                add(new ConfigKey("enabled", true, new ArrayList<String>() {
                    {
                        add("Enable or disable this setting");
                        add("If disabled, this setting will not be used");
                    }
                }));
                add(new ConfigKey("value", 20, new ArrayList<String>() {
                    {
                        add("The value of this setting");
                        add("This value will be used if the setting is enabled");
                    }
                }));
                add(new ConfigKey("command", "say test", new ArrayList<String>() {
                    {
                        add("The command to execute");
                    }
                }));
            }
        };
    }

}
