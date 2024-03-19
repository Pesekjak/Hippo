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

    jar {
        archiveBaseName = "Hippo"
    }

    //
    // Modifying the plugin.yml to include the project version.
    //
    processResources {
        val props = mapOf("version" to version)
        inputs.properties(props)
        filesMatching("plugin.yml") {
            expand(props)
        }
    }

    //
    // Shading in the ASM library
    //
    build {
        dependsOn(shadowJar)
    }
    shadowJar {
        archiveBaseName = "Hippo"
        archiveClassifier = ""
        relocate("org.objectweb.asm", "me.pesekjak.hippo.asm")
    }

}