package io.ollama.server.core.ollama

data class OllamaModel(
    val name: String,
    val family: String,
    val families: List<String>,
    val size: Long,
    val capabilities: List<String>,
) {
    fun getSizeString(): String =
        when {
            size >= 1_000_000_000L -> "${size / 1_000_000_000} GB"
            size >= 1_000_000L -> "${size / 1_000_000} MB"
            else -> "${size / 1_000} KB"
        }
}
