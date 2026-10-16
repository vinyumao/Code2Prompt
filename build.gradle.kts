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
        changeNotes = "<ul><li>移除“要做什么:”提示，并在复制内容末尾追加两个换行，方便继续输入需求。</li><li>复制时同时提供 Markdown 和 HTML 富文本，支持在 Codex 中粘贴文件链接和带语言高亮的代码块。</li><li>保留文件、行号、符号上下文及代码原有的空格和换行。</li></ul>"

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
