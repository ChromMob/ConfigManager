package me.chrommob.config;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface ConfigFieldAnnotation {
    String path() default "";
    String comment() default "";
}
