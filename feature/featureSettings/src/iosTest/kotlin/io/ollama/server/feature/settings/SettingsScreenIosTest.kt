package io.ollama.server.feature.settings

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.runSkikoComposeUiTest
import com.github.takahirom.roborazzi.ExperimentalRoborazziApi
import io.github.takahirom.roborazzi.captureRoboImage
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class, ExperimentalRoborazziApi::class)
internal class SettingsScreenIosTest {
    @Test
    fun preview() =
        runSkikoComposeUiTest {
            setContent { SettingsScreenPreview() }
            waitForIdle()
            onRoot().captureRoboImage(this, filePath = "io.ollama.server.feature.settings.SettingsScreenIosTest.preview.png")
        }
}
