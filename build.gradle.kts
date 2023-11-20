plugins {
    id("java")
    id("com.github.johnrengelman.shadow") version("8.1.1")
}

group = "me.chrommob"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.yaml:snakeyaml:2.2")
}

tasks.shadowJar {
    relocate("org.yaml", "me.chrommob.config.libs")
}