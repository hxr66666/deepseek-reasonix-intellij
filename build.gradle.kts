plugins {
    id("java")
    id("org.jetbrains.intellij.platform") version "2.10.2"
}

group = "org.deepseek.reasonix"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    intellijPlatform {
        defaultRepositories()
    }
}

// Read more: https://plugins.jetbrains.com/docs/intellij/tools-intellij-platform-gradle-plugin.html
dependencies {
    intellijPlatform {
        //intellijIdea("2025.2.4")
        local("/home/hxr/jetbrains/idea-IU-253.32098.37")
        testFramework(org.jetbrains.intellij.platform.gradle.TestFrameworkType.Platform)

        // Add plugin dependencies for compilation here:

        bundledPlugin("com.intellij.java")
        bundledPlugin("org.intellij.plugins.markdown")
        implementation("com.fasterxml.jackson.core:jackson-databind:2.15.3")
    }
}

intellijPlatform {
    pluginConfiguration {
        ideaVersion {
            sinceBuild = "253"
        }

        changeNotes = """
            Initial version
        """.trimIndent()
    }
}

tasks {
    // Set the JVM compatibility versions
    withType<JavaCompile> {
        sourceCompatibility = "21"
        targetCompatibility = "21"
    }
}
