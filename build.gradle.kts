import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.jvm.toolchain.JavaLanguageVersion

plugins {
    java

    id("com.gradleup.shadow") version "9.1.0"
}

group = "br.ynicollas"
version = "1.0.7"

repositories {
    mavenCentral()

    maven { url = uri("https://hub.spigotmc.org/nexus/content/repositories/snapshots/") }
}

dependencies {
    compileOnly("org.spigotmc:spigot-api:1.16.5-R0.1-SNAPSHOT")

    implementation("com.zaxxer:HikariCP:7.0.2")
    implementation("org.mariadb.jdbc:mariadb-java-client:3.5.6")
}

tasks.shadowJar {
    archiveBaseName.set("Kits")
    archiveClassifier.set("")

    relocate("com.zaxxer.hikari", "br.ynicollas.libs.hikari")
    relocate("org.mariadb.jdbc", "br.ynicollas.libs.mariadb")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(11))
    }
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
}

tasks.processResources {
    expand(project.properties)
}