package io.ollama.server.core.ollama

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class IosOllamaServerController : OllamaServerController {
    private val _status = MutableStateFlow(ServerStatus(isRunning = false, port = 11434, pid = null))
    override val status: StateFlow<ServerStatus> = _status.asStateFlow()

    override suspend fun start(): Result<Unit> = Result.failure(UnsupportedOperationException("iOS server not yet implemented (Phase 3)"))

    override suspend fun stop(): Result<Unit> = Result.success(Unit)

    override suspend fun restart(): Result<Unit> = Result.failure(UnsupportedOperationException("iOS server not yet implemented (Phase 3)"))
}
