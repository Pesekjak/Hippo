import Hippo_library_conventions_gradle.Properties

@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    id("hippo.library-conventions")
    alias(libs.plugins.johnrengelman.shadow)
}

repositories {
    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
    maven("https://oss.sonatype.org/content/repositories/snapshots")
    maven("https://oss.sonatype.org/content/repositories/central")
    maven("https://repo.skriptlang.org/releases/")
}

dependencies {
    implementation(project(":asm-core"))
    implementation(project(":magic-utils"))
    implementation(libs.asm)
    compileOnly(libs.spigot.api)
    compileOnly(libs.skript)
    compileOnly(files("$projectDir/../libs/skript-reflect-2.4-dev1.jar"))

    testImplementation(libs.spigot.api)
    testImplementation(libs.skript)
    testImplementation(files("$projectDir/../libs/skript-reflect-2.4-dev1.jar"))
}

tasks {
    processResources {
        filteringCharset = Charsets.UTF_8.name() // We want UTF-8 for everything
        val props = mapOf(
            "name" to Properties.name,
            "version" to Properties.version,
            "mainClass" to Properties.mainClass,
            "description" to Properties.description,
            "authors" to Properties.authors,
            "apiVersion" to Properties.apiVersion,
            "dependencies" to Properties.dependencies
        )
        inputs.properties(props)
        filesMatching("plugin.yml") {
            expand(props)
        }
    }
}