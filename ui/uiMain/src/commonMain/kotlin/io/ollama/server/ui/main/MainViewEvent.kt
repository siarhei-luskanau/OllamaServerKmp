package io.ollama.server.ui.main

sealed interface MainViewEvent {
    data object NavigateBack : MainViewEvent
}
