package io.ollama.server.core.ollama

data class ServerStatus(
    val isRunning: Boolean,
    val port: Int,
    val pid: Int?,
)
