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

            val junitLauncher: String by settings
            library("junit-launcher", "org.junit.platform:junit-platform-launcher:$junitLauncher")

            val paperApi: String by settings
            library("paper-api", "io.papermc.paper:paper-api:$paperApi")

            val skript: String by settings
            library("skript", "com.github.SkriptLang:Skript:$skript")

            val asm: String by settings
            library("asm", "org.ow2.asm:asm:$asm")

            val shadow: String by settings
            plugin("shadow", "com.gradleup.shadow").version(shadow)

            val checkstyle: String by settings
            plugin("checkstyle", "checkstyle").version(checkstyle)
        }

    }
}