plugins {
    id("composeMultiplatformConvention")
}

kotlin {
    android.namespace = "io.ollama.server.core.server"
    sourceSets {
        commonMain.dependencies {
            implementation(projects.core.coreOllama)
        }
    }
}
