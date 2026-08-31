import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.intellij.platform.gradle.IntelliJPlatformType

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
    // Android Studio 2024.2 起使用 JBR 21；插件字节码不得高于宿主运行时。
    // 仅声明字节码目标，避免本机必须另行安装 JDK 21。
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_21)
    }
}

intellijPlatform {
    pluginConfiguration {
        id = "com.wayen.code2prompt"
        name = "Code2Prompt: Copy Code Context"
        version = providers.gradleProperty("pluginVersion")

        description = "Copies the selected code together with its file, line numbers, and symbol context."
        changeNotes = "<ul><li>复制后在浮动工具栏显示成功对勾，并提供复制成功或失败提示。</li><li>使用 Java 21 编译，修复 Android Studio Panda 2 中复制入口无法加载的问题。</li><li>兼容 Android Studio 2024.2（Build 242）及更高版本。</li></ul>"

        ideaVersion {
            sinceBuild = "242"
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

    pluginVerification {
        ides {
            select {
                types.set(listOf(IntelliJPlatformType.AndroidStudio))
                sinceBuild.set("242")
                untilBuild.set("242.*")
            }
        }
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
