plugins {
    id("hippo.library-conventions")
}

repositories {
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://repo.skriptlang.org/releases/")
}

dependencies {
    implementation(libs.asm)

    compileOnly(libs.paper.api)
    compileOnly(libs.skript)

    compileOnly(files("$projectDir/../libs/skript-reflect.jar"))
}