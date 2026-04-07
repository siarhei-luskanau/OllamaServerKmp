package io.ollama.server.feature.server

import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Module

@Module
@ComponentScan(value = ["io.ollama.server.feature.server"])
class ServerCommonModule
