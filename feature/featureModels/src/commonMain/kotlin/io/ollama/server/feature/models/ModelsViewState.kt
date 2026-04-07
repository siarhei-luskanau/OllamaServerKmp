package io.ollama.server.feature.models

import io.ollama.server.core.ollama.OllamaModel

sealed interface ModelsViewState {
    data object Loading : ModelsViewState

    data class Success(
        val models: List<OllamaModel>,
    ) : ModelsViewState

    data class Error(
        val error: Throwable,
    ) : ModelsViewState
}
