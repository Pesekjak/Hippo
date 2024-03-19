import me.pesekjak.hippo.CheckStyleProvider

plugins {
    java
    `java-library`
    checkstyle
}

val group: String by project
setGroup(group)

val version: String by project
setVersion(version)

val libs = project.rootProject
    .extensions
    .getByType(VersionCatalogsExtension::class)
    .named("libs")

//
// Repositories and Dependencies
//

repositories {
    mavenCentral()
}

dependencies {
    compileOnly(libs.findLibrary("jetbrains-annotations").get())

    testImplementation(libs.findLibrary("junit-api").get())
    testRuntimeOnly(libs.findLibrary("junit-engine").get())
    testImplementation(libs.findLibrary("junit-params").get())
}

//
// Java configuration
//

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}


//
// Checkstyle configuration
//

checkstyle {
    toolVersion = "10.13.0"
    config = resources.text.fromUri(CheckStyleProvider.get())
}

dependencies {
    modules {
        // Replace old dependency `google-collections` with `guava`
        // This is required for checkstyle to work
        module("com.google.collections:google-collections") {
            replacedBy("com.google.guava:guava", "google-collections is part of guava")
        }
    }
}

//
// Task configurations
//

tasks {
    withType<JavaCompile> {
        options.release.set(17)
        options.encoding = Charsets.UTF_8.name()
    }
    withType<ProcessResources> {
        filteringCharset = Charsets.UTF_8.name()
    }
    test {
        useJUnitPlatform()
    }
}
