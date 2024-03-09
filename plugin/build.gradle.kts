import me.pesekjak.hippo.PluginProperties

plugins {
    id("hippo.library-conventions")
    alias(libs.plugins.shadow)
}

repositories {
    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
    maven("https://repo.skriptlang.org/releases/")

    maven("https://oss.sonatype.org/content/repositories/snapshots")
    maven("https://oss.sonatype.org/content/repositories/central")
}

dependencies {
    implementation(project(":asm-core"))
    implementation(project(":magic-utils"))
    implementation(libs.asm)

    compileOnly(libs.spigot.api)
    compileOnly(libs.skript)
    compileOnly(files("$projectDir/../libs/skript-reflect.jar"))

    testImplementation(libs.spigot.api)
    testImplementation(libs.skript)
    testImplementation(files("$projectDir/../libs/skript-reflect.jar"))
}

tasks {
    processResources {
        filteringCharset = Charsets.UTF_8.name() // We want UTF-8 for everything
        val props = mapOf(
            "name" to PluginProperties.name,
            "version" to PluginProperties.version,
            "mainClass" to PluginProperties.mainClass,
            "description" to PluginProperties.description,
            "authors" to PluginProperties.authors,
            "apiVersion" to PluginProperties.apiVersion,
            "dependencies" to PluginProperties.dependencies
        )
        inputs.properties(props)
        filesMatching("plugin.yml") {
            expand(props)
        }
    }
}