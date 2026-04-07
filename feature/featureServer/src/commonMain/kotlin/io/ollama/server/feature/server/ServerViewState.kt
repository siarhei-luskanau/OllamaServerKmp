package io.ollama.server.feature.server

sealed interface ServerViewState {
    data object Loading : ServerViewState

    data class Success(
        val isRunning: Boolean,
        val port: Int,
        val pid: Int?,
    ) : ServerViewState

    data class Error(
        val error: Throwable,
    ) : ServerViewState
}
