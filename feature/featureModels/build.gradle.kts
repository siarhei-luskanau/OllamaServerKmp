plugins {
    id("composeMultiplatformConvention")
}

kotlin {
    android.namespace = "io.ollama.server.feature.models"
    sourceSets {
        commonMain.dependencies {
            implementation(projects.core.coreOllama)
        }
    }
}
