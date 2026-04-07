package io.ollama.server.feature.server

sealed interface ServerViewEvent {
    data object StartServer : ServerViewEvent

    data object StopServer : ServerViewEvent

    data object RestartServer : ServerViewEvent

    data object NavigateToModels : ServerViewEvent

    data object NavigateToSettings : ServerViewEvent
}
