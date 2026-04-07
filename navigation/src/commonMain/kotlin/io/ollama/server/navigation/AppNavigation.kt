package io.ollama.server.navigation

import androidx.compose.runtime.mutableStateListOf
import androidx.navigation3.runtime.NavKey
import io.ollama.server.feature.models.ModelsNavigationCallback
import io.ollama.server.feature.server.ServerNavigationCallback
import io.ollama.server.feature.settings.SettingsNavigationCallback
import io.ollama.server.ui.main.MainNavigationCallback
import io.ollama.server.ui.splash.SplashNavigationCallback
import org.koin.core.annotation.Single

@Single
internal class AppNavigation :
    MainNavigationCallback,
    ModelsNavigationCallback,
    ServerNavigationCallback,
    SettingsNavigationCallback,
    SplashNavigationCallback {
    val backStack = mutableStateListOf<NavKey>(AppRoutes.Splash)

    override fun goBack() {
        if (backStack.size > 1) {
            backStack.removeLastOrNull()
        }
    }

    override fun goMainScreen(initArg: String) {
        backStack.add(AppRoutes.Server)
        backStack.remove(AppRoutes.Splash)
    }

    override fun goModelsScreen() {
        backStack.add(AppRoutes.Models)
    }

    override fun goSettingsScreen() {
        backStack.add(AppRoutes.Settings)
    }
}
