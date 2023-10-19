plugins {
    `kotlin-dsl`
}

repositories {
    gradlePluginPortal()
}

dependencies {
    implementation(libs.jetbrains.kotlin.gradle)
    compileOnly(files(libs::class.java.superclass.protectionDomain.codeSource.location))
}