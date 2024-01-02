package me.chrommob.config.test;

import java.io.File;
import java.util.ArrayList;

import me.chrommob.config.ConfigKey;
import me.chrommob.config.ConfigManager;
import me.chrommob.config.ConfigWrapper;

public class ConfigTest {
    public static void main(String[] args) {
        File currentDir = new File(System.getProperty("user.dir"));
        ConfigManager config = new ConfigManager(currentDir);
        config.addConfig(new ConfigWrapper("config", new ArrayList<ConfigKey>() {
            {
                add(new ConfigKey("test", "test", new ArrayList<String>() {
                    {
                        add("This is an example of a comment");
                        add("This is another example of a comment");
                        add("This is the last example of a comment");
                    }
                }));
                add(new ConfigKey("test2", "test2"));
                add(new ConfigKey("test3", new ArrayList<ConfigKey>() {
                    {
                        add(new ConfigKey("test4", "test4", new ArrayList<String>() {
                            {
                                add("This is an example of a comment");
                                add("This is another example of a comment");
                                add("This is the last example of a comment");
                            }
                        }));
                        add(new ConfigKey("test5", "test5"));
                        add(new ConfigKey("test", "test", new ArrayList<String>() {
                            {
                                add("This is an example of a comment");
                                add("This is another example of a comment");
                                add("This is the last example of a comment");
                            }
                        }));
                        add(new ConfigKey("test2", new ArrayList<ConfigKey>() {
                            {
                                add(new ConfigKey("test4", "test4", new ArrayList<String>() {
                                    {
                                        add("This is an example of a comment");
                                        add("This is another example of a comment");
                                        add("This is the last example of a comment");
                                    }
                                }));
                                add(new ConfigKey("test5", "test5"));
                                add(new ConfigKey("test", "test", new ArrayList<String>() {
                                    {
                                        add("This is an example of a comment");
                                        add("This is another example of a comment");
                                        add("This is the last example of a comment");
                                    }
                                }));
                                add(new ConfigKey("test2", "test2"));
                                add(new ConfigKey("test3", new ArrayList<ConfigKey>() {
                                    {
                                        add(new ConfigKey("test4", "test4", new ArrayList<String>() {
                                            {
                                                add("This is an example of a comment");
                                                add("This is another example of a comment");
                                                add("This is the last example of a comment");
                                            }
                                        }));
                                        add(new ConfigKey("test5", "test5"));
                                        add(new ConfigKey("test", "test", new ArrayList<String>() {
                                            {
                                                add("This is an example of a comment");
                                                add("This is another example of a comment");
                                                add("This is the last example of a comment");
                                            }
                                        }));
                                        add(new ConfigKey("test2", "test2"));
                                        add(new ConfigKey("test3", new ArrayList<ConfigKey>() {
                                            {
                                                add(new ConfigKey("test4", "test4", new ArrayList<String>() {
                                                    {
                                                        add("This is an example of a comment");
                                                        add("This is another example of a comment");
                                                        add("This is the last example of a comment");
                                                    }
                                                }));
                                                add(new ConfigKey("test5", "test5"));
                                                add(new ConfigKey("test", "test", new ArrayList<String>() {
                                                    {
                                                        add("This is an example of a comment");
                                                        add("This is another example of a comment");
                                                        add("This is the last example of a comment");
                                                    }
                                                }));
                                                add(new ConfigKey("test2", "test2"));
                                                add(new ConfigKey("test3", new ArrayList<ConfigKey>() {
                                                    {
                                                        add(new ConfigKey("test4", "test4", new ArrayList<String>() {
                                                            {
                                                                add("This is an example of a comment");
                                                                add("This is another example of a comment");
                                                                add("This is the last example of a comment");
                                                            }
                                                        }));
                                                        add(new ConfigKey("test5", "test5"));
                                                        add(new ConfigKey("test", "test", new ArrayList<String>() {
                                                            {
                                                                add("This is an example of a comment");
                                                                add("This is another example of a comment");
                                                                add("This is the last example of a comment");
                                                            }
                                                        }));
                                                    }
                                                }));
                                            }
                                        }));
                                    }
                                }));
                            }
                        }));
                    }
                }));
            }
        }));

    }
}
