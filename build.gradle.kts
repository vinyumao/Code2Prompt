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
        name = "Code2Prompt: Copy Code Context"
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

    // 签名材料保留在已忽略的本地目录；密码仅从环境变量读取。
    signing {
        certificateChainFile = layout.projectDirectory.file("release-secrets/chain.crt")
        privateKeyFile = layout.projectDirectory.file("release-secrets/private.pem")
        password = providers.environmentVariable("PRIVATE_KEY_PASSWORD")
    }
}

tasks {
    test {
        useJUnitPlatform()
    }

    named("verifyPluginSignature") {
        dependsOn(named("signPlugin"))
    }
}
