plugins {
    id("hippo.library-conventions")
}

repositories {
    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
    maven("https://repo.skriptlang.org/releases/")

    maven("https://oss.sonatype.org/content/repositories/snapshots")
    maven("https://oss.sonatype.org/content/repositories/central")
}

dependencies {
    implementation(libs.asm)

    compileOnly(libs.spigot.api)
    compileOnly(libs.skript)

    compileOnly(files("$projectDir/../libs/skript-reflect.jar"))
}