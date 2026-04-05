package io.ollama.server.ui.main

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.runSkikoComposeUiTest
import com.github.takahirom.roborazzi.ExperimentalRoborazziApi
import io.github.takahirom.roborazzi.captureRoboImage
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class, ExperimentalRoborazziApi::class)
internal class MainScreenIosTest {
    @Test
    fun preview() =
        runSkikoComposeUiTest {
            setContent { MainScreenPreview() }
            waitForIdle()
            onRoot().captureRoboImage(this, filePath = "io.ollama.server.ui.main.MainScreenIosTest.preview.png")
        }
}
