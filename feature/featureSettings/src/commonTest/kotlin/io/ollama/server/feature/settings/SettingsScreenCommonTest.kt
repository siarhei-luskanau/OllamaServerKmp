package io.ollama.server.feature.settings

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.printToLog
import androidx.compose.ui.test.runComposeUiTest
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
internal class SettingsScreenCommonTest {
    @Test
    fun simpleCheck() =
        runComposeUiTest {
            setContent { SettingsScreenPreview() }
            waitForIdle()
            onRoot().printToLog("StartTag")
            onNodeWithText("Server URL: http://127.0.0.1:11434").assertIsDisplayed()
        }
}
