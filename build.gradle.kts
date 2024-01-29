import org.jetbrains.intellij.tasks.PublishPluginTask

plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "1.8.10"
    id("org.jetbrains.intellij") version "1.14.1"
}

val pluginGroup: String by project
val pluginVersion: String by project
val pluginSinceBuild: String by project
val pluginUntilBuild: String by project

group = pluginGroup
version = pluginVersion

repositories {
    mavenCentral()
}

//dependencies {
//    implementation(kotlin("stdlib-jdk8"))
//}

// See https://github.com/JetBrains/gradle-intellij-plugin/
intellij {
    version.set("2022.2")
    type.set("IC")

    plugins.set(listOf("java", "android", "Kotlin"))
//    localPath = "C:\\Program Files\\JetBrains\\IntelliJ IDEA Community Edition 2021.2"
//    instrumentCode = false
}

tasks {
    withType<JavaCompile> {
        sourceCompatibility = "11"
        targetCompatibility = "11"
    }

    patchPluginXml {
//        version(pluginVersion)
        version.set(pluginVersion)
        sinceBuild.set(pluginSinceBuild)
        untilBuild.set(pluginUntilBuild)
    }

    publishPlugin {
        val publishToken = System.getenv("IDEA_PublishToken")
        token.set(publishToken)
    }
}


tasks.getByName<org.jetbrains.intellij.tasks.PatchPluginXmlTask>("patchPluginXml") {
    changeNotes.set(
        """
          Support Android Bean Parcelable and Getter Setter

          2022.8.3 update support intellij version

        """.trimIndent()
    )

}