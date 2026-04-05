package io.ollama.server.core.ollama

interface OllamaApiClient {
    suspend fun listModels(): List<OllamaModel>

    suspend fun pullModel(name: String): OllamaModel

    suspend fun isServerReady(): Boolean
}
