import Hippo_library_conventions_gradle.Properties

@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    id("hippo.library-conventions")
    alias(libs.plugins.johnrengelman.shadow)
}

allprojects {
    group = "me.pesekjak.hippo"
    version = "1.1"
}

dependencies {
    implementation(project(":asm-core"))
    implementation(project(":magic-utils"))
    implementation(project(":plugin"))
}

Properties.name = "Hippo"
Properties.version = version.toString()
Properties.mainClass = "me.pesekjak.hippo.Hippo"
Properties.description = "Skript addon for custom classes"
Properties.authors = "[pesekjak]"
Properties.apiVersion = "1.13"
Properties.dependencies = "[Skript, skript-reflect]"

tasks {
    build {
        dependsOn(shadowJar)
    }
    shadowJar {
        archiveClassifier.set("")
        relocate("org.objectweb.asm", "me.pesekjak.hippo.asm")
    }
}