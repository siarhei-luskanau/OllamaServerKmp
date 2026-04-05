rootProject.name = "ollama-server-kmp"
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

include(
    ":app:androidApp",
    ":core:coreCommon",
    ":core:coreOllama",
    ":core:corePref",
    ":core:coreServer",
    ":diApp",
    ":feature:featureChat",
    ":feature:featureModels",
    ":feature:featureServer",
    ":navigation",
    ":ui:uiCommon",
    ":ui:uiMain",
    ":ui:uiSplash",
)

pluginManagement {
    includeBuild("convention-plugin-multiplatform")
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
                includeGroupByRegex("android.*")
            }
        }
        gradlePluginPortal()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
                includeGroupByRegex("android.*")
            }
        }
        mavenCentral()
    }
}
