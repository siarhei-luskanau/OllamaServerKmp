plugins {
    id("composeMultiplatformConvention")
}

kotlin {
    android.namespace = "io.ollama.server.feature.server"
    sourceSets {
        commonMain.dependencies {
            implementation(projects.core.coreOllama)
        }
    }
}
