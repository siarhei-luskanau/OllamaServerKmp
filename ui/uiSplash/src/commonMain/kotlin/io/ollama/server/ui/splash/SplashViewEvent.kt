package io.ollama.server.ui.splash

sealed interface SplashViewEvent {
    data object Launched : SplashViewEvent
}
