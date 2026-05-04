plugins {
    java
    id("org.jetbrains.intellij.platform") version "2.16.0"
}

group = "com.ijerinic"
version = "0.1.0"

repositories {
    mavenCentral()
    intellijPlatform {
        defaultRepositories()
    }
}

dependencies {
    // Official Google Gen AI Java SDK — supports streaming via generateContentStream
    implementation("com.google.genai:google-genai:1.52.0")

    intellijPlatform {
        // Targeting IntelliJ IDEA Community 2024.3 — stable, broadly available
        intellijIdeaCommunity("2024.3")
    }
}

java {
    toolchain {
        // Java 21 — current LTS, supported by recent IntelliJ Platform versions
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

intellijPlatform {
    pluginConfiguration {
        ideaVersion {
            sinceBuild.set("243")
            untilBuild.set("251.*")
        }
    }
}

tasks {
    wrapper {
        gradleVersion = "8.10"
    }
}
