rootProject.name = "Hippo"

pluginManagement {
    includeBuild("build-logic")
}

include("asm-core")
include("magic-utils")
include("plugin")

dependencyResolutionManagement {
    versionCatalogs {

        create("libs") {
            val jetbrainsAnnotations: String by settings
            library("jetbrains-annotations", "org.jetbrains:annotations:$jetbrainsAnnotations")

            val junit: String by settings
            library("junit-api", "org.junit.jupiter:junit-jupiter-api:$junit")
            library("junit-engine", "org.junit.jupiter:junit-jupiter-engine:$junit")
            library("junit-params", "org.junit.jupiter:junit-jupiter-params:$junit")

            val spigotApi: String by settings
            library("spigot-api", "org.spigotmc:spigot-api:$spigotApi")

            val skript: String by settings
            library("skript", "com.github.SkriptLang:Skript:$skript")

            val asm: String by settings
            library("asm", "org.ow2.asm:asm:$asm")

            val shadow: String by settings
            plugin("shadow", "io.github.goooler.shadow").version(shadow)
        }

    }
}