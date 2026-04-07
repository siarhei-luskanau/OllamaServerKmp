package io.ollama.server.feature.settings

sealed interface SettingsViewEvent {
    data object NavigateBack : SettingsViewEvent
}
