plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "2.2.20"
    id("org.jetbrains.intellij.platform") version "2.10.4"
}

group = providers.gradleProperty("pluginGroup").get()
version = providers.gradleProperty("pluginVersion").get()

kotlin {
    jvmToolchain(17)
}

repositories {
    mavenCentral()
    intellijPlatform {
        defaultRepositories()
    }
}

dependencies {
    intellijPlatform {
        intellijIdea(providers.gradleProperty("platformVersion").get())
    }
}

intellijPlatform {
    pluginConfiguration {
        name = providers.gradleProperty("pluginName").get()
        version = providers.gradleProperty("pluginVersion").get()
        description = providers.fileContents(layout.projectDirectory.file("description.html")).asText
        changeNotes = providers.fileContents(layout.projectDirectory.file("change-notes.html")).asText
        ideaVersion {
            sinceBuild = providers.gradleProperty("pluginSinceBuild").get()
            untilBuild = providers.gradleProperty("pluginUntilBuild").get().ifBlank { null }
        }
    }
    instrumentCode = false
}

tasks {
    buildSearchableOptions {
        enabled = false
    }
}
