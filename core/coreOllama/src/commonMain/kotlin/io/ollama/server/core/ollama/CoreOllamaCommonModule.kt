package io.ollama.server.core.ollama

import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

@Module
@ComponentScan("io.ollama.server.core.ollama")
class CoreOllamaCommonModule {
    @Single
    fun ollamaApiClient(): OllamaApiClient = provideOllamaApiClient("http://127.0.0.1:11434")
}
