package io.ollama.server.di

import io.ollama.server.core.common.CoreCommonCommonModule
import io.ollama.server.core.ollama.CoreOllamaCommonModule
import io.ollama.server.core.pref.CorePrefCommonModule
import io.ollama.server.feature.models.ModelsCommonModule
import io.ollama.server.feature.server.ServerCommonModule
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
        ModelsCommonModule::class,
        NavigationCommonModule::class,
        ServerCommonModule::class,
        SplashCommonModule::class,
    ],
)
internal class DiKoinApplication
