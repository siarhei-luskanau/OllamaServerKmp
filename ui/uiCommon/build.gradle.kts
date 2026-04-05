plugins {
    id("composeMultiplatformConvention")
}

kotlin.android.namespace = "io.ollama.server.ui.common"

compose.resources {
    publicResClass = true
    packageOfResClass = "${kotlin.android.namespace}.resources"
    generateResClass = always
}
