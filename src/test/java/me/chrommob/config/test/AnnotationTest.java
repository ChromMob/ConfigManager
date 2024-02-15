package me.chrommob.config.test;

import me.chrommob.config.ConfigFieldAnnotation;
import me.chrommob.config.ConfigNameAnnotation;

@ConfigNameAnnotation("test")
public class AnnotationTest {
    @ConfigFieldAnnotation(path = "test.win", comment = "This is a test")
    public static boolean enabled = false;

    @ConfigFieldAnnotation(path = "test.complicated", comment = "This is a complicated test")
    public static String complicated = "complicated";
}
