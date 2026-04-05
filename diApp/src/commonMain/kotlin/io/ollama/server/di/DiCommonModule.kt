package io.ollama.server.di

import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Module

@Module
@ComponentScan(value = ["io.ollama.server.di"])
class DiCommonModule
