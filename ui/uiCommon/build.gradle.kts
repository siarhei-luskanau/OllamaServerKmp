plugins {
    id("composeMultiplatformConvention")
}

kotlin.android.namespace = "template.ui.common"

compose.resources {
    publicResClass = true
    packageOfResClass = "${kotlin.android.namespace}.resources"
    generateResClass = always
}
