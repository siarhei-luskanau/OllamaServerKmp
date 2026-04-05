plugins {
    id("composeMultiplatformConvention")
}

kotlin {
    android.namespace = "io.ollama.server.core.ollama"
    sourceSets {
        commonMain.dependencies {
            implementation(libs.koog.agents)
            implementation(libs.koog.prompt.executor.ollama)
            implementation(project.dependencies.platform(libs.ktor.bom))
        }

        androidMain.dependencies {
            implementation(libs.ktor.client.okhttp)
        }

        iosMain.dependencies {
            implementation(libs.ktor.client.darwin)
        }
    }
}
