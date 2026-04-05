package io.ollama.server.core.ollama

import ai.koog.prompt.executor.ollama.client.OllamaClient

class OllamaApiClientImpl(
    private val ollamaClient: OllamaClient,
) : OllamaApiClient {
    override suspend fun listModels(): List<OllamaModel> =
        ollamaClient.getModels().map { card ->
            OllamaModel(
                name = card.name,
                family = card.family,
                families = card.families.orEmpty(),
                size = card.size,
                capabilities = card.capabilities.map { it.id },
            )
        }

    override suspend fun pullModel(name: String): OllamaModel {
        val card =
            ollamaClient.getModelOrNull(name, pullIfMissing = true)
                ?: throw RuntimeException("Model not found and could not be pulled: $name")

        return OllamaModel(
            name = card.name,
            family = card.family,
            families = card.families.orEmpty(),
            size = card.size,
            capabilities = card.capabilities.map { it.id },
        )
    }

    override suspend fun isServerReady(): Boolean =
        try {
            ollamaClient.getModels()
            true
        } catch (_: Exception) {
            false
        }
}

fun provideOllamaApiClient(baseUrl: String): OllamaApiClient = OllamaApiClientImpl(ollamaClient = OllamaClient(baseUrl = baseUrl))
