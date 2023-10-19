rootProject.name = "Hippo"

enableFeaturePreview("VERSION_CATALOGS")

pluginManagement {
    includeBuild("build-logic")
}

include("asm-core")
include("magic-utils")
include("plugin")
