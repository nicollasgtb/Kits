plugins {
    java
    
    id("com.gradleup.shadow") version "9.3.0"
}

group = "com.codenicollas.kits"
version = "1.1.2"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

repositories {
    mavenCentral()
    
    maven {
        name = "spigot"
        url = uri("https://hub.spigotmc.org/nexus/content/groups/public/")
    }   
}

dependencies {
    compileOnly("org.spigotmc:spigot-api:1.21.4-R0.1-SNAPSHOT")

    implementation("com.zaxxer:HikariCP:7.0.2")
    implementation("org.mariadb.jdbc:mariadb-java-client:3.5.7")
}

tasks {
    build {
        dependsOn(shadowJar)
    }

    processResources {
        filesMatching("plugin.yml") {
            expand("version" to version)
        }
    }

    shadowJar {
        archiveBaseName.set("Kits")
        archiveClassifier.set("")

        relocate("com.zaxxer.hikari", "br.ynicollas.kits.libs.hikari")
        relocate("org.mariadb.jdbc", "br.ynicollas.kits.libs.mariadb")
        
        relocate("org.slf4j", "br.ynicollas.kits.libs.slf4j")
    }

    withType<JavaCompile>().configureEach {
        options.encoding = "UTF-8"
        options.release.set(11)
    }
}