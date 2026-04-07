package io.ollama.server.feature.settings

sealed interface SettingsViewState {
    data object Loading : SettingsViewState

    data class Success(
        val serverUrl: String,
    ) : SettingsViewState

    data class Error(
        val error: Throwable,
    ) : SettingsViewState
}
