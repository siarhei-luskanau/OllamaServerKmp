package io.ollama.server.core.ollama

import kotlinx.coroutines.flow.StateFlow

interface OllamaServerController {
    val status: StateFlow<ServerStatus>

    suspend fun start(): Result<Unit>

    suspend fun stop(): Result<Unit>

    suspend fun restart(): Result<Unit>
}
