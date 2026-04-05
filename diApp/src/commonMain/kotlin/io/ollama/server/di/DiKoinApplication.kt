package io.ollama.server.di

import io.ollama.server.core.common.CoreCommonCommonModule
import io.ollama.server.core.ollama.CoreOllamaCommonModule
import io.ollama.server.core.pref.CorePrefCommonModule
import io.ollama.server.navigation.NavigationCommonModule
import io.ollama.server.ui.main.MainCommonModule
import io.ollama.server.ui.splash.SplashCommonModule
import org.koin.core.annotation.KoinApplication

@KoinApplication(
    modules = [
        CoreCommonCommonModule::class,
        CoreOllamaCommonModule::class,
        CorePrefCommonModule::class,
        DiCommonModule::class,
        MainCommonModule::class,
        NavigationCommonModule::class,
        SplashCommonModule::class,
    ],
)
internal class DiKoinApplication
