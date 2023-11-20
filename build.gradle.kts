plugins {
    id("maven-publish")
    id("java")
    id("com.github.johnrengelman.shadow") version("8.1.1")
}

group = "me.chrommob"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

var targetJavaVersion = 8
var encoding = "UTF-8"

java {
    val javaVersion = JavaVersion.toVersion(targetJavaVersion)
    sourceCompatibility = javaVersion
    targetCompatibility = javaVersion
    if (JavaVersion.current() < javaVersion) {
        toolchain.languageVersion = JavaLanguageVersion.of(targetJavaVersion)
    }
}

tasks.compileJava {
    if (targetJavaVersion >= 10 || JavaVersion.current().isJava10Compatible()) {
        options.release = targetJavaVersion
    }
    options.encoding = encoding
}

dependencies {
    implementation("org.yaml:snakeyaml:2.2")
}

tasks.shadowJar {
    relocate("org.yaml", "me.chrommob.config.libs")
}