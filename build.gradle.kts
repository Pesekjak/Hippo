import me.pesekjak.hippo.PluginProperties

plugins {
    id("hippo.library-conventions")
    alias(libs.plugins.shadow)
}

dependencies {
    implementation(project(":asm-core"))
    implementation(project(":magic-utils"))
    implementation(project(":plugin"))
}

PluginProperties.name = "Hippo"
PluginProperties.version = version.toString()
PluginProperties.mainClass = "me.pesekjak.hippo.Hippo"
PluginProperties.description = "Skript addon for custom classes"
PluginProperties.authors = "[pesekjak]"
PluginProperties.apiVersion = "1.13"
PluginProperties.dependencies = "[Skript, skript-reflect]"

tasks {
    build {
        dependsOn(shadowJar)
    }
    shadowJar {
        archiveClassifier.set("")
        relocate("org.objectweb.asm", "me.pesekjak.hippo.asm")
    }
}