package io.ollama.server.core.ollama

import io.ktor.http.ContentType
import io.ktor.server.application.call
import io.ktor.server.cio.CIO
import io.ktor.server.cio.CIOApplicationEngine
import io.ktor.server.engine.EmbeddedServer
import io.ktor.server.engine.embeddedServer
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.routing
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.koin.core.annotation.Single

@Single
class IosOllamaServerController : OllamaServerController {
    private val _status = MutableStateFlow(ServerStatus(isRunning = false, port = PORT, pid = null))
    override val status: StateFlow<ServerStatus> = _status.asStateFlow()

    private var server: EmbeddedServer<CIOApplicationEngine, CIOApplicationEngine.Configuration>? = null

    override suspend fun start(): Result<Unit> =
        runCatching {
            server =
                embeddedServer(CIO, port = PORT, host = "127.0.0.1") {
                    routing {
                        get("/") {
                            call.respondText("Ollama is running", ContentType.Text.Plain)
                        }
                        get("/api/tags") {
                            call.respondText("""{"models":[]}""", ContentType.Application.Json)
                        }
                        get("/api/ps") {
                            call.respondText("""{"models":[]}""", ContentType.Application.Json)
                        }
                        post("/api/show") {
                            call.respondText("""{"error":"model not found"}""", ContentType.Application.Json)
                        }
                        post("/api/pull") {
                            call.respondText("""{"error":"model pull not supported on iOS"}""", ContentType.Application.Json)
                        }
                    }
                }
            server!!.start(wait = false)
            _status.value = ServerStatus(isRunning = true, port = PORT, pid = null)
        }

    override suspend fun stop(): Result<Unit> =
        runCatching {
            server?.stop(gracePeriodMillis = 1_000, timeoutMillis = 5_000)
            server = null
            _status.value = ServerStatus(isRunning = false, port = PORT, pid = null)
        }

    override suspend fun restart(): Result<Unit> {
        stop()
        return start()
    }

    companion object {
        private const val PORT = 11434
    }
}
