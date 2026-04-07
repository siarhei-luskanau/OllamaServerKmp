package io.ollama.server.navigation

import io.ollama.server.feature.models.ModelsScreen
import io.ollama.server.feature.server.ServerScreen
import io.ollama.server.ui.main.MainScreen
import io.ollama.server.ui.splash.SplashScreen
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.core.annotation.Module
import org.koin.core.parameter.parametersOf
import org.koin.dsl.module
import org.koin.dsl.navigation3.navigation

@Module
@ComponentScan(value = ["io.ollama.server.navigation"])
class NavigationCommonModule

@OptIn(KoinExperimentalAPI::class)
val navigationModule =
    module {
        navigation<AppRoutes.Splash> {
            SplashScreen(viewModel = koinViewModel())
        }
        navigation<AppRoutes.Main> { route ->
            MainScreen(viewModel = koinViewModel { parametersOf(route.initArg) })
        }
        navigation<AppRoutes.Server> {
            ServerScreen(viewModel = koinViewModel())
        }
        navigation<AppRoutes.Models> {
            ModelsScreen(viewModel = koinViewModel())
        }
    }
