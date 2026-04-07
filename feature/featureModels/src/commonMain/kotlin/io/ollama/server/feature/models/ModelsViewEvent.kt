package io.ollama.server.feature.models

sealed interface ModelsViewEvent {
    data object NavigateBack : ModelsViewEvent

    data object LoadModels : ModelsViewEvent

    data class PullModel(
        val name: String,
    ) : ModelsViewEvent
}
