plugins {
    id("composeMultiplatformConvention")
}

kotlin {
    android.namespace = "io.ollama.server.feature.chat"
    sourceSets {
        commonMain.dependencies {
            implementation(projects.core.coreOllama)
        }
    }
}
