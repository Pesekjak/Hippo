plugins {
    java
    `java-library`
    checkstyle
}

val libs = extensions.getByType(org.gradle.accessors.dm.LibrariesForLibs::class)

repositories {
    mavenCentral()
}

dependencies {
    compileOnly(libs.jetbrains.annotations)
    testImplementation(libs.junit.api)
    testRuntimeOnly(libs.junit.engine)
    testImplementation(libs.junit.params)
}

checkstyle {
    toolVersion = libs.versions.checkstyle.get()
    configFile = File(rootDir, "code_style.xml")
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(17))
}

tasks {
    compileJava {
        options.encoding = Charsets.UTF_8.name()
        options.release.set(17)
        options.compilerArgs = listOf("-Xlint:unchecked")
    }
    javadoc {
        options.encoding = Charsets.UTF_8.name()
    }
    processResources {
        filteringCharset = Charsets.UTF_8.name()
    }
    test {
        useJUnitPlatform()
    }
}

object Properties {

    lateinit var name: String
    lateinit var version: String
    lateinit var mainClass: String
    lateinit var description: String
    lateinit var authors: String
    lateinit var apiVersion: String
    lateinit var dependencies: String

}