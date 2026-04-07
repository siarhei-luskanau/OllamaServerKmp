package io.ollama.server.feature.models

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.printToLog
import androidx.compose.ui.test.runComposeUiTest
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
internal class ModelsScreenCommonTest {
    @Test
    fun simpleCheck() =
        runComposeUiTest {
            setContent { ModelsScreenPreview() }
            waitForIdle()
            onRoot().printToLog("StartTag")
            onNodeWithText("llama3.2").assertIsDisplayed()
        }
}
