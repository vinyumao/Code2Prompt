plugins {
    kotlin("jvm") version "2.4.0"
    id("org.jetbrains.intellij.platform") version "2.18.1"
}

group = "com.wayen.code2prompt"
version = providers.gradleProperty("pluginVersion").get()

repositories {
    mavenCentral()
    intellijPlatform {
        defaultRepositories()
    }
}

dependencies {
    testImplementation(kotlin("test"))

    intellijPlatform {
        local(providers.gradleProperty("platformPath"))
        bundledPlugin("com.intellij.java")
    }
}

kotlin {
    jvmToolchain(25)
}

intellijPlatform {
    pluginConfiguration {
        id = "com.wayen.code2prompt"
        name = "Code2Prompt"
        version = providers.gradleProperty("pluginVersion")

        description = "Copies the selected code together with its file, line numbers, and symbol context."
        changeNotes = "<ul><li>Initial local development build.</li></ul>"

        ideaVersion {
            sinceBuild = "261"
        }

        vendor {
            name = "Wayne"
        }
    }
}

tasks {
    test {
        useJUnitPlatform()
    }
}
