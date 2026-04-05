package io.ollama.server.core.ollama

import android.content.Context
import android.content.Intent
import io.ollama.server.core.ollama.OllamaServerController
import io.ollama.server.core.ollama.ServerStatus
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.koin.core.annotation.Single
import java.net.HttpURLConnection
import java.net.URL

@Single
internal class AndroidOllamaServerController(
    private val context: Context,
) : OllamaServerController {
    private val _status = MutableStateFlow(ServerStatus(isRunning = false, port = PORT, pid = null))
    override val status: StateFlow<ServerStatus> = _status.asStateFlow()

    override suspend fun start(): Result<Unit> {
        context.startForegroundService(Intent(context, OllamaForegroundService::class.java))
        return waitForServerReady()
    }

    override suspend fun stop(): Result<Unit> {
        context.stopService(Intent(context, OllamaForegroundService::class.java))
        _status.value = ServerStatus(isRunning = false, port = PORT, pid = null)
        return Result.success(Unit)
    }

    override suspend fun restart(): Result<Unit> {
        stop()
        delay(500)
        return start()
    }

    private suspend fun waitForServerReady(timeoutMs: Long = 30_000): Result<Unit> {
        val deadline = System.currentTimeMillis() + timeoutMs
        while (System.currentTimeMillis() < deadline) {
            if (checkServerReady()) {
                _status.value = ServerStatus(isRunning = true, port = PORT, pid = null)
                return Result.success(Unit)
            }
            delay(500)
        }
        return Result.failure(Exception("Ollama server did not start within ${timeoutMs}ms"))
    }

    private fun checkServerReady(): Boolean =
        try {
            val connection = URL("http://127.0.0.1:$PORT/api/tags").openConnection() as HttpURLConnection
            connection.connectTimeout = 1_000
            connection.readTimeout = 1_000
            val code = connection.responseCode
            connection.disconnect()
            code in 200..299
        } catch (_: Exception) {
            false
        }

    companion object {
        private const val PORT = 11434
    }
}
