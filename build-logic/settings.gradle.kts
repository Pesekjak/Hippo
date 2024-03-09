import java.io.FileReader
import java.util.Properties

val rootProperties = Properties()
rootProperties.load(FileReader(File(rootDir.parent, "gradle.properties")))

dependencyResolutionManagement {
    versionCatalogs {

        create("libs") {
            val jetbrainsKotlinGradle: String by rootProperties
            library("jetbrains-kotlin-gradle", "org.jetbrains.kotlin:kotlin-gradle-plugin:$jetbrainsKotlinGradle")
        }

    }
}