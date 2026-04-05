import androidx.compose.ui.window.ComposeUIViewController
import io.ollama.server.di.KoinApp
import platform.UIKit.UIViewController

fun mainViewController(): UIViewController =
    ComposeUIViewController {
        KoinApp()
    }
